package de.raidcraft.conversations.actions;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.action.action.Action;
import de.raidcraft.api.conversations.Conversations;
import de.raidcraft.api.conversations.conversation.ConversationTemplate;
import de.raidcraft.api.conversations.host.ConversationHost;
import de.raidcraft.api.conversations.stage.StageTemplate;
import de.raidcraft.conversations.hosts.PlayerHost;
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
                    "conv: <conv id>",
                    "host: [optional host]",
                    "stage: [optional stage to start at]"
            }
    )
    public void accept(Player player, ConfigurationSection config) {

        Optional<ConversationTemplate> template = Conversations.getConversationTemplate(config.getString("conv"));
        if (!template.isPresent()) {
            RaidCraft.LOGGER.warning("Invalid Conversation Template with id "
                    + config.getString("conv") + " defined in " + ConfigUtil.getFileName(config));
            return;
        }
        ConversationHost<?> host;
        if (config.isSet("host")) {
            Optional<ConversationHost<?>> conversationHost = Conversations.getConversationHost(config.getString("host"));
            if (!conversationHost.isPresent()) {
                RaidCraft.LOGGER.warning("Invalid Conversation Host with id "
                        + config.getString("host") + " defined in " + ConfigUtil.getFileName(config));
                return;
            }
            host = conversationHost.get();
        } else {
            host = new PlayerHost(player);
        }
        if (config.isSet("stage")) {
            Optional<StageTemplate> stage = template.get().getStage(config.getString("stage"));
            if (!stage.isPresent()) {
                RaidCraft.LOGGER.warning("Invalid Stage " + config.getString("stage") + "defined in " + template.get().getIdentifier());
                return;
            }
            template.get().startConversation(player, host, stage.get());
        } else {
            template.get().startConversation(player, host);
        }
    }
}
