package de.raidcraft.conversations;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.Component;
import de.raidcraft.api.action.Timer;
import de.raidcraft.api.action.action.Action;
import de.raidcraft.api.action.flow.Flow;
import de.raidcraft.api.config.SimpleConfiguration;
import de.raidcraft.api.conversations.ConversationProvider;
import de.raidcraft.api.conversations.Conversations;
import de.raidcraft.api.conversations.answer.Answer;
import de.raidcraft.api.conversations.answer.InputAnswer;
import de.raidcraft.api.conversations.answer.SimpleAnswer;
import de.raidcraft.api.conversations.builder.CodedConversationTemplate;
import de.raidcraft.api.conversations.conversation.Conversation;
import de.raidcraft.api.conversations.conversation.ConversationEndReason;
import de.raidcraft.api.conversations.conversation.ConversationTemplate;
import de.raidcraft.api.conversations.conversation.ConversationVariable;
import de.raidcraft.api.conversations.host.ConversationHost;
import de.raidcraft.api.conversations.host.ConversationHostFactory;
import de.raidcraft.api.conversations.host.PlayerHost;
import de.raidcraft.api.conversations.stage.Stage;
import de.raidcraft.api.conversations.stage.StageTemplate;
import de.raidcraft.conversations.answers.DefaultAnswer;
import de.raidcraft.conversations.conversations.DefaultConversationTemplate;
import de.raidcraft.conversations.conversations.PlayerConversation;
import de.raidcraft.conversations.hosts.NPCHost;
import de.raidcraft.conversations.stages.DefaultStageTemplate;
import de.raidcraft.conversations.stages.DynamicStageTemplate;
import de.raidcraft.conversations.tables.TPersistentHost;
import de.raidcraft.conversations.tables.TPersistentHostOption;
import de.raidcraft.conversations.tables.TPlayerConversation;
import de.raidcraft.util.*;
import de.raidcraft.util.fanciful.FancyMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author mdoering
 */
public class ConversationManager implements ConversationProvider, Component {

    private final RCConversationsPlugin plugin;
    private final Map<String, Constructor<? extends Answer>> answerTemplates = new CaseInsensitiveMap<>();
    private final Map<String, Constructor<? extends StageTemplate>> stageTemplates = new CaseInsensitiveMap<>();
    private final Map<String, Constructor<? extends ConversationTemplate>> conversationTemplates = new CaseInsensitiveMap<>();
    private final Map<String, Constructor<? extends Conversation>> conversationTypes = new CaseInsensitiveMap<>();
    private final Map<String, ConversationHostFactory<?>> hostFactories = new CaseInsensitiveMap<>();
    private final Map<Pattern, ConversationVariable> variables = new HashMap<>();
    private final Map<String, ConversationTemplate> conversations = new CaseInsensitiveMap<>();
    private final Map<UUID, Conversation> activeConversations = new HashMap<>();
    private final Map<String, ConversationHost<?>> cachedHosts = new CaseInsensitiveMap<>();

    public ConversationManager(RCConversationsPlugin plugin) {

        this.plugin = plugin;
        RaidCraft.registerComponent(ConversationManager.class, this);
        Conversations.enable(this);
        registerConversationTemplate(ConversationTemplate.DEFAULT_CONVERSATION_TEMPLATE, DefaultConversationTemplate.class);
        registerConversationType(Conversation.DEFAULT_TYPE, PlayerConversation.class);
        registerStageTemplate(StageTemplate.DEFAULT_STAGE_TEMPLATE, DefaultStageTemplate.class);
        registerAnswer(Answer.DEFAULT_ANSWER_TEMPLATE, DefaultAnswer.class);
        registerAnswer(Answer.DEFAULT_INPUT_TYPE, InputAnswer.class);

        registerConversationVariable(Pattern.compile("%\\[([\\w_\\-\\d]+)\\]"), (matcher, conversation) -> conversation.getString(matcher.group(1)));

        RaidCraft.registerPlayerVariable(Pattern.compile("%name"), (matcher, player) -> player.getName());
        RaidCraft.registerPlayerVariable(Pattern.compile("%\\*\\[([\\w_\\-\\d\\.]+)\\]"), (matcher, player) -> {
            Optional<Timer> activeTimer = Timer.getActiveTimer(player, matcher.group(1));
            return activeTimer.map(timer -> TimeUtil.getAccurrateShortFormatedTime(TimeUtil.ticksToMillis(timer.getRemainingTime())))
                    .orElse("[Invalid Timer]");
        });

        registerHostFactory("NPC", new NPCHost.NPCHostFactory());

        Bukkit.getScheduler().runTaskLater(plugin, this::load, 1 * 20L);
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
                loadConversations(file, base + file.getName() + ".");
            } else {
                loadConversation(base + file.getName().replace(".yml", ""), plugin.configure(new SimpleConfiguration<>(plugin, file)));
            }
        }
    }

    public void checkDistance(Player player) {

        Optional<Conversation> activeConversation = getActiveConversation(player);
        if (!activeConversation.isPresent() || !activeConversation.get().getTemplate().isEndingOutOfRange()) {
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
    public List<Answer> createAnswers(StageTemplate template, ConfigurationSection config) {

        List<Answer> answers = new ArrayList<>();
        if (config == null) return answers;
        // lets process our flow answers first
        answers.addAll(Flow.parseAnswers(template, config));
        // and now the block statements
        for (String key : config.getKeys(false)) {
            // handled by flow
            if (config.isList(key)) continue;
            ConfigurationSection section = config.getConfigurationSection(key);
            if (key.equals("input") && !section.isSet("type")) {
                section.set("type", Answer.DEFAULT_INPUT_TYPE);
            }
            Optional<Answer> answer = Conversations.getAnswer(template, section);
            if (answer.isPresent()) {
                answers.add(answer.get());
            } else {
                RaidCraft.LOGGER.warning("Unknown answer type " + section.getString("type") + " in " + ConfigUtil.getFileName(config));
            }
        }
        return answers;
    }

    @Override
    public Optional<Answer> getAnswer(StageTemplate stageTemplate, ConfigurationSection config) {


        String type;
        if (config.isSet("type")) {
            type = config.getString("type");
        } else {
            type = Answer.DEFAULT_ANSWER_TEMPLATE;
        }
        return getAnswer(type, config);
    }

    @Override
    public Optional<Answer> getAnswer(String type, ConfigurationSection config) {

        Constructor<? extends Answer> constructor = answerTemplates.get(type);
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
    public void registerStageTemplate(String type, Class<? extends StageTemplate> stage) {

        try {
            Constructor<? extends StageTemplate> constructor = stage.getDeclaredConstructor(String.class);
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
                Optional<StageTemplate> stageTemplate = Optional.of(constructor.newInstance(identifier));
                stageTemplate.ifPresent(template -> {
                    template.loadConfig(config);
                    template.setConversationTemplate(conversationTemplate);
                });
                return stageTemplate;
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
                Optional<ConversationTemplate> template = Optional.of(constructor.newInstance(identifier, config));
                if (template.isPresent()) {
                    template.get().loadConfig(config);
                }
                return template;
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return Optional.empty();
    }

    @Override
    public void registerConversationType(String type, Class<? extends Conversation> conversation) {

        try {
            Constructor<? extends Conversation> constructor = conversation.getDeclaredConstructor(Player.class, ConversationTemplate.class, ConversationHost.class);
            constructor.setAccessible(true);
            conversationTypes.put(type, constructor);
            plugin.getLogger().info("registered conversation type " + type + " -> " + conversation.getCanonicalName());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Conversation startConversation(String type, Player player, ConversationTemplate template, ConversationHost host) {

        try {
            if (!conversationTypes.containsKey(type)) {
                type = Conversation.DEFAULT_TYPE;
            }
            Constructor<? extends Conversation> constructor = conversationTypes.get(type);
            return constructor.newInstance(player, template, host);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void registerHostFactory(String identifier, ConversationHostFactory<?> factory) {

        if (hostFactories.containsKey(identifier)) {
            plugin.getLogger().warning("HostFactory " + identifier + " is already registered: "
                    + hostFactories.get(identifier).getClass().getCanonicalName() + ". " +
                    "Cannot register duplicate factory: " + factory.getClass().getCanonicalName());
            return;
        }
        hostFactories.put(identifier, factory);
    }

    private void loadSavedHostConversations(ConversationHost host) {

        // lets loadConfig all saved player conversations
        List<TPlayerConversation> conversationList = plugin.getDatabase().find(TPlayerConversation.class).where()
                .eq("host", host.getUniqueId())
                .findList();
        for (TPlayerConversation savedConversation : conversationList) {
            Optional<ConversationTemplate> template = getLoadedConversationTemplate(savedConversation.getConversation());
            if (!template.isPresent()) {
                plugin.getLogger().warning("Host tried to loadConfig unknown Saved ConversationTemplate (" + savedConversation.getId() + ") "
                        + savedConversation.getConversation() + " for player "
                        + UUIDUtil.getNameFromUUID(savedConversation.getPlayer()));
            } else {
                host.setConversation(savedConversation.getPlayer(), template.get());
            }
        }
    }

    @Override
    public Optional<ConversationHost<?>> createConversationHost(String creatingPlugin, String identifier, String type, Location location) {

        if (cachedHosts.containsKey(identifier)) {
            return Optional.of(cachedHosts.get(identifier));
        }
        if (!hostFactories.containsKey(type)) {
            plugin.getLogger().warning("Could not find host factory " + type);
            return Optional.empty();
        }
        ConversationHostFactory<?> factory = hostFactories.get(type);
        return Optional.of(factory.create(creatingPlugin, identifier, location));
    }

    @Override
    public Optional<ConversationHost<?>> createConversationHost(String creatingPlugin, String identifier, ConfigurationSection config) {

        if (cachedHosts.containsKey(identifier)) {
            return Optional.of(cachedHosts.get(identifier));
        }
        String type = config.getString("type");
        if (!hostFactories.containsKey(type)) {
            plugin.getLogger().warning("Could not find host factory " + type + " in " + ConfigUtil.getFileName(config));
            return Optional.empty();
        }
        Location location = ConfigUtil.getLocationFromConfig(config.getConfigurationSection("location"));
        if (location == null) {
            plugin.getLogger().warning("Location in " + ConfigUtil.getFileName(config) + " not defined!");
            return Optional.empty();
        }
        Optional<ConversationHost<?>> host = createConversationHost(creatingPlugin, identifier, type, location);
        if (host.isPresent()) {
            host.get().load(config);
            loadSavedHostConversations(host.get());
            cachedHosts.put(identifier, host.get());
        }
        return host;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<ConversationHost<T>> createConversationHost(T host, ConfigurationSection config) {

        String type = config.getString("type");
        if (!hostFactories.containsKey(type)) {
            plugin.getLogger().warning("Could not find host factory " + type + " in " + ConfigUtil.getFileName(config));
            return Optional.empty();
        }
        ConversationHostFactory<T> factory = (ConversationHostFactory<T>) hostFactories.get(type);
        ConversationHost<T> conversationHost = factory.create(host);
        // loadConfig all saved player conversations
        conversationHost.load(config);
        loadSavedHostConversations(conversationHost);

        return Optional.of(conversationHost);
    }

    public Optional<ConversationHost<?>> createConversationHost(TPersistentHost host) {

        Optional<Location> location = host.getLocation();
        if (location.isPresent()) {
            Optional<ConversationHost<?>> conversationHost = createConversationHost(plugin.getName(), UUID.randomUUID().toString(), host.getHostType(), location.get());
            if (!conversationHost.isPresent()) {
                plugin.getLogger().warning("unable to loadConfig persistent host " + host.getId() + " at " + location.get());
            } else {
                ConversationHost<?> loadedHost = conversationHost.get();
                Optional<ConversationTemplate> conversationTemplate = getLoadedConversationTemplate(host.getConversation());
                if (conversationTemplate.isPresent()) {
                    loadedHost.addDefaultConversation(conversationTemplate.get());
                } else {
                    plugin.getLogger().warning("unable to find default conversation " + host.getConversation() + " of persistent host " + host.getId());
                }
                MemoryConfiguration config = new MemoryConfiguration();
                for (TPersistentHostOption option : host.getOptions()) {
                    config.set(option.getConfKey(), option.getConfValue());
                }
                loadedHost.load(config);
                cachedHosts.put(loadedHost.getUniqueId().toString(), loadedHost);
            }
            return conversationHost;
        }
        return Optional.empty();
    }

    @Override
    public Optional<ConversationHost<?>> getConversationHost(String id) {

        return Optional.ofNullable(cachedHosts.get(id));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<ConversationHost<T>> getConversationHost(T host) {

        for (ConversationHost<?> conversationHost : cachedHosts.values()) {
            if (conversationHost.getType().equals(host)) {
                return Optional.of((ConversationHost<T>) conversationHost);
            }
        }
        return Optional.empty();
    }

    @Override
    public <T> Optional<ConversationHost<T>> getOrCreateConversationHost(T host, ConfigurationSection config) {

        Optional<ConversationHost<T>> conversationHost = getConversationHost(host);
        if (conversationHost.isPresent()) return conversationHost;
        return createConversationHost(host, config);
    }

    @Override
    public void registerConversationVariable(Pattern pattern, ConversationVariable variable) {

        variables.put(pattern, variable);
    }

    @Override
    public Map<Pattern, ConversationVariable> getConversationVariables() {

        return variables;
    }

    public Set<String> getLoadedConversations() {
        return conversations.keySet();
    }

    @Override
    public Optional<ConversationTemplate> loadConversation(String identifier, ConfigurationSection config) {

        if (config.isList("worlds")) {
            List<String> worlds = config.getStringList("worlds");
            if (Bukkit.getWorlds().stream().map(World::getName).noneMatch(worlds::contains)) {
                plugin.getLogger().info("Not loading " + identifier + " because the required worlds are not loaded.");
                return Optional.empty();
            }
        }
        // TODO: refactor to directly loadConfig configured conversation template by using generics
        Optional<ConversationTemplate> template = createConversationTemplate(identifier, config);

        if (!template.isPresent()) {
            plugin.getLogger().warning("Could not find conversation template type " + config.getString("type") + " in " + ConfigUtil.getFileName(config));
            return Optional.empty();
        }

        template.get().loadConfig(config);

        return Optional.of(registerConversationTemplate(template.get()));
    }

    @Override
    public ConversationTemplate registerConversationTemplate(ConversationTemplate template) {
        conversations.put(template.getIdentifier(), template);
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
    public <TConversation extends Conversation> Optional<TConversation> startConversation(Player player, String conversation, Class<TConversation> conversationClass) {

        return startConversation(player, conversation).map(conversationClass::cast);
    }

    @Override
    public Optional<Conversation> startConversation(Player player, ConversationHost<?> conversationHost) {

        Optional<ConversationTemplate> conversation = conversationHost.getConversation(player);
        return conversation.map(conversationTemplate -> startConversation(player, conversationTemplate, conversationHost));
    }

    @Override
    public Optional<Conversation> startConversation(Player player, String conversation) {

        if (!conversations.containsKey(conversation)) {
            plugin.getLogger().warning("Tried to startStage unknown conversation " + conversation + " for " + player.getName());
            return Optional.empty();
        }
        ConversationTemplate template = conversations.get(conversation);
        return Optional.of(startConversation(player, template));
    }

    @Override
    public Conversation startConversation(Player player, ConversationTemplate template, ConversationHost<?> host) {
        return template.startConversation(player, host);
    }

    @Override
    public Conversation startConversation(Player player, ConversationTemplate template) {
        return template.startConversation(player, new PlayerHost(player));
    }

    @Override
    public Conversation startConversation(Player player) {
        return new CodedConversationTemplate(UUID.randomUUID().toString()).startConversation(player, new PlayerHost(player));
    }

    @Override
    public Optional<Conversation> getActiveConversation(Player player) {

        return Optional.ofNullable(activeConversations.get(player.getUniqueId()));
    }

    @Override
    public Optional<Conversation> setActiveConversation(Conversation conversation) {

        Optional<Conversation> activeConversation = removeActiveConversation(conversation.getOwner());
        activeConversations.put(conversation.getOwner().getUniqueId(), conversation);
        return activeConversation;
    }

    @Override
    public Optional<Conversation> removeActiveConversation(Player player) {

        return Optional.ofNullable(activeConversations.remove(player.getUniqueId()));
    }

    @Override
    public boolean hasActiveConversation(Player player) {

        return activeConversations.containsKey(player.getUniqueId());
    }

    @Override
    public Stage createStage(Conversation conversation, String text, Answer... answers) {

        return new DynamicStageTemplate(text, answers).create(conversation);
    }

    @Override
    public Answer createAnswer(String text, Action... actions) {

        SimpleAnswer answer = new SimpleAnswer(text);
        for (Action action : actions) {
            answer.addActionToAnswer(action);
        }
        return answer;
    }

    @Override
    public <T extends Answer> Optional<Answer> createAnswer(Class<T> answerClass, Action... actions) {

        for (Map.Entry<String, Constructor<? extends Answer>> entry : answerTemplates.entrySet()) {
            if (entry.getValue().getDeclaringClass().equals(answerClass)) {
                return getAnswer(entry.getKey(), new MemoryConfiguration());
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<ConversationHost<?>> spawnConversationHost(String pluginName, String name, String conversationName, Location location) {

        Optional<ConversationHost<?>> npc = createConversationHost(pluginName, UUID.randomUUID().toString(), "NPC", location);
        if (npc.isPresent()) {
            ConversationHost<?> host = npc.get();
            NPCHost conversationHost = (NPCHost) host;
            conversationHost.getType().setName(name);
            Optional<ConversationTemplate> template = getLoadedConversationTemplate(conversationName);
            if (template.isPresent()) {
                conversationHost.addDefaultConversation(template.get());
                conversationHost.load(template.get().getHostSettings());
                host.load(template.get().getHostSettings());
                cachedHosts.put(host.getUniqueId().toString(), host);
            } else {
                plugin.getLogger().warning("Cannot spawn conversation host npc without valid conversation: " + conversationName + " does not exist!");
                host.delete();
            }
            return npc;
        }
        return Optional.empty();
    }

    public void deleteConversationHost(ConversationHost host) {

        host.delete();
        TPersistentHost persistentHost = plugin.getDatabase().find(TPersistentHost.class).where().eq("host", host.getUniqueId()).findOne();
        if (persistentHost != null) {
            plugin.getDatabase().delete(persistentHost);
        }
        for (Map.Entry<String, ConversationHost<?>> entry : cachedHosts.entrySet()) {
            if (entry.getValue().equals(host)) {
                cachedHosts.remove(entry.getKey());
                break;
            }
        }
    }

    public List<ConversationHost> getNearbyHosts(Location location, int radius) {

        return cachedHosts.values().stream()
                .filter(host -> LocationUtil.isWithinRadius(location, host.getLocation(), radius))
                .collect(Collectors.toList());
    }
}
