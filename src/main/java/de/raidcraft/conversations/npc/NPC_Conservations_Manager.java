package de.raidcraft.conversations.npc;

import de.raidcraft.api.npc.NPC_Manager;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.trait.CurrentLocation;
import org.bukkit.Location;

/**
 * @author Dragonfire
 */
public class NPC_Conservations_Manager {

    private static NPC_Conservations_Manager INSTANCE;
    private NPC_Manager manager;


    private NPC_Conservations_Manager() {

        manager = NPC_Manager.getInstance();
    }

    public static NPC_Conservations_Manager getInstance() {

        if (INSTANCE == null) {
            INSTANCE = new NPC_Conservations_Manager();
        }
        return INSTANCE;
    }

    /**
     * NPC_Manager.createPersistNpc
     */
    public NPC createPersistNpcConservations(String name, String host, String conversationName) {

        NPC npc = manager.createPersistNpc(name, host);

        npc.addTrait(ConversationsTrait.class);
        npc.getTrait(ConversationsTrait.class).setConversationName(conversationName);

        // add traits
        npc.addTrait(Equipment.class);
        npc.addTrait(CitizensAPI.getTraitFactory().getTraitClass("lookclose"));


        npc.data().set(NPC.DEFAULT_PROTECTED_METADATA, true);

        manager.store(host);
        return npc;
    }

    /**
     * NPC_Manager.spawnPersistNpc
     */
    public NPC spawnPersistNpcConservations(Location loc, String name, String host, String conversationName) {

        NPC npc = this.createPersistNpcConservations(name, host, conversationName);
        npc.spawn(loc);
        manager.store(host);
        return npc;
    }

    /**
     * NPC_Manager.createNonPersistNpc
     */
    public NPC createNonPersistNpcConservations(String name, String host, String conversationName) {

        NPC npc = manager.createNonPersistNpc(name, host);

        npc.addTrait(ConversationsTrait.class);
        npc.getTrait(ConversationsTrait.class).setConversationName(conversationName);

        npc.addTrait(CitizensAPI.getTraitFactory().getTraitClass("lookclose"));

        npc.data().set(NPC.DEFAULT_PROTECTED_METADATA, true);

        return npc;
    }

    /**
     * NPC_Manager.spawnNonPersistNpc
     */
    public NPC spawnNonPersistNpcConservations(Location loc, String name, String host, String conversationName) {

        NPC npc = this.createNonPersistNpcConservations(name, host, conversationName);
        npc.addTrait(CurrentLocation.class);
        npc.getTrait(CurrentLocation.class).setLocation(loc);
        npc.spawn(loc);
        return npc;
    }
}
