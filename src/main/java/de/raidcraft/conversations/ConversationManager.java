package de.raidcraft.conversations;

import de.raidcraft.api.conversations.ConversationProvider;
import de.raidcraft.api.conversations.answer.Answer;
import de.raidcraft.api.conversations.conversation.ConversationTemplate;
import de.raidcraft.api.conversations.host.ConversationHost;
import de.raidcraft.api.conversations.stage.StageTemplate;
import de.raidcraft.util.CaseInsensitiveMap;
import de.raidcraft.util.ConfigUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Optional;

/**
 * @author mdoering
 */
public class ConversationManager implements ConversationProvider {

    private final RCConversationsPlugin plugin;
    private final Map<String, Constructor<? extends Answer>> answerTemplates = new CaseInsensitiveMap<>();
    private final Map<String, Constructor<? extends StageTemplate>> stageTemplates = new CaseInsensitiveMap<>();
    private final Map<String, ConversationTemplate> conversations = new CaseInsensitiveMap<>();

    public ConversationManager(RCConversationsPlugin plugin) {

        this.plugin = plugin;
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
    public void loadConversation(String identifier, ConfigurationSection config) {

        if (conversations.containsKey(identifier)) {
            plugin.getLogger().warning("Tried to register duplicate conversation: " + identifier + " from " + ConfigUtil.getFileName(config));
            return;
        }
        conversations.put(identifier, new ConfiguredConversationTemplate(identifier, config));
    }

    @Override
    public void triggerConversation(Player player, ConversationHost conversationHost) {
        //TODO: implement
    }
}
