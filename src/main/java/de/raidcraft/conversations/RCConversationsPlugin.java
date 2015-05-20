package de.raidcraft.conversations;

import de.raidcraft.api.BasePlugin;
import de.raidcraft.api.action.ActionAPI;
import de.raidcraft.api.config.ConfigurationBase;
import de.raidcraft.api.config.Setting;
import de.raidcraft.api.conversations.conversation.Conversation;
import de.raidcraft.api.quests.QuestConfigLoader;
import de.raidcraft.api.quests.Quests;
import de.raidcraft.conversations.actions.AbortConversationAction;
import de.raidcraft.conversations.actions.ChangeStageAction;
import de.raidcraft.conversations.actions.EndConversationAction;
import de.raidcraft.conversations.actions.SetVariableAction;
import de.raidcraft.conversations.commands.ConversationCommands;
import de.raidcraft.conversations.requirements.CompareVariableRequirement;
import de.raidcraft.conversations.tables.TPlayerConversation;
import de.raidcraft.conversations.tables.TPlayerVariable;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mdoering
 */
public class RCConversationsPlugin extends BasePlugin {

    @Getter
    private ConversationManager conversationManager;
    @Getter
    private LocalConfiguration configuration;

    @Override
    public void enable() {

        this.conversationManager = new ConversationManager(this);
        this.configuration = configure(new LocalConfiguration(this));
        registerCommands(ConversationCommands.class);
        registerActionAPI();
        Quests.registerQuestLoader(new QuestConfigLoader("host") {
            @Override
            public void loadConfig(String id, ConfigurationSection config) {

                getConversationManager().loadConversation(id, config);
            }
        });
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
        classes.add(TPlayerVariable.class);
        return classes;
    }

    public static class LocalConfiguration extends ConfigurationBase<RCConversationsPlugin> {

        public LocalConfiguration(RCConversationsPlugin plugin) {

            super(plugin, "config.yml");
        }

        @Setting("conversation.abort-warn-radius")
        public int conversationAbortWarnRadius = 5;
        @Setting("conversation.abort-radius")
        public int conversationAbortRadius = 10;
    }
}
