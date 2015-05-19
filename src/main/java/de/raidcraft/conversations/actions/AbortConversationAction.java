package de.raidcraft.conversations.actions;

import de.raidcraft.api.action.action.Action;
import de.raidcraft.api.conversations.conversation.Conversation;
import de.raidcraft.api.conversations.conversation.ConversationEndReason;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author mdoering
 */
public class AbortConversationAction implements Action<Conversation> {

    @Override
    @Information(
            value = "abort",
            desc = "Aborts the current conversation with the given or custom reason.",
            conf = "reason: ConversationEndReason or Custom Message"
    )
    public void accept(Conversation conversation, ConfigurationSection config) {

        String reason = config.getString("reason");
        ConversationEndReason endReason = ConversationEndReason.fromString(reason);
        if (endReason == null) {
            endReason = ConversationEndReason.CUSTOM;
            endReason.setMessage(reason);
        }
        conversation.abort(endReason);
    }
}
