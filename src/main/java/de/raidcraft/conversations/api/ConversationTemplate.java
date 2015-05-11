package de.raidcraft.conversations.api;

import de.raidcraft.api.action.requirement.RequirementHolder;
import org.bukkit.entity.Player;

/**
 * @author mdoering
 */
public interface ConversationTemplate extends RequirementHolder<Player> {

    /**
     * Gets the unique identifier of the conversation.
     *
     * @return unique identifier
     */
    public String getIdentifier();

    /**
     * Gets the priority of the conversation. A higher priority is relevant for the chosen
     * default conversation if multiple exist in a host and all requirements match.
     *
     * @return conversation priority
     */
    public int getPriority();
}
