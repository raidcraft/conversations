package de.raidcraft.conversations.listener;

import de.raidcraft.api.conversations.host.ConversationHost;
import de.raidcraft.conversations.RCConversationsPlugin;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Optional;

/**
 * Author: Philip
 * Date: 16.03.13 - 02:19
 */
public class NPCListener implements Listener {

    private RCConversationsPlugin plugin;

    public NPCListener(RCConversationsPlugin plugin) {

        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onRightClick(NPCRightClickEvent event) {

        Optional<ConversationHost<NPC>> host = plugin.getConversationManager().getConversationHost(event.getNPC());
        if (!host.isPresent()) return;
        host.get().startConversation(event.getClicker());
    }

    // TODO: reenable this when citizens pulls fix: https://github.com/CitizensDev/Citizens2/pull/30
    // currently citizens fires a LeftClickEvent when the entity is damaged
    /*
    @EventHandler(ignoreCancelled = true)
    public void onLeftClick(NPCLeftClickEvent event) {
        ConversationHost host = getClickedHost(event);
        if(host == null) return;

        plugin.getConversationManager().triggerConversation(host, event.getClicker());
    }*/
}