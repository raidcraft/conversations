package de.raidcraft.conversations.listener;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.conversations.Conversations;
import de.raidcraft.api.conversations.conversation.Conversation;
import de.raidcraft.api.conversations.events.ConversationHostInteractEvent;
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
        // we are going to track the conversation of the player before the host.interact event
        // so we can check if the conversation changed during the event (e.g. conversation.startStage withAction)
        Optional<Conversation> activeConversation = Conversations.getActiveConversation(event.getClicker());
        // lets trigger the host interact event to allow actions and stuff to react
        Optional<String> identifier = host.get().getIdentifier();
        if (!identifier.isPresent()) return;
        // check if the player has a previous conversation before the interact
        ConversationHostInteractEvent hostInteractEvent = new ConversationHostInteractEvent(identifier.get(), host.get(), event.getClicker());
        RaidCraft.callEvent(hostInteractEvent);
        if (hostInteractEvent.isCancelled()) {
            event.setCancelled(true);
            return;
        }
        // lets get the now active conversation and compare it to the previous one
        Optional<Conversation> newActiveConversation = Conversations.getActiveConversation(event.getClicker());
        // nothing changed if both conversation are equal
        if ((!activeConversation.isPresent() && !newActiveConversation.isPresent())
                || (activeConversation.isPresent() && newActiveConversation.isPresent() && activeConversation.equals(newActiveConversation))) {
            host.get().startConversation(event.getClicker());
        }
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