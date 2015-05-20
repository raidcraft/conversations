package de.raidcraft.conversations.tables;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

/**
 * @author mdoering
 */
@Entity
@Table(
        name = "conv_player_variables",
        uniqueConstraints = @UniqueConstraint(columnNames = {"player", "name"})
)
@Getter
@Setter
public class TPlayerVariable {

    @Id
    private int id;
    @Column(nullable = false)
    private UUID player;
    private String conversation;
    private String stage;
    @Column(nullable = false)
    private String name;
    private String value;
    private Timestamp lastUpdate;

    @PreUpdate
    public void onUpdate() {

        this.lastUpdate = Timestamp.from(Instant.now());
    }
}
