package de.raidcraft.conversations.actions;

import de.raidcraft.api.action.action.Action;
import de.raidcraft.api.conversations.conversation.Conversation;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author mdoering
 */
public class AbortConversationActions implements Action<Conversation> {

    @Override
    @Information(
            value = "abort-actions",
            desc = "Aborts the execution of conversation actions"
    )
    public void accept(Conversation conversation, ConfigurationSection config) {

        conversation.abortActionExection();
    }
}
