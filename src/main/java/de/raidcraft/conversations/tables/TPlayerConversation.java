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
@Table(name = "rc_conversation_saved_conversations")
public class TPlayerConversation {

    @Id
    private int id;
    private UUID player;
    private UUID host;
    private String conversation;
    private String stage;
    private Timestamp timestamp;
}
