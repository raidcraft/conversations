package de.raidcraft.conversations.api;

import lombok.NonNull;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

/**
 * @author mdoering
 */
public interface ConversationHost {

    /**
     * Gets the default conversation of this conversation host. If no default
     * conversation exists an empty {@link java.util.Optional} will be returned.
     * A host can have multiple default conversations and picks the one that will meet all
     * {@link de.raidcraft.api.action.requirement.Requirement}s. If more than one conversation
     * matches the requirements the one with the higher priority or order will be returned.
     *
     * @return optional default conversation
     */
    public Optional<ConversationTemplate> getDefaultConversation();

    /**
     * Gets a list of all possible default conversations.
     *
     * @return list of default conversations
     */
    public List<ConversationTemplate> getDefaultConversations();

    /**
     * Gets the saved {@link de.raidcraft.conversations.api.Conversation} for the player or the
     * default conversation if no saved Conversation exists.
     *
     * @param player to get {@link de.raidcraft.conversations.api.Conversation} for
     * @return optional saved conversation or default conversation
     */
    public Optional<Conversation> getConversation(@NonNull Player player);

    /**
     * Sets the given {@link de.raidcraft.conversations.api.Conversation} for the player. The
     * Conversation will persist until it is unset.
     *
     * @param player to set persistant conversation for
     * @param conversation to set
     */
    public void setConversation(@NonNull Player player, @NonNull ConversationTemplate conversation);

    /**
     * Sets the given {@link de.raidcraft.conversations.api.Conversation} for the player.
     * If persist is set to false the Conversation will be removed after it was finished once.
     *
     * @param player to set conversation for
     * @param conversation to set
     * @param persistant false if conversation should be unset after it completed once
     */
    public void setConversation(@NonNull Player player, @NonNull ConversationTemplate conversation, boolean persistant);

    /**
     * Unsets the current conversation for the player. Will set the conversation to the default conversation.
     *
     * @param player to unset conversation for
     * @return optional conversation if a conversation was removed
     */
    public Optional<ConversationTemplate> unsetConversation(@NonNull Player player);
}
