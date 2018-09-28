package de.raidcraft.conversations.commands;

import com.sk89q.minecraft.util.commands.*;
import de.raidcraft.api.conversations.conversation.Conversation;
import de.raidcraft.api.conversations.conversation.ConversationTemplate;
import de.raidcraft.api.conversations.host.ConversationHost;
import de.raidcraft.api.conversations.host.PlayerHost;
import de.raidcraft.conversations.RCConversationsPlugin;
import de.raidcraft.conversations.tables.TPersistentHost;
import de.raidcraft.conversations.tables.TPersistentHostOption;
import de.raidcraft.util.PaginatedResult;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.Set;
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
            aliases = {"rcca"},
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
                usage = "<conversation> <npc displayName>",
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
            settings.set("type", "NPC");
            if (npcName != null) settings.set("name", npcName);
            if (context.hasFlag('n')) settings.set("talk-close", true);

            if (!settings.isSet("name")) {
                throw new CommandException("Für diese Conversation muss ein NPC Name angegeben werden!");
            }

            ConfigurationSection location = settings.createSection("location");
            location.set("world", player.getWorld().getName());
            location.set("x", player.getLocation().getBlockX());
            location.set("y", player.getLocation().getBlockY());
            location.set("z", player.getLocation().getBlockZ());
            location.set("pitch", player.getLocation().getPitch());
            location.set("yaw", player.getLocation().getYaw());

            Optional<ConversationHost<?>> conversationHost = plugin.getConversationManager().createConversationHost(plugin.getName(), settings);
            if (!conversationHost.isPresent()) {
                throw new CommandException("Unable to create conversation host!");
            }

            conversationHost.get().addDefaultConversation(template);

            ConversationHost<?> host = conversationHost.get();
            TPersistentHost persistentHost = new TPersistentHost(player, host);
            persistentHost.setHostType("NPC");
            persistentHost.setConversation(conversationName);

            for (String key : settings.getKeys(true)) {
                if (settings.isConfigurationSection(key)) continue;
                TPersistentHostOption option = new TPersistentHostOption();
                option.setHost(persistentHost);
                option.setConfKey(key);
                option.setConfValue(settings.getString(key));
                persistentHost.getOptions().add(option);
            }
            plugin.getRcDatabase().save(persistentHost);
            plugin.getRcDatabase().save(persistentHost.getOptions());

            sender.sendMessage(org.bukkit.ChatColor.GREEN + "Der NPC wurde erfolgreich erstellt!");
        }

        @Command(
                aliases = {"delete", "remove"},
                desc = "Removes the nearest host with the given ID.",
                flags = "r:",
                help = "[host id] [-r <radius>]"
        )
        @CommandPermissions("rcconversations.admin.delete")
        public void delete(CommandContext args, CommandSender sender) throws CommandException {

            ConversationHost conversationHost;
            if (args.argsLength() > 0) {
                Optional<ConversationHost<?>> host = plugin.getConversationManager().getConversationHost(args.getString(0));
                if (!host.isPresent()) {
                    throw new CommandException("Host " + args.getString(0) + " not found!");
                }
                conversationHost = host.get();
            } else {
                List<ConversationHost> hosts = plugin.getConversationManager().getNearbyHosts(((Player) sender).getLocation(), args.getFlagInteger('r', 3));
                if (!hosts.isEmpty()) {
                    throw new CommandException("No conversation hosts nearby (" + args.getFlagInteger('r', 3) + " blocks) found!");
                }
                if (hosts.size() > 1) {
                    throw new CommandException("Multiple hosts nearby (" + args.getFlagInteger('r', 3) + " blocks) found! Please choose a smaller radius or specify the ID.");
                }
                conversationHost = hosts.get(0);
            }
            plugin.getConversationManager().deleteConversationHost(conversationHost);
            sender.sendMessage(ChatColor.GREEN + "Host " + conversationHost.getName() + " has been removed successfully!");
        }

        @Command(
                aliases = {"start"},
                desc = "Starts the given conversation.",
                min = 1,
                flags = "p:",
                help = "<conversation id> [-p <player>] [host]"
        )
        @CommandPermissions("rcconversations.admin.startStage")
        public void start(CommandContext args, CommandSender sender) throws CommandException {

            Optional<ConversationTemplate> conversationTemplate = plugin.getConversationManager().getLoadedConversationTemplate(args.getString(0));
            if (!conversationTemplate.isPresent()) {
                throw new CommandException("Es gibt keine Conversation mit der ID: " + args.getString(0));
            }

            Player player = (Player) sender;
            if (args.hasFlag('p')) {
                player = Bukkit.getPlayer(args.getFlag('p'));
                if (player == null) {
                    throw new CommandException("Der Spieler " + args.getFlag('p') + " ist nicht online");
                }
            }

            ConversationHost conversationHost;
            if (args.argsLength() > 1) {
                Optional<ConversationHost<?>> host = plugin.getConversationManager().getConversationHost(args.getString(1));
                if (!host.isPresent()) {
                    throw new CommandException("Es gibt keinen Host mit der ID: " + args.getString(1));
                }
                conversationHost = host.get();
            } else {
                conversationHost = new PlayerHost(player);
            }

            conversationTemplate.get().startConversation(player, conversationHost);
        }

        @Command(
                aliases = {"list"},
                desc = "Lists all configured conversations.",
                flags = "p:",
                help = "[-p <page>]"
        )
        public void list(CommandContext args, CommandSender sender) throws CommandException {

            Set<String> conversations = plugin.getConversationManager().getLoadedConversations();

            new PaginatedResult<String>("Loaded conversations") {
                @Override
                public String format(String entry) {
                    return entry;
                }
            }.display(sender, conversations, args.getFlagInteger('p', 1));
        }

        @Command(
                aliases = {"answer"},
                desc = "Answers to the current conversation.",
                min = 1,
                help = "<answer_uuid>"
        )
        public void answer(CommandContext args, CommandSender sender) throws CommandException {

            Conversation conversation = getActiveConversation(sender);
            conversation.answer(args.getJoinedStrings(0));
        }

        @Command(
                aliases = {"page"},
                desc = "Browses to the next page of the conversation stage."
        )
        public void page(CommandContext args, CommandSender sender) throws CommandException {

            Conversation conversation = getActiveConversation(sender);
            if (!conversation.changePage(args.getInteger(0))) {
                throw new CommandException("Konnte nicht zu Seite " + args.getInteger(0) + " wechseln.");
            }
        }

        private Conversation getActiveConversation(CommandSender sender) throws CommandException {

            if (!(sender instanceof Player)) {
                throw new CommandException("Conversation Holder must be a Player.");
            }
            Optional<Conversation> optional = plugin.getConversationManager().getActiveConversation((Player) sender);
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
