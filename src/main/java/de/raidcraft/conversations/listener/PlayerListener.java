package de.raidcraft.conversations.listener;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.conversations.conversation.Conversation;
import de.raidcraft.api.conversations.conversation.ConversationEndReason;
import de.raidcraft.conversations.RCConversationsPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Optional;

/**
 * @author Philip
 */
public class PlayerListener implements Listener {

    private final RCConversationsPlugin plugin;

    public PlayerListener(RCConversationsPlugin plugin) {

        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {

        Optional<Conversation<Player>> activeConversation = plugin.getConversationManager().getActiveConversation(event.getPlayer());
        if (activeConversation.isPresent()) {
            activeConversation.get().abort(ConversationEndReason.PLAYER_QUIT);
        }
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {

        Optional<Conversation<Player>> activeConversation = plugin.getConversationManager().getActiveConversation(event.getPlayer());
        if (activeConversation.isPresent()) {
            activeConversation.get().abort(ConversationEndReason.PLAYER_CHANGED_WORLD);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {

        if (!RaidCraft.hasMoved(event.getPlayer(), event.getTo())) {
            return;
        }

        plugin.getConversationManager().checkDistance(event.getPlayer());
    }

}
