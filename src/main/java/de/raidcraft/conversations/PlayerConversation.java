package de.raidcraft.conversations;

import de.raidcraft.api.conversations.conversation.AbstractConversation;
import de.raidcraft.api.conversations.conversation.Conversation;
import de.raidcraft.api.conversations.host.ConversationHost;
import de.raidcraft.api.conversations.conversation.ConversationTemplate;
import mkremins.fanciful.FancyMessage;
import org.bukkit.entity.Player;

/**
 * @author mdoering
 */
public class PlayerConversation extends AbstractConversation<Player> {

    public PlayerConversation(Player player, ConversationTemplate conversationTemplate, ConversationHost conversationHost) {

        super(player, conversationTemplate, conversationHost);
    }

    @Override
    public Conversation<Player> sendMessage(String... lines) {

        for (String line : lines) {
            getEntity().sendMessage(line);
        }
        return this;
    }

    @Override
    public Conversation<Player> sendMessage(FancyMessage... lines) {

        for (FancyMessage line : lines) {
            line.send(getEntity());
        }
        return this;
    }
}
