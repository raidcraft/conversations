package de.raidcraft.conversations.tables;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.conversations.host.ConversationHost;
import de.raidcraft.util.LocationUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "rc_conversation_persistent_hosts")
public class TPersistentHost {

    @Id
    private int id;
    private UUID host;
    private UUID creator;
    private String conversation;
    private String hostType;
    private String world;
    private int x;
    private int y;
    private int z;

    @OneToMany(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "host_id")
    private Set<TPersistentHostOption> options = new HashSet<>();

    public TPersistentHost() {}

    public TPersistentHost(Player creator, ConversationHost<?> host) {

        this.host = host.getUniqueId();
        this.creator = creator.getUniqueId();
        this.world = host.getLocation().getWorld().getName();
        this.x = host.getLocation().getBlockX();
        this.y = host.getLocation().getBlockY();
        this.z = host.getLocation().getBlockZ();
    }

    public Optional<Location> getLocation() {

        World world = LocationUtil.getCaseInsensitiveWorld(this.world);
        if (world == null) {
            RaidCraft.LOGGER.warning("world " + this.world + " of persistent host " + id + " is not loaded! Not loading host...");
            return Optional.empty();
        }
        return Optional.of(new Location(world, x, y, z));
    }
}
