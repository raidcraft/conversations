package de.raidcraft.conversations.stages;

import de.raidcraft.api.action.ActionAPI;
import de.raidcraft.api.action.action.Action;
import de.raidcraft.api.conversations.Conversations;
import de.raidcraft.api.conversations.stage.AbstractStageTemplate;
import de.raidcraft.util.ConfigUtil;
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

    public ConfiguredStageTemplate(String identifier) {

        super(identifier);
    }

    public void loadConfig(ConfigurationSection config) {
        setText(config.getString("text"));
        setAutoShowingAnswers(config.getBoolean("auto-show-answers", true));

        this.loadRequirements(config.getConfigurationSection("requirements"));
        this.loadActions(getActions(), config.getConfigurationSection("actions"));
        this.loadActions(getRandomActions(), config.getConfigurationSection("random-actions"));
        this.loadAnswers(config.getConfigurationSection("answers"));
    }

    private void loadRequirements(ConfigurationSection config) {
        getRequirements().clear();
        if (config == null) return;
        getRequirements().addAll(ActionAPI.createRequirements(
                getConversationTemplate().map(template -> template.getIdentifier()).orElse(ConfigUtil.getFileName(config)) + "." + getIdentifier(),
                config)
        );
    }

    private void loadActions(List<Action<?>> actions, ConfigurationSection config) {
        actions.clear();
        if (config == null) return;
        actions.addAll(ActionAPI.createActions(config));
    }

    protected void loadAnswers(ConfigurationSection config) {
        getAnswers().clear();
        if (config == null) return;
        getAnswers().addAll(Conversations.createAnswers(this, config));
    }
}
