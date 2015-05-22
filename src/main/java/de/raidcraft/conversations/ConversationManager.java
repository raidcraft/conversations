package de.raidcraft.conversations;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.Component;
import de.raidcraft.api.config.SimpleConfiguration;
import de.raidcraft.api.conversations.ConversationProvider;
import de.raidcraft.api.conversations.Conversations;
import de.raidcraft.api.conversations.answer.Answer;
import de.raidcraft.api.conversations.conversation.Conversation;
import de.raidcraft.api.conversations.conversation.ConversationEndReason;
import de.raidcraft.api.conversations.conversation.ConversationTemplate;
import de.raidcraft.api.conversations.conversation.ConversationVariable;
import de.raidcraft.api.conversations.host.ConversationHost;
import de.raidcraft.api.conversations.stage.StageTemplate;
import de.raidcraft.conversations.answers.DefaultAnswer;
import de.raidcraft.conversations.answers.InputAnswer;
import de.raidcraft.conversations.answers.SimpleAnswer;
import de.raidcraft.conversations.conversations.DefaultConversationTemplate;
import de.raidcraft.conversations.stages.DefaultStageTemplate;
import de.raidcraft.conversations.tables.TPlayerConversation;
import de.raidcraft.util.CaseInsensitiveMap;
import de.raidcraft.util.ConfigUtil;
import de.raidcraft.util.LocationUtil;
import de.raidcraft.util.UUIDUtil;
import mkremins.fanciful.FancyMessage;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * @author mdoering
 */
public class ConversationManager implements ConversationProvider, Component {

    private final RCConversationsPlugin plugin;
    private final Map<String, Constructor<? extends Answer>> answerTemplates = new CaseInsensitiveMap<>();
    private final Map<String, Constructor<? extends StageTemplate>> stageTemplates = new CaseInsensitiveMap<>();
    private final Map<String, Constructor<? extends ConversationTemplate>> conversationTemplates = new CaseInsensitiveMap<>();
    private final Map<Class<?>, Constructor<? extends ConversationHost<?>>> hostTemplates = new HashMap<>();
    private final Map<String, ConversationVariable> variables = new CaseInsensitiveMap<>();
    private final Map<String, ConversationTemplate> conversations = new CaseInsensitiveMap<>();
    private final Map<UUID, Conversation<Player>> activeConversations = new HashMap<>();
    private final Map<Object, ConversationHost<?>> cachedHosts = new HashMap<>();

    public ConversationManager(RCConversationsPlugin plugin) {

        this.plugin = plugin;
        RaidCraft.registerComponent(ConversationManager.class, this);
        Conversations.enable(this);
        registerConversationTemplate(ConversationTemplate.DEFAULT_CONVERSATION_TEMPLATE, DefaultConversationTemplate.class);
        registerStage(StageTemplate.DEFAULT_STAGE_TEMPLATE, DefaultStageTemplate.class);
        registerAnswer(Answer.DEFAULT_ANSWER_TEMPLATE, DefaultAnswer.class);
        registerAnswer(Answer.ANSWER_INPUT_TYPE, InputAnswer.class);
        registerConversationVariable("%name", conversation -> conversation.getEntity().getName());
        load();
    }

    public void reload() {

        unload();
        load();
    }

    public void load() {

        loadConversations(new File(plugin.getDataFolder(), "conversations"), "");
    }

    public void unload() {

        conversations.clear();
        cachedHosts.clear();
    }

    private void loadConversations(File path, String base) {

        File[] files = path.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) {
                loadConversations(path, base + file.getName() + ".");
            } else {
                loadConversation(base + file.getName().replace(".yml", ""), plugin.configure(new SimpleConfiguration<>(plugin, file)));
            }
        }
    }

    public void checkDistance(Player player) {

        Optional<Conversation<Player>> activeConversation = getActiveConversation(player);
        if (!activeConversation.isPresent()) {
            return;
        }

        Location hostLocation = activeConversation.get().getHost().getLocation();
        if (hostLocation == null) {
            activeConversation.get().abort(ConversationEndReason.OUT_OF_RANGE);
            return;
        }

        int distance = LocationUtil.getBlockDistance(player.getLocation(), hostLocation);

        if (distance < RaidCraft.getComponent(RCConversationsPlugin.class).getConfiguration().conversationAbortWarnRadius) {
            activeConversation.get().set("distance-warned", false);
            return;
        }

        if (!activeConversation.get().getBoolean("distance-warned") && distance == RaidCraft.getComponent(RCConversationsPlugin.class).getConfiguration().conversationAbortWarnRadius) {
            player.sendMessage(ChatColor.GRAY + ChatColor.ITALIC.toString() + "Du entfernst dich von deinem Gesprächspartner!");
            activeConversation.get().set("distance-warned", true);
            return;
        }

        if (distance > RaidCraft.getComponent(RCConversationsPlugin.class).getConfiguration().conversationAbortRadius) {
            player.sendMessage(ChatColor.GRAY + ChatColor.ITALIC.toString() + "Ihr versteht euch nicht mehr.");
            activeConversation.get().abort(ConversationEndReason.OUT_OF_RANGE);
        }
    }

    @Override
    public void registerAnswer(String type, Class<? extends Answer> answer) {

        try {
            Constructor<? extends Answer> constructor = answer.getDeclaredConstructor(String.class, ConfigurationSection.class);
            constructor.setAccessible(true);
            answerTemplates.put(type, constructor);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Optional<Answer> getAnswer(StageTemplate stageTemplate, ConfigurationSection config) {

        Constructor<? extends Answer> constructor;
        String type;
        if (config.isSet("type")) {
            type = config.getString("type");
        } else {
            type = Answer.DEFAULT_ANSWER_TEMPLATE;
        }
        constructor = answerTemplates.get(type);
        if (constructor != null) {
            try {
                return Optional.of(constructor.newInstance(type, config));
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return Optional.empty();
    }

    @Override
    public Answer getAnswer(String text) {

        return new SimpleAnswer(text);
    }

    @Override
    public Answer getAnswer(FancyMessage message) {

        return new SimpleAnswer(message);
    }

    @Override
    public void registerStage(String type, Class<? extends StageTemplate> stage) {

        try {
            Constructor<? extends StageTemplate> constructor = stage.getDeclaredConstructor(String.class, ConversationTemplate.class, ConfigurationSection.class);
            constructor.setAccessible(true);
            stageTemplates.put(type, constructor);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Optional<StageTemplate> getStageTemplate(String identifier, ConversationTemplate conversationTemplate, ConfigurationSection config) {

        Constructor<? extends StageTemplate> constructor;
        if (config.isSet("type")) {
            constructor = stageTemplates.get(config.getString("type"));
        } else {
            constructor = stageTemplates.get(StageTemplate.DEFAULT_STAGE_TEMPLATE);
        }
        if (constructor != null) {
            try {
                return Optional.of(constructor.newInstance(identifier, conversationTemplate, config));
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return Optional.empty();
    }

    @Override
    public void registerConversationTemplate(String type, Class<? extends ConversationTemplate> conversationTemplate) {

        try {
            Constructor<? extends ConversationTemplate> constructor = conversationTemplate.getDeclaredConstructor(String.class, ConfigurationSection.class);
            constructor.setAccessible(true);
            conversationTemplates.put(type, constructor);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Optional<ConversationTemplate> createConversationTemplate(String identifier, ConfigurationSection config) {

        Constructor<? extends ConversationTemplate> constructor;
        if (config.isSet("type")) {
            constructor = conversationTemplates.get(config.getString("type"));
        } else {
            constructor = conversationTemplates.get(ConversationTemplate.DEFAULT_CONVERSATION_TEMPLATE);
        }
        if (constructor != null) {
            try {
                return Optional.of(constructor.newInstance(identifier, config));
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return Optional.empty();
    }

    @Override
    public <T> void registerConversationHost(Class<T> type, Class<? extends ConversationHost<T>> host) {

        if (hostTemplates.containsKey(type)) {
            plugin.getLogger().warning(host.getCanonicalName() + ": ConversationHost with the type " + type.getCanonicalName()
                    + " is already registered: " + hostTemplates.get(type).getClass().getCanonicalName());
            return;
        }
        try {
            Constructor<? extends ConversationHost<T>> constructor = host.getDeclaredConstructor(type, ConfigurationSection.class);
            constructor.setAccessible(true);
            hostTemplates.put(type, constructor);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<ConversationHost<T>> createConversationHost(T type, ConfigurationSection config) {

        if (cachedHosts.containsKey(type)) {
            return Optional.of((ConversationHost<T>) cachedHosts.get(type));
        }
        try {
            for (Map.Entry<Class<?>, Constructor<? extends ConversationHost<?>>> entry : hostTemplates.entrySet()) {
                if (entry.getKey().isAssignableFrom(type.getClass())) {
                    ConversationHost<T> host = (ConversationHost<T>) entry.getValue().newInstance(type, config);
                    // lets load all saved player conversations
                    List<TPlayerConversation> conversationList = plugin.getDatabase().find(TPlayerConversation.class).where()
                            .eq("host", host.getUniqueId())
                            .findList();
                    for (TPlayerConversation savedConversation : conversationList) {
                        Optional<ConversationTemplate> template = getLoadedConversationTemplate(savedConversation.getConversation());
                        if (!template.isPresent()) {
                            plugin.getLogger().warning("Host tried to load unknown Saved ConversationTemplate (" + savedConversation.getId() + ") "
                                    + savedConversation.getConversation() + " for player "
                                    + UUIDUtil.getNameFromUUID(savedConversation.getPlayer()) + ": " + ConfigUtil.getFileName(config));
                        } else {
                            host.setConversation(savedConversation.getPlayer(), template.get());
                        }
                    }
                    cachedHosts.put(type, host);
                    return Optional.of(host);
                }
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<ConversationHost<T>> getConversationHost(T type) {

        return Optional.of((ConversationHost<T>) cachedHosts.get(type));
    }

    @Override
    public void registerConversationVariable(String name, ConversationVariable variable) {

        variables.put(name, variable);
    }

    @Override
    public Map<String, ConversationVariable> getConversationVariables() {

        return variables;
    }

    @Override
    public Optional<ConversationTemplate> loadConversation(String identifier, ConfigurationSection config) {

        if (conversations.containsKey(identifier)) {
            plugin.getLogger().warning("Tried to register duplicate conversation: " + identifier + " from " + ConfigUtil.getFileName(config));
            return Optional.of(conversations.get(identifier));
        }
        Optional<ConversationTemplate> template = createConversationTemplate(identifier, config);
        if (!template.isPresent()) {
            plugin.getLogger().warning("Could not find conversation template type " + config.getString("type") + " in " + ConfigUtil.getFileName(config));
            return Optional.empty();
        }
        conversations.put(identifier, template.get());
        return template;
    }

    @Override
    public Optional<ConversationTemplate> getLoadedConversationTemplate(String identifier) {

        return Optional.ofNullable(conversations.get(identifier));
    }

    @Override
    public List<ConversationTemplate> findConversationTemplate(String identifier) {

        ArrayList<ConversationTemplate> templates = new ArrayList<>();
        Optional<ConversationTemplate> template = getLoadedConversationTemplate(identifier);
        if (template.isPresent()) {
            templates.add(template.get());
            return templates;
        }
        identifier = identifier.toLowerCase().trim();
        for (Map.Entry<String, ConversationTemplate> entry : conversations.entrySet()) {
            if (entry.getKey().toLowerCase().endsWith(identifier)) {
                templates.add(entry.getValue());
            }
        }
        return templates;
    }

    @Override
    public Optional<Conversation<Player>> startConversation(Player player, ConversationHost<?> conversationHost) {

        Optional<ConversationTemplate> conversation = conversationHost.getConversation(player);
        if (conversation.isPresent()) {
            return Optional.of(conversation.get().startConversation(player, conversationHost));
        }
        return Optional.empty();
    }

    @Override
    public Optional<Conversation<Player>> getActiveConversation(Player player) {

        return Optional.ofNullable(activeConversations.get(player.getUniqueId()));
    }

    @Override
    public Optional<Conversation<Player>> setActiveConversation(Conversation<Player> conversation) {

        Optional<Conversation<Player>> activeConversation = removeActiveConversation(conversation.getEntity());
        activeConversations.put(conversation.getEntity().getUniqueId(), conversation);
        return activeConversation;
    }

    @Override
    public Optional<Conversation<Player>> removeActiveConversation(Player player) {

        Conversation<Player> conversation = activeConversations.remove(player.getUniqueId());
        if (conversation != null) {
            conversation.abort(ConversationEndReason.START_NEW_CONVERSATION);
        }
        return Optional.ofNullable(conversation);
    }

    @Override
    public boolean hasActiveConversation(Player player) {

        return activeConversations.containsKey(player.getUniqueId());
    }
}
