package de.raidcraft.conversations;

import de.raidcraft.api.BasePlugin;
import de.raidcraft.api.action.ActionAPI;
import de.raidcraft.api.config.ConfigurationBase;
import de.raidcraft.api.config.Setting;
import de.raidcraft.api.conversations.Conversations;
import de.raidcraft.api.conversations.conversation.Conversation;
import de.raidcraft.api.npc.NPC_Manager;
import de.raidcraft.api.npc.RC_Traits;
import de.raidcraft.api.quests.QuestConfigLoader;
import de.raidcraft.api.quests.Quests;
import de.raidcraft.conversations.actions.AbortConversationAction;
import de.raidcraft.conversations.actions.ChangeStageAction;
import de.raidcraft.conversations.actions.EndConversationAction;
import de.raidcraft.conversations.actions.SetVariableAction;
import de.raidcraft.conversations.actions.ShowAnswersAction;
import de.raidcraft.conversations.actions.StartConversationAction;
import de.raidcraft.conversations.commands.ConversationCommands;
import de.raidcraft.conversations.listener.ChatListener;
import de.raidcraft.conversations.listener.ConversationListener;
import de.raidcraft.conversations.listener.NPCListener;
import de.raidcraft.conversations.listener.PlayerListener;
import de.raidcraft.conversations.npc.ConversationNPCManager;
import de.raidcraft.conversations.npc.TalkCloseTrait;
import de.raidcraft.conversations.requirements.CompareVariableRequirement;
import de.raidcraft.conversations.tables.TPersistentHost;
import de.raidcraft.conversations.tables.TPersistentHostOption;
import de.raidcraft.conversations.tables.TPlayerConversation;
import de.raidcraft.conversations.tables.TPlayerVariable;
import de.raidcraft.conversations.trigger.HostTrigger;
import lombok.Getter;
import org.bukkit.Bukkit;
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

        Quests.registerQuestLoader(new QuestConfigLoader("host", 50) {
            @Override
            public void loadConfig(String id, ConfigurationSection config) {

                Conversations.createConversationHost(id, config);
            }
        });

        Quests.registerQuestLoader(new QuestConfigLoader("conv", 10) {
            @Override
            public void loadConfig(String id, ConfigurationSection config) {

                Conversations.loadConversation(id, config);
            }
        });

        registerEvents(new ChatListener(this));
        registerEvents(new PlayerListener(this));
        registerEvents(new ConversationListener(this));

        // register NPC traits, trait listener and load all NPC's
        NPC_Manager.getInstance().registerTrait(TalkCloseTrait.class, RC_Traits.TALK_CLOSE);

        Bukkit.getPluginManager().registerEvents(new NPCListener(this), this);
        // load all persistant conversation hosts from the database after everything is properly registered
        Bukkit.getScheduler().runTaskLater(this, this::loadPersistantConversationHosts, 15 * 20L);
    }

    @Override
    public void disable() {

        Conversations.disable(getConversationManager());
        getConversationManager().unload();
    }

    @Override
    public void reload() {

        ConversationNPCManager.despawnNPCs();
        getConfiguration().reload();
        getConversationManager().reload();
    }

    private void loadPersistantConversationHosts() {

        List<TPersistentHost> list = getDatabase().find(TPersistentHost.class).findList();
        for (TPersistentHost host : list) {
            getConversationManager().createConversationHost(host);
        }
    }

    private void registerActionAPI() {

        ActionAPI.register(this).global()
                .trigger(new HostTrigger())
                .action(new ShowAnswersAction(), Conversation.class)
                .action(new ChangeStageAction(), Conversation.class)
                .action(new StartConversationAction())
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
        classes.add(TPersistentHostOption.class);
        classes.add(TPersistentHost.class);
        return classes;
    }

    public static class LocalConfiguration extends ConfigurationBase<RCConversationsPlugin> {

        public LocalConfiguration(RCConversationsPlugin plugin) {

            super(plugin, "config.yml");
        }

        @Setting("conversation.abort-warn-radius")
        public int conversationAbortWarnRadius = 5;
        @Setting("host.proximity-max-range")
        public int maxHostProximityRange = 10;
        @Setting("conversation.abort-radius")
        public int conversationAbortRadius = 10;
        @Setting("conversation.exit-words")
        public String[] exitWords = {"exit", "quit", "end", "stop"};
    }
}
