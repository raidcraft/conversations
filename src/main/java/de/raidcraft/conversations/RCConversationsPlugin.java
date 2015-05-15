package de.raidcraft.conversations;

import de.raidcraft.api.BasePlugin;
import de.raidcraft.conversations.commands.ConversationCommands;
import de.raidcraft.conversations.tables.TPlayerConversation;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mdoering
 */
public class RCConversationsPlugin extends BasePlugin {

    @Getter
    private ConversationManager conversationManager;

    @Override
    public void enable() {

        this.conversationManager = new ConversationManager(this);
        registerCommands(ConversationCommands.class);
    }

    @Override
    public void disable() {
        //TODO: implement
    }

    @Override
    public List<Class<?>> getDatabaseClasses() {

        ArrayList<Class<?>> classes = new ArrayList<>();
        classes.add(TPlayerConversation.class);
        return classes;
    }
}
