package de.raidcraft.conversations.commands;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.NestedCommand;
import de.raidcraft.api.conversations.answer.Answer;
import de.raidcraft.api.conversations.conversation.Conversation;
import de.raidcraft.api.conversations.conversation.ConversationTemplate;
import de.raidcraft.conversations.RCConversationsPlugin;
import de.raidcraft.conversations.npc.NPCEdit;
import de.raidcraft.conversations.npc.NPCEditSettings;
import de.raidcraft.conversations.npc.NPC_Conservations_Manager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    @NestedCommand(NestedConversationAdminCommands.class)
    public void convAdminBaseCommand(CommandContext args, CommandSender sender) {


    }

    public static class NestedConversationCommands {

        private final RCConversationsPlugin plugin;

        public NestedConversationCommands(RCConversationsPlugin plugin) {

            this.plugin = plugin;
        }

        @Command(
                aliases = {"create"},
                desc = "Create conversation NPC",
                usage = "<conversation> <npc name>",
                min = 1,
                flags = "n"
        )
        @CommandPermissions("rcconversations.npc.create")
        public void createNPC(CommandContext context, CommandSender sender) throws CommandException {

            Player player = (Player) sender;
            String conversationName = context.getString(0);
            String npcName = null;
            if (context.argsLength() > 1) {
                npcName = context.getJoinedStrings(1);
            }

            ConversationTemplate template = findTemplate(conversationName);
            ConfigurationSection settings = template.getHostSettings();
            npcName = settings.getString("npc-name", npcName);
            settings.set("talk-nearby", context.hasFlag('n'));
            if (npcName == null) {
                throw new CommandException("Für diese Conversation muss ein NPC-Name mitgegeben werden!");
            }

            NPC_Conservations_Manager.getInstance().spawnPersistNpcConservations(player.getLocation(), npcName, plugin.getName(), conversationName);

            sender.sendMessage(org.bukkit.ChatColor.GREEN + "Der NPC wurde erfolgreich erstellt!");
        }

        @Command(
                aliases = {"edit"},
                desc = "Edit conversation NPC",
                usage = "-c neuer-conversation-name",
                flags = "c:"
        )
        @CommandPermissions("rcconversations.npc.create")
        public void editNPC(CommandContext context, CommandSender sender) throws CommandException {

            if (!(sender instanceof Player)) {
                sender.sendMessage(org.bukkit.ChatColor.RED + "Ingame command!");
                return;
            }
            Player player = (Player) sender;
            String newConversation = null;
            NPCEdit npcedit = NPCEdit.getInstance(plugin);

            if (!context.hasFlag('c') && !context.hasFlag('n') && npcedit.isRegistered(player.getUniqueId())) {

                npcedit.removePlayer(player.getUniqueId());
                sender.sendMessage(org.bukkit.ChatColor.YELLOW + "Conversation Editormodus verlassen!");
            }

            if (context.hasFlag('c')) {
                newConversation = context.getFlag('c');
                findTemplate(newConversation);
            }

            npcedit.addPlayer(player.getUniqueId(), new NPCEditSettings(newConversation));
            sender.sendMessage(org.bukkit.ChatColor.GREEN + "Klicke einen NPC an um deine Änderungen zu übernehmen!");
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

        private ConversationTemplate findTemplate(String identifier) throws CommandException {

            List<ConversationTemplate> templates = plugin.getConversationManager().findConversationTemplate(identifier);

            if (templates.isEmpty()) {
                throw new CommandException("Es gibt keine Conversation mit dem Namen: " + identifier);
            }
            if (templates.size() > 1) {
                throw new CommandException("Mehrere Conversationen mit dem Namen " + identifier + " gefunden: " +
                        templates.stream().map(ConversationTemplate::getIdentifier).collect(Collectors.joining(",")));
            }

            return templates.get(0);
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
