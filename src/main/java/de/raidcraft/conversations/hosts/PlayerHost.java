package de.raidcraft.conversations.hosts;

import de.raidcraft.api.conversations.host.AbstractConversationHost;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * @author mdoering
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PlayerHost extends AbstractConversationHost<Player> {

    public PlayerHost(Player player, ConfigurationSection config) {

        super(player.getUniqueId(), player, config);
    }

    @Override
    protected void load(ConfigurationSection config) {
        //TODO: implement
    }

    @Override
    public Location getLocation() {

        return getType().getLocation();
    }
}
