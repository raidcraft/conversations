package de.raidcraft.conversations.conversations;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.action.ActionAPI;
import de.raidcraft.api.action.action.Action;
import de.raidcraft.api.action.requirement.Requirement;
import de.raidcraft.api.conversations.Conversations;
import de.raidcraft.api.conversations.conversation.Conversation;
import de.raidcraft.api.conversations.conversation.ConversationEndReason;
import de.raidcraft.api.conversations.conversation.ConversationTemplate;
import de.raidcraft.api.conversations.host.ConversationHost;
import de.raidcraft.api.conversations.stage.StageTemplate;
import de.raidcraft.util.CaseInsensitiveMap;
import de.raidcraft.util.ConfigUtil;
import lombok.Data;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author mdoering
 */
@Data
public abstract class ConfiguredConversationTemplate implements ConversationTemplate {

    private final String identifier;
    private final String conversationType;
    private final boolean persistant;
    private final int priority;
    private final ConfigurationSection hostSettings;
    private final List<Requirement<?>> requirements;
    private final List<Action<?>> actions;
    private final Map<String, StageTemplate> stages;

    public ConfiguredConversationTemplate(String identifier, ConfigurationSection config) {

        this.identifier = identifier;
        this.conversationType = config.getString("conv-type", Conversation.DEFAULT_TYPE);
        this.persistant = config.getBoolean("persistant", false);
        this.priority = config.getInt("priority", 1);
        this.hostSettings = config.isConfigurationSection("settings") ? config.getConfigurationSection("settings") : new MemoryConfiguration();
        this.requirements = ActionAPI.createRequirements(identifier, config.getConfigurationSection("requirements"));
        this.actions = ActionAPI.createActions(config.getConfigurationSection("actions"));
        this.stages = loadStages(config.getConfigurationSection("stages"));
        load(config.getConfigurationSection("args"));
    }

    protected abstract void load(ConfigurationSection args);

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
    public Map<String, StageTemplate> getStages() {

        return new CaseInsensitiveMap<>(stages);
    }

    @Override
    public Optional<StageTemplate> getStage(String name) {

        return Optional.ofNullable(stages.get(name));
    }

    @Override
    public Conversation createConversation(Player player, ConversationHost host) {

        return Conversations.createConversation(getConversationType(), player, this, host);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Conversation startConversation(Player player, ConversationHost host) {

        Optional<Conversation> activeConversation = Conversations.removeActiveConversation(player);
        if (activeConversation.isPresent()) {
            if (!activeConversation.get().getTemplate().equals(this)) {
                activeConversation.get().abort(ConversationEndReason.START_NEW_CONVERSATION);
            } else {
                Conversations.setActiveConversation(activeConversation.get());
                return activeConversation.get();
            }
        }

        // lets execute all actions of this conversation
        Conversation conversation = createConversation(player, host);
        for (Action<?> action : getActions()) {
            if (conversation.isAbortActionExecution()) break;
            if (ActionAPI.matchesType(action, Player.class)) {
                ((Action<Player>) action).accept(player);
            } else if (ActionAPI.matchesType(action, Conversation.class)) {
                ((Action<Conversation>) action).accept(conversation);
            }
        }

        conversation.start();
        return conversation;
    }

    @Override
    public int compareTo(ConversationTemplate o) {

        return Integer.compare(getPriority(), o.getPriority());
    }
}
