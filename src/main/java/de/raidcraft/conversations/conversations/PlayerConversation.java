package de.raidcraft.conversations.conversations;

import com.avaje.ebean.EbeanServer;
import de.raidcraft.RaidCraft;
import de.raidcraft.api.conversations.Conversations;
import de.raidcraft.api.conversations.conversation.AbstractConversation;
import de.raidcraft.api.conversations.conversation.Conversation;
import de.raidcraft.api.conversations.conversation.ConversationEndReason;
import de.raidcraft.api.conversations.conversation.ConversationTemplate;
import de.raidcraft.api.conversations.host.ConversationHost;
import de.raidcraft.api.conversations.stage.Stage;
import de.raidcraft.conversations.RCConversationsPlugin;
import de.raidcraft.conversations.tables.TPlayerConversation;
import de.raidcraft.conversations.tables.TPlayerVariable;
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

    private Optional<TPlayerVariable> findVariable(String name) {

        EbeanServer database = RaidCraft.getDatabase(RCConversationsPlugin.class);
        return Optional.ofNullable(database.find(TPlayerVariable.class).where()
                .eq("player", getEntity().getUniqueId())
                .eq("name", name)
                .findUnique());
    }

    @Override
    public void setGlobal(String key, Object value) {

        set(key, value);
        EbeanServer database = RaidCraft.getDatabase(RCConversationsPlugin.class);
        Optional<TPlayerVariable> optional = findVariable(key);
        TPlayerVariable variable;
        if (!optional.isPresent()) {
            variable = new TPlayerVariable();
            variable.setPlayer(getEntity().getUniqueId());
            variable.setName(key);
        } else {
            variable = optional.get();
        }
        variable.setConversation(getIdentifier());
        Optional<Stage> currentStage = getCurrentStage();
        if (currentStage.isPresent()) variable.setStage(currentStage.get().getIdentifier());
        variable.setValue(value.toString());
        database.save(variable);
    }

    @Override
    public Object get(String path, Object def) {

        Optional<TPlayerVariable> variable = findVariable(path);
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
    public boolean start() {

        boolean start = super.start();
        if (start) {
            Conversations.addActiveConversation(this);
        }
        return start;
    }

    @Override
    public Optional<Stage> end(ConversationEndReason reason) {

        Optional<Stage> stage = super.end(reason);
        Conversations.removeActiveConversation(getEntity());
        return stage;
    }

    @Override
    public Optional<Stage> abort(ConversationEndReason reason) {

        Optional<Stage> stage = super.abort(reason);
        Conversations.removeActiveConversation(getEntity());
        return stage;
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
