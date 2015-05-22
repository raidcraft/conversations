package de.raidcraft.conversations.npc;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Control class for editing NPC in admin mode.
 *
 * @author Philip Urban, Dragonfire
 */
public class NPCEdit {

    private static NPCEdit INSTANCE;
    private Plugin plugin;
    private Map<UUID, Listener> editListeners = new HashMap<>();
    private Map<UUID, NPCEditSettings> npcPlayerSettings = new HashMap<>();

    private NPCEdit(Plugin plugin) {

        this.plugin = plugin;
    }

    public static NPCEdit getInstance(Plugin plugin) {

        if (INSTANCE == null) {
            INSTANCE = new NPCEdit(plugin);
        }
        return INSTANCE;
    }

    public void addPlayer(UUID player, NPCEditSettings npcSetting) {

        Listener editListener = new NPCEditListener(player, this);
        editListeners.put(player, editListener);
        npcPlayerSettings.put(player, npcSetting);
        Bukkit.getPluginManager().registerEvents(editListener, plugin);
    }

    public boolean isRegistered(UUID player) {

        return editListeners.containsKey(player);
    }

    public void removePlayer(UUID player) {

        Listener oldListener = this.editListeners.remove(player);
        if (oldListener != null) {
            HandlerList.unregisterAll(oldListener);
        }
        npcPlayerSettings.remove(player);
    }

    public boolean editNPC(Player player, NPC npc) {

        if (player == null || npc == null) return false;
        NPCEditSettings settings = npcPlayerSettings.get(player.getUniqueId());
        if (settings == null) return false;

        ConversationsTrait trait = npc.getTrait(ConversationsTrait.class);
        if (settings.getNewConversation() != null) {
            trait.setConversationName(settings.getNewConversation());
        }
        player.sendMessage(ChatColor.YELLOW + "NPC wurde bearbeitet: Conv = " + trait.getConversationName());
        return true;
    }
}
