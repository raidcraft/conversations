package de.raidcraft.conversations.commands;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.NestedCommand;
import de.raidcraft.api.conversations.answer.Answer;
import de.raidcraft.api.conversations.conversation.Conversation;
import de.raidcraft.conversations.RCConversationsPlugin;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

/**
 * @author mdoering
 */
public class ConversationCommands {

    private final RCConversationsPlugin plugin;

    public ConversationCommands(RCConversationsPlugin plugin) {

        this.plugin = plugin;
    }

    @Command(
            aliases = {"conversations", "conv", "rcc"},
            desc = "Base command for RCConversations."
    )
    @NestedCommand(NestedConversationCommands.class)
    public void convBaseCommand(CommandContext args, CommandSender sender) {


    }

    @Command(
            aliases = {"rcaa"},
            desc = "Base command for RCConversations Admin Actions."
    )
    @NestedCommand(NestedConversationCommands.class)
    public void convAdminBaseCommand(CommandContext args, CommandSender sender) {


    }

    public static class NestedConversationCommands {

        private final RCConversationsPlugin plugin;

        public NestedConversationCommands(RCConversationsPlugin plugin) {

            this.plugin = plugin;
        }

        private Conversation<Player> getActiveConversation(CommandSender sender) throws CommandException {

            if (!(sender instanceof Player)) {
                throw new CommandException("Conversation Holder must be a Player.");
            }
            Optional<Conversation<Player>> optional = plugin.getConversationManager().getActiveConversation((Player) sender);
            if (!optional.isPresent()) {
                throw new CommandException("Du musst erst eine Unterhaltung beginnen bevor du darauf antworten kannst.");
            }
            return optional.get();
        }

        @Command(
                aliases = {"answer"},
                desc = "Answers to the current conversation."
        )
        public void answer(CommandContext args, CommandSender sender) throws CommandException {

            Conversation<Player> conversation = getActiveConversation(sender);
            Optional<Answer> answer = conversation.answer(args.getJoinedStrings(0));
            if (!answer.isPresent()) {
                throw new CommandException("Keine gültige Antwort für " + args.getJoinedStrings(0) + " gefunden.");
            }
        }

        @Command(
                aliases = {"page"},
                desc = "Browses to the next page of the conversation stage."
        )
        public void page(CommandContext args, CommandSender sender) throws CommandException {

            Conversation<Player> conversation = getActiveConversation(sender);
            if (!conversation.changePage(args.getInteger(0))) {
                throw new CommandException("Konnte nicht zu Seite " + args.getInteger(0) + " wechseln.");
            }
        }
    }

    public static class NestedConversationAdminCommands {

        private final RCConversationsPlugin plugin;

        public NestedConversationAdminCommands(RCConversationsPlugin plugin) {

            this.plugin = plugin;
        }

        @Command(
                aliases = {"reload"},
                desc = "Reloads the Conversations Plugin."
        )
        @CommandPermissions("rcconversations.cmd.reload")
        public void reload(CommandContext args, CommandSender sender) {

            plugin.reload();
            sender.sendMessage(ChatColor.GREEN + "Reloaded the RCConversations plugin successfully!");
        }
    }
}
