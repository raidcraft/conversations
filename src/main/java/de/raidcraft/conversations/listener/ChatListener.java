package de.raidcraft.conversations.listener;

import de.raidcraft.api.conversations.answer.Answer;
import de.raidcraft.api.conversations.conversation.Conversation;
import de.raidcraft.api.conversations.conversation.ConversationEndReason;
import de.raidcraft.api.conversations.stage.Stage;
import de.raidcraft.conversations.ConversationManager;
import de.raidcraft.conversations.RCConversationsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Optional;

/**
 * @author Philip
 */
public class ChatListener implements Listener {

    private final RCConversationsPlugin plugin;

    public ChatListener(RCConversationsPlugin plugin) {

        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {

        ConversationManager manager = plugin.getConversationManager();
        Optional<Conversation> activeConversation = manager.getActiveConversation(event.getPlayer());
        if (!activeConversation.isPresent()) {
            return;
        }

        String[] exitWords = plugin.getConfiguration().exitWords;
        Conversation conversation = activeConversation.get();

        for (String exitWord : exitWords) {
            if (exitWord.equalsIgnoreCase(event.getMessage())) {
                conversation.abort(ConversationEndReason.PLAYER_ABORT);
                event.setCancelled(true);
                return;
            }
        }

        Optional<Stage> currentStage = conversation.getCurrentStage();
        if (!currentStage.isPresent()) {
            event.getPlayer().sendMessage("Du befindest dich in keiner gültigen Stage und kannst aktuell nicht antworten!");
            event.setCancelled(true);
            return;
        }

        // trigger conversation and hide chat message
        Optional<Answer> answer = conversation.answer(currentStage.get(), event.getMessage());
        if (answer.isPresent()) {
            event.setCancelled(true);
        }
    }

}
