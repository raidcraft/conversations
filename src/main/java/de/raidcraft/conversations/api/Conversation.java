package de.raidcraft.conversations.api;

import org.bukkit.entity.Player;

import java.util.List;

/**
 * @author mdoering
 */
public interface Conversation {

    /**
     * Gets the unique identifier of the conversation.
     *
     * @return unique identifier
     */
    public String getIdentifier();

    /**
     * Gets the player involved in this conversation.
     *
     * @return player involved in the conversation
     */
    public Player getPlayer();

    /**
     * Gets the host that started the conversation.
     *
     * @return conversation host
     */
    public ConversationHost getHost();

    /**
     * Gets the {@link de.raidcraft.conversations.api.ConversationTemplate} of this Conversation.
     *
     * @return conversation template
     */
    public ConversationTemplate getTemplate();

    /**
     * Gets the current active stage of the conversation.
     *
     * @return current stage
     */
    public Stage getCurrentStage();

    /**
     * Sets the current stage to the given stage.
     *
     * @param stage to set
     * @return the set stage
     */
    public Stage setCurrentStage(Stage stage);

    /**
     * Gets a list of all stages attached to this conversation.
     *
     * @return list of stages
     */
    public List<Stage> getStages();

    /**
     * Adds the given stage to the conversation.
     *
     * @param stage to add
     * @return this conversation
     */
    public Conversation addStage(Stage stage);
}
