package de.raidcraft.conversations.hosts;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.conversations.host.AbstractConversationHost;
import de.raidcraft.api.conversations.host.ConversationHost;
import de.raidcraft.api.conversations.host.ConversationHostFactory;
import de.raidcraft.api.npc.NPC_Manager;
import de.raidcraft.conversations.RCConversationsPlugin;
import de.raidcraft.conversations.npc.TalkCloseTrait;
import de.raidcraft.util.ConfigUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.npc.skin.SkinnableEntity;
import net.citizensnpcs.trait.LookClose;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Optional;
import java.util.UUID;

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
        if (config.isSet("skin") && getType().getEntity() instanceof SkinnableEntity) {
            if (!config.isSet("skin.value") || !config.isSet("skin.signature")) {
                RaidCraft.LOGGER.warning("Invalid SKIN for Host " + ConfigUtil.getFileName(config));
            } else {
                SkinnableEntity entity = (SkinnableEntity) getType().getEntity();
                entity.setSkinPersistent(getUniqueId().toString(), config.getString("skin.value"), config.getString("skin.signature"));
            }
        }
        if (config.isSet("entity-type")) getType().setBukkitEntityType(EntityType.valueOf(config.getString("entity-type")));
        if (config.isSet("protected")) getType().setProtected(config.getBoolean("protected", true));
        if (config.isConfigurationSection("talk-close")) {
            int radius = config.getInt("talk-close.radius", 5);
            if (radius > 0) {
                getType().addTrait(TalkCloseTrait.class);
                getType().getTrait(TalkCloseTrait.class).setRadius(radius);
            }
        } else if (config.getBoolean("talk-close", false)) {
            getType().addTrait(TalkCloseTrait.class);
        }
        if (config.isConfigurationSection("look-close")) {
            getType().addTrait(LookClose.class);
            LookClose lookClose = getType().getTrait(LookClose.class);
            lookClose.setRealisticLooking(config.getBoolean("look-close.realistic", true));
            lookClose.setRange(config.getInt("look-close.radius", 7));
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
    public boolean addTrait(Class<? extends Trait> traitClass) {
        getType().addTrait(traitClass);
        return true;
    }

    @Override
    public boolean addTrait(Trait trait) {
        getType().addTrait(trait);
        return true;
    }

    @Override
    public <TTrait extends Trait> Optional<TTrait> getTrait(Class<TTrait> traitClass) {
        return Optional.ofNullable(getType().getTrait(traitClass));
    }

    @Override
    public boolean hasTrait(Class<? extends Trait> traitClass) {
        return getType().hasTrait(traitClass);
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
