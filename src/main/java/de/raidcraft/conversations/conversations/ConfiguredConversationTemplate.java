package de.raidcraft.conversations.conversations;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.action.ActionAPI;
import de.raidcraft.api.conversations.Conversations;
import de.raidcraft.api.conversations.conversation.AbstractConversationTemplate;
import de.raidcraft.api.conversations.conversation.Conversation;
import de.raidcraft.api.conversations.stage.StageTemplate;
import de.raidcraft.util.ConfigUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Optional;

/**
 * @author mdoering
 */
@Data
@EqualsAndHashCode(callSuper = true, of = {"identifier"})
public class ConfiguredConversationTemplate extends AbstractConversationTemplate {

    public ConfiguredConversationTemplate(String identifier, ConfigurationSection config) {
        super(identifier);
    }

    public void loadConfig(ConfigurationSection config) {
        this.setConversationType(config.getString("conv-type", Conversation.DEFAULT_TYPE));
        this.setExitable(!config.getBoolean("block-end", false));
        this.setBlockingConversationStart(config.getBoolean("block-conv-startStage", false));
        this.setEndingOutOfRange(config.getBoolean("end-out-of-range", this.isExitable()));
        this.setPersistent(config.getBoolean("persistent", false));
        this.setPriority(config.getInt("priority", 1));
        this.setAutoEnding(config.getBoolean("auto-end", false));
        this.setHostSettings(config.getConfigurationSection("host-settings"));

        this.loadRequirements(config.getConfigurationSection("requirements"));
        this.loadActions(config.getConfigurationSection("actions"));
        this.loadStages(config.getConfigurationSection("stages"));
    }

    private void loadRequirements(ConfigurationSection config) {
        getRequirements().clear();
        if (config != null) {
            getRequirements().addAll(ActionAPI.createRequirements(getIdentifier(), config));
        }
    }

    private void loadActions(ConfigurationSection config) {
        getActions().clear();
        if (config != null) {
            getActions().addAll(ActionAPI.createActions(config));
        }
    }

    private void loadStages(ConfigurationSection config) {
        getStages().clear();

        if (config == null) return;

        for (String key : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(key);
            Optional<StageTemplate> stageTemplate = Conversations.getStageTemplate(key, this, section);
            if (stageTemplate.isPresent()) {
                getStages().put(key, stageTemplate.get());
            } else {
                RaidCraft.LOGGER.warning("Unknown stage type " + section.getString("type") + " defined in " + ConfigUtil.getFileName(config));
            }
        }
    }
}
