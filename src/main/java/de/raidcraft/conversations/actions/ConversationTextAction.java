package de.raidcraft.conversations.actions;

import de.raidcraft.api.action.action.Action;
import de.raidcraft.api.conversations.conversation.Conversation;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author mdoering
 */
public class ConversationTextAction implements Action<Conversation> {

    @Override
    @Information(
            value = "text",
            desc = "Prints the given text to the player. Multiline splitting with |.",
            conf = "text"
    )
    public void accept(Conversation conversation, ConfigurationSection config) {

        conversation.sendMessage(config.getString("text").split("\\|"));
    }
}
