package de.raidcraft.conversations.stages;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.action.ActionAPI;
import de.raidcraft.api.action.action.Action;
import de.raidcraft.api.action.requirement.Requirement;
import de.raidcraft.api.conversations.Conversations;
import de.raidcraft.api.conversations.answer.Answer;
import de.raidcraft.api.conversations.conversation.Conversation;
import de.raidcraft.api.conversations.conversation.ConversationTemplate;
import de.raidcraft.api.conversations.stage.Stage;
import de.raidcraft.api.conversations.stage.StageTemplate;
import de.raidcraft.util.ConfigUtil;
import lombok.Data;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author mdoering
 */
@Data
public abstract class ConfiguredStageTemplate implements StageTemplate {

    private final String identifier;
    private final ConversationTemplate conversationTemplate;
    private final Optional<String[]> text;
    private final List<Requirement<?>> requirements;
    private final List<Action<?>> actions;
    private final List<Answer> answers;
    protected final ConfigurationSection config;

    public ConfiguredStageTemplate(String identifier, ConversationTemplate conversationTemplate, ConfigurationSection config) {

        this.identifier = identifier;
        this.conversationTemplate = conversationTemplate;
        this.config = config;
        this.text = config.getString("text") == null ? Optional.empty() : Optional.of(config.getString("text").split("\\|"));
        this.requirements = ActionAPI.createRequirements(getConversationTemplate().getIdentifier() + "." + identifier, config.getConfigurationSection("requirements"));
        this.actions = ActionAPI.createActions(config.getConfigurationSection("actions"));
        this.answers = loadAnswers(config.getConfigurationSection("answers"));
        load(config.getConfigurationSection("args"));
    }

    protected abstract void load(ConfigurationSection args);

    protected List<Answer> loadAnswers(ConfigurationSection config) {

        List<Answer> answers = new ArrayList<>();
        if (config == null) return answers;
        for (String key : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(key);
            Optional<Answer> answer = Conversations.getAnswer(this, section);
            if (answer.isPresent()) {
                answers.add(answer.get());
            } else {
                RaidCraft.LOGGER.warning("Unknown answer type " + section.getString("type") + " in " + ConfigUtil.getFileName(config));
            }
        }
        return answers;
    }

    @Override
    public Stage create(Conversation conversation) {

        return new SimpleStage(conversation, this);
    }
}
