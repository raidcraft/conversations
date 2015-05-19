package de.raidcraft.conversations.tables;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.UUID;

/**
 * @author mdoering
 */
@Entity
@Getter
@Setter
@Table(name = "conv_conversation_variables")
public class TConversationVariable {

    @Id
    private int id;
    private UUID player;
    private UUID host;
    private String conversation;
    private String stage;
    private String name;
    private String value;
    private Timestamp timestamp;
}
