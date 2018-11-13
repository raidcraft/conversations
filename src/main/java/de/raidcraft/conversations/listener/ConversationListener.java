package de.raidcraft.conversations.listener;

import de.raidcraft.api.conversations.events.ConversationHostInteractEvent;
import de.raidcraft.api.conversations.events.ConversationHostProximityEvent;
import de.raidcraft.api.conversations.events.RCConversationAbortedEvent;
import de.raidcraft.api.conversations.events.RCConversationChangedStageEvent;
import de.raidcraft.api.conversations.events.RCConversationEndedEvent;
import de.raidcraft.api.conversations.events.RCConversationStageTriggeredEvent;
import de.raidcraft.api.conversations.events.RCConversationStartEvent;
import de.raidcraft.conversations.RCConversationsPlugin;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * @author Silthus
 */
public class ConversationListener implements Listener {

    private final RCConversationsPlugin plugin;

    public ConversationListener(RCConversationsPlugin plugin) {

        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onConversationAbort(RCConversationAbortedEvent event) {

        if (event.getReason().isSilent()) return;
        event.getPlayer().sendMessage(ChatColor.GRAY + "Unterhaltung abgebrochen: " + ChatColor.DARK_GRAY + event.getReason().getMessage());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onConversationEnd(RCConversationEndedEvent event) {

        if (event.getReason().isSilent()) return;
        event.getPlayer().sendMessage(ChatColor.DARK_GRAY + event.getReason().getMessage());
    }
    
    /*
    Do some debugging if defined
     */

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void convStart(RCConversationStartEvent event) {

        if (!plugin.getConfiguration().debug_start) return;
        plugin.getLogger().info("Conversation " + event.getConversation().getIdentifier()
                + " STARTED for "
                + event.getPlayer().getName()
                + " with host " + event.getHost().getName());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void convEnd(RCConversationEndedEvent event) {

        if (!plugin.getConfiguration().debug_end) return;
        plugin.getLogger().info("Conversation " + event.getConversation().getIdentifier()
                + " ENDED for "
                + event.getPlayer().getName()
                + " with host " + event.getHost().getName()
                + " and reason " + event.getReason().name());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void convAbort(RCConversationAbortedEvent event) {

        if (!plugin.getConfiguration().debug_abort) return;
        plugin.getLogger().info("Conversation " + event.getConversation().getIdentifier()
                + " ABORTED for "
                + event.getPlayer().getName()
                + " with host " + event.getHost().getName()
                + " and reason " + event.getReason().name());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void stageTriggered(RCConversationStageTriggeredEvent event) {

        if (!plugin.getConfiguration().debug_stage_trigger) return;
        plugin.getLogger().info("Conversation " + event.getConversation().getIdentifier()
                + " TRIGGERED STAGE "
                + event.getStage().getIdentifier()
                + " for " + event.getPlayer().getName()
                + " with host "
                + event.getHost().getName());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void stageChanged(RCConversationChangedStageEvent event) {

        if (!plugin.getConfiguration().debug_stage_change) return;
        plugin.getLogger().info("Conversation " + event.getConversation().getIdentifier()
                + " CHANGED STAGE"
                + " from " + (event.getOldStage().isPresent() ? event.getOldStage().get().getIdentifier() : "N/A")
                + " to " + event.getNewStage().getIdentifier()
                + " for " + event.getPlayer().getName()
                + " with host " + event.getHost().getName());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void hostProximity(ConversationHostProximityEvent event) {

        if (!plugin.getConfiguration().debug_proximity) return;
        plugin.getLogger().info("Host PROXIMITY FIRED"
                + " for " + event.getHostIdentifier()
                + " by " + event.getPlayer().getName()
                + " with radius " + event.getRadius()
        );
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void hostProximity(ConversationHostInteractEvent event) {

        if (!plugin.getConfiguration().debug_interact) return;
        plugin.getLogger().info("Host INTERACT FIRED"
                        + " for " + event.getHostIdentifier()
                        + " by " + event.getPlayer().getName());
    }
}
