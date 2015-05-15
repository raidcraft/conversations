package de.raidcraft.conversations;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.action.ActionAPI;
import de.raidcraft.api.action.requirement.Requirement;
import de.raidcraft.api.conversations.conversation.Conversation;
import de.raidcraft.api.conversations.host.ConversationHost;
import de.raidcraft.api.conversations.conversation.ConversationTemplate;
import de.raidcraft.api.conversations.Conversations;
import de.raidcraft.api.conversations.stage.StageTemplate;
import de.raidcraft.util.CaseInsensitiveMap;
import de.raidcraft.util.ConfigUtil;
import lombok.Data;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author mdoering
 */
@Data
public class ConfiguredConversationTemplate implements ConversationTemplate {

    private final String identifier;
    private final boolean persistant;
    private final int priority;
    private final List<Requirement<?>> requirements;
    private final Map<String, StageTemplate> stages;

    public ConfiguredConversationTemplate(String identifier, ConfigurationSection config) {

        this.identifier = identifier;
        this.persistant = config.getBoolean("persistant", false);
        this.priority = config.getInt("priority", 1);
        this.requirements = ActionAPI.createRequirements(identifier, config.getConfigurationSection("requirements"));
        this.stages = loadStages(config.getConfigurationSection("stages"));
    }

    private Map<String, StageTemplate> loadStages(ConfigurationSection config) {

        Map<String, StageTemplate> stages = new CaseInsensitiveMap<>();
        if (config == null) return stages;
        for (String key : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(key);
            Optional<StageTemplate> stageTemplate = Conversations.getStageTemplate(key, this, section);
            if (stageTemplate.isPresent()) {
                stages.put(key, stageTemplate.get());
            } else {
                RaidCraft.LOGGER.warning("Unknown stage type " + section.getString("type") + " defined in " + ConfigUtil.getFileName(config));
            }
        }
        return stages;
    }

    @Override
    public Optional<StageTemplate> getStage(String name) {

        return Optional.ofNullable(stages.get(name));
    }

    @Override
    public Conversation<Player> createConversation(Player player, ConversationHost host) {

        return new PlayerConversation(player, this, host);
    }
}
