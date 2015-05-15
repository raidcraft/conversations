package de.raidcraft.conversations;

import com.avaje.ebean.EbeanServer;
import de.raidcraft.RaidCraft;
import de.raidcraft.api.conversations.conversation.AbstractConversation;
import de.raidcraft.api.conversations.conversation.Conversation;
import de.raidcraft.api.conversations.host.ConversationHost;
import de.raidcraft.api.conversations.conversation.ConversationTemplate;
import de.raidcraft.api.conversations.stage.Stage;
import de.raidcraft.conversations.tables.TPlayerConversation;
import mkremins.fanciful.FancyMessage;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

/**
 * @author mdoering
 */
public class PlayerConversation extends AbstractConversation<Player> {

    public PlayerConversation(Player player, ConversationTemplate conversationTemplate, ConversationHost conversationHost) {

        super(player, conversationTemplate, conversationHost);
    }

    @Override
    public Conversation<Player> sendMessage(String... lines) {

        for (String line : lines) {
            getEntity().sendMessage(line);
        }
        return this;
    }

    @Override
    public Conversation<Player> sendMessage(FancyMessage... lines) {

        for (FancyMessage line : lines) {
            line.send(getEntity());
        }
        return this;
    }

    @Override
    public void save() {

        EbeanServer database = RaidCraft.getDatabase(RCConversationsPlugin.class);
        TPlayerConversation entry = database.find(TPlayerConversation.class).where()
                .eq("player", getEntity().getUniqueId())
                .eq("host", getHost().getUniqueId())
                .eq("conversation", getIdentifier())
                .findUnique();
        if (entry == null) {
            entry = new TPlayerConversation();
            entry.setPlayer(getEntity().getUniqueId());
            entry.setHost(getHost().getUniqueId());
        }
        entry.setConversation(getIdentifier());
        entry.setTimestamp(Timestamp.from(Instant.now()));
        Optional<Stage> currentStage = getCurrentStage();
        if (currentStage.isPresent()) {
            entry.setStage(currentStage.get().getIdentifier());
        }
        database.save(entry);
    }

    @Override
    protected void load() {

        EbeanServer database = RaidCraft.getDatabase(RCConversationsPlugin.class);
        TPlayerConversation entry = database.find(TPlayerConversation.class).where()
                .eq("player", getEntity().getUniqueId())
                .eq("host", getHost().getUniqueId())
                .eq("conversation", getIdentifier())
                .findUnique();
        if (entry != null && entry.getStage() != null) {
            Optional<Stage> stage = getStage(entry.getStage());
            if (stage.isPresent()) {
                setCurrentStage(stage.get());
            }
        }
    }
}
