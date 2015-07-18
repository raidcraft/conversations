package de.raidcraft.conversations.listener;

import de.raidcraft.api.conversations.events.RCConversationAbortedEvent;
import de.raidcraft.api.conversations.events.RCConversationEndedEvent;
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

        switch (event.getReason()) {
            case CUSTOM:
            case SILENT:
            case ACTION:
            case START_NEW_CONVERSATION:
                return;
        }
        event.getPlayer().sendMessage(ChatColor.GRAY + "Unterhaltung abgebrochen: " + ChatColor.DARK_GRAY + event.getReason().getMessage());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onConversationAbort(RCConversationEndedEvent event) {

        switch (event.getReason()) {
            case CUSTOM:
            case SILENT:
            case ACTION:
            case START_NEW_CONVERSATION:
                return;
        }
        event.getPlayer().sendMessage(ChatColor.GRAY + "Unterhaltung beendet: " + ChatColor.DARK_GRAY + event.getReason().getMessage());
    }
}
