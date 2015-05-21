package de.raidcraft.conversations;

import de.raidcraft.api.config.SimpleConfiguration;
import de.raidcraft.api.conversations.ConversationProvider;
import de.raidcraft.api.conversations.Conversations;
import de.raidcraft.api.conversations.answer.Answer;
import de.raidcraft.api.conversations.conversation.Conversation;
import de.raidcraft.api.conversations.conversation.ConversationEndReason;
import de.raidcraft.api.conversations.conversation.ConversationTemplate;
import de.raidcraft.api.conversations.host.ConversationHost;
import de.raidcraft.api.conversations.stage.StageTemplate;
import de.raidcraft.conversations.answers.DefaultAnswer;
import de.raidcraft.conversations.answers.SimpleAnswer;
import de.raidcraft.conversations.conversations.DefaultConversationTemplate;
import de.raidcraft.conversations.stages.DefaultStageTemplate;
import de.raidcraft.util.CaseInsensitiveMap;
import de.raidcraft.util.ConfigUtil;
import mkremins.fanciful.FancyMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * @author mdoering
 */
public class ConversationManager implements ConversationProvider {

    private final RCConversationsPlugin plugin;
    private final Map<String, Constructor<? extends Answer>> answerTemplates = new CaseInsensitiveMap<>();
    private final Map<String, Constructor<? extends StageTemplate>> stageTemplates = new CaseInsensitiveMap<>();
    private final Map<String, Constructor<? extends ConversationTemplate>> conversationTemplates = new CaseInsensitiveMap<>();
    private final Map<String, ConversationTemplate> conversations = new CaseInsensitiveMap<>();
    private final Map<UUID, Conversation<Player>> activeConversations = new HashMap<>();

    public ConversationManager(RCConversationsPlugin plugin) {

        this.plugin = plugin;
        Conversations.enable(this);
        registerConversationTemplate(ConversationTemplate.DEFAULT_CONVERSATION_TEMPLATE, DefaultConversationTemplate.class);
        registerStage(StageTemplate.DEFAULT_STAGE_TEMPLATE, DefaultStageTemplate.class);
        registerAnswer(Answer.DEFAULT_ANSWER_TEMPLATE, DefaultAnswer.class);
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

    @Override
    public void registerAnswer(String type, Class<? extends Answer> answer) {

        try {
            Constructor<? extends Answer> constructor = answer.getDeclaredConstructor(ConfigurationSection.class);
            constructor.setAccessible(true);
            answerTemplates.put(type, constructor);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Optional<Answer> getAnswer(StageTemplate stageTemplate, ConfigurationSection config) {

        Constructor<? extends Answer> constructor;
        if (config.isSet("type")) {
            constructor = answerTemplates.get(config.getString("type"));
        } else {
            constructor = answerTemplates.get(Answer.DEFAULT_ANSWER_TEMPLATE);
        }
        if (constructor != null) {
            try {
                return Optional.of(constructor.newInstance(config));
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
    public Optional<ConversationTemplate> getConversationTemplate(String identifier, ConfigurationSection config) {

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
    public Optional<ConversationTemplate> loadConversation(String identifier, ConfigurationSection config) {

        if (conversations.containsKey(identifier)) {
            plugin.getLogger().warning("Tried to register duplicate conversation: " + identifier + " from " + ConfigUtil.getFileName(config));
            return Optional.of(conversations.get(identifier));
        }
        Optional<ConversationTemplate> template = getConversationTemplate(identifier, config);
        if (!template.isPresent()) {
            plugin.getLogger().warning("Could not find conversation template type " + config.getString("type") + " in " + ConfigUtil.getFileName(config));
            return Optional.empty();
        }
        conversations.put(identifier, template.get());
        return template;
    }

    @Override
    public Optional<Conversation<Player>> startConversation(Player player, ConversationHost conversationHost) {

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
