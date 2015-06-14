package de.raidcraft.conversations.tables;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "conversation_persistent_hosts")
public class TPersistentHosts {

    @Id
    private int id;
    private UUID creator;
    private String conversation;
    private String hostType;
    private String world;
    private int x;
    private int y;
    private int z;

    @OneToMany(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "host_id")
    private Set<TPersistentHostOptions> options;
}
