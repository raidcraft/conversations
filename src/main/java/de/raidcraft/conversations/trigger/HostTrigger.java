package de.raidcraft.conversations.trigger;

import de.raidcraft.api.action.trigger.Trigger;
import de.raidcraft.api.conversations.events.ConversationHostInteractEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * @author Silthus
 */
public class HostTrigger extends Trigger implements Listener {

    public HostTrigger() {

        super("host", "interact");
    }

    @Information(
            value = "host.interact",
            desc = "Is triggered when the player interacts with the given host.",
            conf = {
                    "host: <id>"
            }
    )
    @EventHandler(ignoreCancelled = true)
    public void onQuestHostInteract(ConversationHostInteractEvent event) {

        informListeners("interact", event.getPlayer(),
                config -> event.getHostIdentifier().equalsIgnoreCase(config.getString("host")));
    }
}
