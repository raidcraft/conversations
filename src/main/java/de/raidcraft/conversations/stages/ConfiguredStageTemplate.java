package de.raidcraft.conversations.stages;

import de.raidcraft.api.action.ActionAPI;
import de.raidcraft.api.conversations.Conversations;
import de.raidcraft.api.conversations.answer.Answer;
import de.raidcraft.api.conversations.conversation.ConversationTemplate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

/**
 * @author mdoering
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class ConfiguredStageTemplate extends AbstractStageTemplate {

    protected final ConfigurationSection config;

    public ConfiguredStageTemplate(String identifier, ConversationTemplate conversationTemplate, ConfigurationSection config) {

        super(identifier, conversationTemplate);
        this.config = config;
        setText(config.getString("text"));
        this.requirements = ActionAPI.createRequirements(getConversationTemplate().getIdentifier() + "." + identifier, config.getConfigurationSection("requirements"));
        this.actions = ActionAPI.createActions(config.getConfigurationSection("actions"));
        this.randomActions = ActionAPI.createActions(config.getConfigurationSection("random-actions"));
        this.answers = loadAnswers(config.getConfigurationSection("answers"));
        this.autoShowingAnswers = config.getBoolean("auto-show-answers", true);
        load(config.getConfigurationSection("args"));
    }

    protected abstract void load(ConfigurationSection args);

    protected List<Answer> loadAnswers(ConfigurationSection config) {

        return Conversations.createAnswers(this, config);
    }
}
