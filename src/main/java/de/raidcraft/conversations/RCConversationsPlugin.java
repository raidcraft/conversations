package de.raidcraft.conversations;

import de.raidcraft.api.BasePlugin;
import de.raidcraft.api.action.ActionAPI;
import de.raidcraft.api.conversations.conversation.Conversation;
import de.raidcraft.conversations.actions.AbortConversationAction;
import de.raidcraft.conversations.actions.ChangeStageAction;
import de.raidcraft.conversations.actions.EndConversationAction;
import de.raidcraft.conversations.actions.SetVariableAction;
import de.raidcraft.conversations.commands.ConversationCommands;
import de.raidcraft.conversations.requirements.CompareVariableRequirement;
import de.raidcraft.conversations.tables.TConversationVariable;
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
        registerActionAPI();
    }

    @Override
    public void disable() {
        //TODO: implement
    }

    private void registerActionAPI() {

        ActionAPI.register(this).global()
                .action(new ChangeStageAction(), Conversation.class)
                .action(new EndConversationAction(), Conversation.class)
                .action(new AbortConversationAction(), Conversation.class)
                .action(new SetVariableAction(), Conversation.class)
                .requirement(new CompareVariableRequirement(), Conversation.class);
    }

    @Override
    public List<Class<?>> getDatabaseClasses() {

        ArrayList<Class<?>> classes = new ArrayList<>();
        classes.add(TPlayerConversation.class);
        classes.add(TConversationVariable.class);
        return classes;
    }
}
