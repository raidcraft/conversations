package de.raidcraft.conversations.actions;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.action.action.Action;
import de.raidcraft.api.conversations.Conversations;
import de.raidcraft.api.conversations.conversation.ConversationTemplate;
import de.raidcraft.api.conversations.host.ConversationHost;
import de.raidcraft.util.ConfigUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Optional;

/**
 * @author mdoering
 */
public class StartConversationAction implements Action<Player> {

    @Override
    @Information(
            value = "conversation.start",
            desc = "Starts the given conversation with the given host.",
            conf = {
                    "host: host to start conversation with",
                    "conv: conversation to start"
            }
    )
    public void accept(Player player, ConfigurationSection config) {

        Optional<ConversationTemplate> template = Conversations.getConversationTemplate(config.getString("conv"));
        if (!template.isPresent()) {
            RaidCraft.LOGGER.warning("Invalid Conversation Template with id "
                    + config.getString("conv") + " defined in " + ConfigUtil.getFileName(config));
            return;
        }
        Optional<ConversationHost<?>> host = Conversations.getConversationHost(config.getString("host"));
        if (!host.isPresent()) {
            RaidCraft.LOGGER.warning("Invalid Conversation Host with id "
                    + config.getString("host") + " defined in " + ConfigUtil.getFileName(config));
            return;
        }
        template.get().startConversation(player, host.get());
    }
}
