package de.raidcraft.conversations.hosts;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.conversations.host.AbstractConversationHost;
import de.raidcraft.api.conversations.host.ConversationHost;
import de.raidcraft.api.conversations.host.ConversationHostFactory;
import de.raidcraft.api.npc.NPC_Manager;
import de.raidcraft.conversations.RCConversationsPlugin;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.trait.LookClose;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.metadata.FixedMetadataValue;

/**
 * @author mdoering
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NPCHost extends AbstractConversationHost<NPC> {

    public static class NPCHostFactory implements ConversationHostFactory<NPC> {

        @Override
        public ConversationHost<NPC> create(String plugin, String identifier, Location location) {

            return new NPCHost(identifier, NPC_Manager.getInstance().spawnNonPersistNpc(location, "UNNAMED", plugin));
        }

        @Override
        public ConversationHost<NPC> create(NPC type) {

            NPCHost npcHost = new NPCHost(null, type);
            if (!npcHost.getType().isSpawned() && npcHost.getType().getStoredLocation() != null) {
                npcHost.getType().spawn(type.getStoredLocation());
            }
            return npcHost;
        }
    }

    public NPCHost(String identifier, NPC npc) {

        super(npc.getUniqueId(), identifier, npc);
        setName(npc.getName());
    }

    @Override
    public void load(ConfigurationSection config) {

        super.load(config);
        if (config.isSet("name")) {
            getType().setName(config.getString("name"));
            setName(getType().getName());
        }
        if (config.isSet("entity-type")) getType().setBukkitEntityType(EntityType.valueOf(config.getString("entity-type")));
        if (config.isSet("protected")) getType().setProtected(config.getBoolean("protected", true));
        if (config.isConfigurationSection("look-close")) {
            LookClose lookClose = new LookClose();
            lookClose.setRealisticLooking(config.getBoolean("look-close.realistic", true));
            lookClose.setRange(config.getInt("look-close.radius", 7));
            getType().addTrait(lookClose);
        } else if (config.getBoolean("look-close", true)) {
            getType().addTrait(LookClose.class);
        }

        if (config.isConfigurationSection("equipment")) {
            loadEquipment(config.getConfigurationSection("equipment"));
        }

        if (config.isList("entity-metakeys") && getType().isSpawned()) {
            config.getStringList("entity-metakeys").forEach(key -> {
                getType().getEntity().setMetadata(key, new FixedMetadataValue(RaidCraft.getComponent(RCConversationsPlugin.class), true));
            });
        }
    }

    private void loadEquipment(ConfigurationSection equipment) {

        Equipment equipmentTrait = getType().getTrait(Equipment.class);
        if (equipment != null && equipmentTrait != null) {
            RaidCraft.getItem(equipment.getString("hand")).ifPresent(item -> equipmentTrait.set(0, item));
            RaidCraft.getItem(equipment.getString("head")).ifPresent(item -> equipmentTrait.set(1, item));
            RaidCraft.getItem(equipment.getString("chest")).ifPresent(item -> equipmentTrait.set(2, item));
            RaidCraft.getItem(equipment.getString("legs")).ifPresent(item -> equipmentTrait.set(3, item));
            RaidCraft.getItem(equipment.getString("boots")).ifPresent(item -> equipmentTrait.set(4, item));
        }
    }

    @Override
    public Location getLocation() {

        if (getType().isSpawned() && getType().getEntity() != null) {
            return getType().getEntity().getLocation();
        }
        return getType().getStoredLocation();
    }

    @Override
    public void delete() {

        getType().destroy();
    }
}
