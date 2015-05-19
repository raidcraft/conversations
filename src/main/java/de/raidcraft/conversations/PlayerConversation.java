package de.raidcraft.conversations;

import com.avaje.ebean.EbeanServer;
import de.raidcraft.RaidCraft;
import de.raidcraft.api.conversations.conversation.AbstractConversation;
import de.raidcraft.api.conversations.conversation.Conversation;
import de.raidcraft.api.conversations.conversation.ConversationTemplate;
import de.raidcraft.api.conversations.host.ConversationHost;
import de.raidcraft.api.conversations.stage.Stage;
import de.raidcraft.conversations.tables.TConversationVariable;
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

    private Optional<TConversationVariable> findVariable(String name) {

        EbeanServer database = RaidCraft.getDatabase(RCConversationsPlugin.class);
        return Optional.ofNullable(database.find(TConversationVariable.class).where()
                .eq("conversation", getIdentifier())
                .eq("host", getHost().getUniqueId())
                .eq("player", getEntity().getUniqueId())
                .eq("name", name)
                .findUnique());
    }

    @Override
    public void setGlobal(String key, Object value) {

        set(key, value);
        EbeanServer database = RaidCraft.getDatabase(RCConversationsPlugin.class);
        Optional<TConversationVariable> optional = findVariable(key);
        TConversationVariable variable;
        if (!optional.isPresent()) {
            variable = new TConversationVariable();
            variable.setConversation(getIdentifier());
            variable.setPlayer(getEntity().getUniqueId());
            variable.setHost(getHost().getUniqueId());
        } else {
            variable = optional.get();
        }
        variable.setValue(value.toString());
        variable.setStage(getCurrentStage().isPresent() ? getCurrentStage().get().getIdentifier() : null);
        variable.setTimestamp(Timestamp.from(Instant.now()));
        database.save(variable);
    }

    @Override
    public Object get(String path, Object def) {

        Optional<TConversationVariable> variable = findVariable(path);
        if (variable.isPresent()) {
            set(variable.get().getName(), variable.get().getValue());
        }
        return super.get(path, def);
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
