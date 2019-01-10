package de.raidcraft.conversations.actions;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.action.action.Action;
import de.raidcraft.api.conversations.Conversations;
import de.raidcraft.api.conversations.conversation.Conversation;
import de.raidcraft.api.conversations.host.ConversationHost;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Optional;

public class NpcEmoteAction implements Action<Player> {

    @Override
    @Information(
            value = "npc.emote",
            desc = "Prints a NPC emote into the chat.",
            conf = {
                    "text: <text to print as emote>",
                    "npc: [name of the npc]"
            }
    )
    public void accept(Player player, ConfigurationSection config) {

        Optional<Conversation> activeConversation = Conversations.getActiveConversation(player);
        String npc;
        if (activeConversation.isPresent()) {
            Optional<String> name = activeConversation.get().getHost().getName();
            npc = name.orElse(config.getString("npc"));
        } else {
            npc = Conversations.getConversationHost(config.getString("npc"))
                    .map(ConversationHost::getName)
                    .map(name -> name.orElse(null))
                    .orElse(config.getString("npc"));
        }

        String[] text = config.getString("text").split("\\|");
        for (String line : text) {
            String message;
            if (npc != null) {
                message = ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + npc + ChatColor.DARK_GRAY + "]"
                        + ChatColor.GOLD + ": " + ChatColor.GRAY + line;
            } else {
                message = ChatColor.GRAY + line;
            }
            if (activeConversation.isPresent()) {
                activeConversation.get().sendMessage(message);
            } else {
                player.sendMessage(RaidCraft.replaceVariables(player, message));
            }
        }
    }
}
