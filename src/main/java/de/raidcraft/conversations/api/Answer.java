package de.raidcraft.conversations.api;

import de.raidcraft.api.action.action.ActionHolder;
import org.bukkit.entity.Player;

/**
 * @author mdoering
 */
public interface Answer extends ActionHolder<Player> {

    /**
     * Gets the text displayed as answer.
     *
     * @return displayed answer
     */
    public String getText();

    /**
     * Executes all actions of this answer for the given conversation.
     *
     * @param conversation to execute actions for
     */
    public void executeActions(Conversation conversation);
}
