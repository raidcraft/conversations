package de.raidcraft.conversations.trigger;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.action.trigger.Trigger;
import de.raidcraft.api.conversations.events.ConversationHostInteractEvent;
import de.raidcraft.api.conversations.events.ConversationHostProximityEvent;
import de.raidcraft.conversations.ConversationManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * @author Silthus
 */
public class HostTrigger extends Trigger implements Listener {

    private final ConversationManager conversationManager;

    public HostTrigger() {

        super("host", "interact", "proximity");
        this.conversationManager = RaidCraft.getComponent(ConversationManager.class);
    }

    @Information(
            value = "host.interact",
            desc = "Is triggered when the player interacts with the given host.",
            conf = {
                    "host: <id>",
                    "conv: optional conversation to start"
            }
    )
    @EventHandler(ignoreCancelled = true)
    public void onQuestHostInteract(ConversationHostInteractEvent event) {

        informListeners("interact", event.getPlayer(),
                config ->{
                    if (event.getHostIdentifier().equalsIgnoreCase(config.getString("host"))) {
                        if (config.isSet("conv")) {
                            event.getQuestHost().startConversation(event.getPlayer(), config.getString("conv"));
                        }
                        event.setCancelled(true);
                        return true;
                    }
                    return false;
                });
    }

    @Information(
            value = "host.proximity",
            desc = "Is triggered when the player walks by a host.",
            conf = {
                    "host: <id>",
                    "radius: [3]"
            }
    )
    @EventHandler(ignoreCancelled = true)
    public void onHostProximity(ConversationHostProximityEvent event) {

        informListeners("proximity", event.getPlayer(),
                config -> event.getHostIdentifier().equalsIgnoreCase(config.getString("host"))
                        && event.getRadius() <= config.getInt("radius", 3));
    }
}
