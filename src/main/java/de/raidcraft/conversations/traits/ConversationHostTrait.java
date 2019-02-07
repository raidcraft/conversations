package de.raidcraft.conversations.traits;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.conversations.host.AbstractConversationHost;
import de.raidcraft.conversations.ConversationManager;
import de.raidcraft.npcs.ConfigStorage;
import de.raidcraft.npcs.RCNPCsPlugin;
import de.raidcraft.npcs.traits.ToFNPCTrait;
import jdk.internal.joptsimple.internal.Strings;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.DataKey;
import org.bukkit.Location;

import java.util.UUID;

@TraitName("host")
public class ConversationHostTrait extends Trait {

    private String id;

    public ConversationHostTrait() {
        super("host");
    }

    public String getId() {
        if (Strings.isNullOrEmpty(id)) {
            return getNPC().data().get(RCNPCsPlugin.NPC_TOF_ID);
        }
        return id;
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        if (key instanceof ConfigStorage.YamlKey) {
            this.id = ((ConfigStorage.YamlKey) key).getId();
            return;
        }
        if (!getNPC().hasTrait(ToFNPCTrait.class)) {
            throw new NPCLoadException("Cannot spawn conversation host without ToF trait!");
        }
        id = getNPC().getTrait(ToFNPCTrait.class).getConfigPath();
    }

    @Override
    public void onSpawn() {
        RaidCraft.getComponent(ConversationManager.class)
                .registerConversationHost(id, new ConversationNPC(getNPC().getUniqueId(), id, getNPC()));
    }

    @Override
    public void onDespawn() {
        RaidCraft.getComponent(ConversationManager.class)
                .unregisterConversationTemplate(id);
    }

    public class ConversationNPC extends AbstractConversationHost<NPC> {

        public ConversationNPC(UUID uniqueId, String identifier, NPC type) {
            super(uniqueId, identifier, type);
        }

        @Override
        public Location getLocation() {
            return getNPC().getStoredLocation();
        }

        @Override
        public void delete() {
            getNPC().destroy();
        }
    }
}
