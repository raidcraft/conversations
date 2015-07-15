package de.raidcraft.conversations.npc;

import de.raidcraft.api.npc.NPC_Manager;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;

/**
 * @author Dragonfire
 */
public class ConversationNPCManager {

    public static final String CITIZENS_FACTORY_NAME = "RCConversations";

    public static NPC createNPC(String name) {

        return NPC_Manager.getInstance().createNonPersistNpc(name, CITIZENS_FACTORY_NAME);
    }

    public static NPC spawnNPC(String name, Location location) {

        return NPC_Manager.getInstance().spawnNonPersistNpc(location, name, CITIZENS_FACTORY_NAME);
    }

    public static void despawnNPCs() {

        NPC_Manager.getInstance().clear(CITIZENS_FACTORY_NAME);
    }
}
