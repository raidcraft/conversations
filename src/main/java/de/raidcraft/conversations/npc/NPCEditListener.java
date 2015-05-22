package de.raidcraft.conversations.npc;

import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.UUID;

/**
 * Listener, for each player (admin) that has an acitve edit mode for npcs.
 *
 * @author Dragonfire
 */
public class NPCEditListener implements Listener {

    private NPCEdit npc_edit;
    private UUID player;

    public NPCEditListener(UUID editPlayer, NPCEdit npc_edit) {

        this.player = editPlayer;
        this.npc_edit = npc_edit;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onRightClick(NPCRightClickEvent event) {

        if (event.getClicker().getUniqueId() != this.player) return;
        event.setCancelled(true);
        this.npc_edit.editNPC(event.getClicker(), event.getNPC());
    }

    // TODO: reenable this when citizens pulls fix: https://github.com/CitizensDev/Citizens2/pull/30
    // currently citizens fires a LeftClickEvent when the entity is damaged
    /*
    @EventHandler(ignoreCancelled = true)
    public void onLeftClick(NPCLeftClickEvent event) {
        if(event.getClicker().getUniqueId() != this.player) return;
        event.setCancelled(true);
    }*/
}
