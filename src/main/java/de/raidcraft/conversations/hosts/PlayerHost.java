package de.raidcraft.conversations.hosts;

import de.raidcraft.api.conversations.host.AbstractConversationHost;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * @author mdoering
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PlayerHost extends AbstractConversationHost<Player> {

    public PlayerHost(Player player) {

        super(player.getUniqueId(), player);
    }

    @Override
    public Location getLocation() {

        return getType().getLocation();
    }

    @Override
    public void delete() {

        throw new UnsupportedOperationException("Cannot delete player host!");
    }
}
