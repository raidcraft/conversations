package de.raidcraft.conversations.hosts;

import de.raidcraft.api.conversations.host.AbstractConversationHost;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author mdoering
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NPCHost extends AbstractConversationHost<NPC> {

    public NPCHost(NPC npc, ConfigurationSection config) {

        super(npc.getUniqueId(), npc, config);
    }

    @Override
    protected void load(ConfigurationSection config) {
        //TODO: implement
    }

    @Override
    public Location getLocation() {

        if (getType().isSpawned() && getType().getEntity() != null) {
            return getType().getEntity().getLocation();
        }
        return getType().getStoredLocation();
    }
}
