package de.raidcraft.conversations.hosts;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.conversations.host.AbstractConversationHost;
import de.raidcraft.api.conversations.host.ConversationHost;
import de.raidcraft.api.conversations.host.ConversationHostFactory;
import de.raidcraft.api.items.CustomItemException;
import de.raidcraft.conversations.RCConversationsPlugin;
import de.raidcraft.conversations.npc.ConversationNPCManager;
import de.raidcraft.conversations.npc.TalkCloseTrait;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
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
        public ConversationHost<NPC> create(Location location) {

            return new NPCHost(ConversationNPCManager.spawnNPC("UNNAMED", location));
        }

        @Override
        public ConversationHost<NPC> create(NPC type) {

            NPCHost npcHost = new NPCHost(type);
            if (!npcHost.getType().isSpawned() && npcHost.getType().getStoredLocation() != null) {
                npcHost.getType().spawn(type.getStoredLocation());
            }
            return npcHost;
        }
    }

    public NPCHost(NPC npc) {

        super(npc.getUniqueId(), npc);
    }

    @Override
    public void load(ConfigurationSection config) {

        super.load(config);
        if (config.isSet("name")) getType().setName(config.getString("name"));
        if (config.isSet("entity-type")) getType().setBukkitEntityType(EntityType.valueOf(config.getString("entity-type")));
        if (config.isSet("protected")) getType().setProtected(config.getBoolean("protected", true));
        if (config.isSet("talk-close")) getType().addTrait(TalkCloseTrait.class);

        if (config.isConfigurationSection("equipment")) {
            loadEquipment(config.getConfigurationSection("equipment"));
        }

        if (config.isList("entity-metakeys")) {
            config.getStringList("entity-metakeys").forEach(key -> {
                getType().getEntity().setMetadata(key, new FixedMetadataValue(RaidCraft.getComponent(RCConversationsPlugin.class), true));
            });
        }
    }

    private void loadEquipment(ConfigurationSection equipment) {

        Equipment equipmentTrait = getType().getTrait(Equipment.class);
        if (equipment != null && equipmentTrait != null) {
            String itemName;
            try {
                itemName = equipment.getString("hand");
                if (itemName != null && !itemName.equals("")) {
                    equipmentTrait.set(0, RaidCraft.getItem(itemName));
                }
            } catch (CustomItemException e) {
                e.printStackTrace();
            }

            try {
                itemName = equipment.getString("helmet");
                if (itemName != null && !itemName.equals("")) {
                    equipmentTrait.set(1, RaidCraft.getItem(itemName));
                }
            } catch (CustomItemException e) {
                e.printStackTrace();
            }

            try {
                itemName = equipment.getString("chestplate");
                if (itemName != null && !itemName.equals("")) {
                    equipmentTrait.set(2, RaidCraft.getItem(itemName));
                }
            } catch (CustomItemException e) {
                e.printStackTrace();
            }

            try {
                itemName = equipment.getString("leggings");
                if (itemName != null && !itemName.equals("")) {
                    equipmentTrait.set(3, RaidCraft.getItem(itemName));
                }
            } catch (CustomItemException e) {
                e.printStackTrace();
            }

            try {
                itemName = equipment.getString("boots");
                if (itemName != null && !itemName.equals("")) {
                    equipmentTrait.set(4, RaidCraft.getItem(itemName));
                }
            } catch (CustomItemException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Location getLocation() {

        if (getType().isSpawned() && getType().getEntity() != null) {
            return getType().getEntity().getLocation();
        }
        return getType().getStoredLocation();
    }
}
