package de.raidcraft.conversations.tables;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "conversation_persistent_host_options")
public class TPersistentHostOption {

    @Id
    private int id;
    private String option;
    private String value;

    @ManyToOne
    private TPersistentHost host;
}
