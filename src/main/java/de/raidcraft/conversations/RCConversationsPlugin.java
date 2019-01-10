package de.raidcraft.conversations;

import de.raidcraft.api.BasePlugin;
import de.raidcraft.api.action.ActionAPI;
import de.raidcraft.api.config.Comment;
import de.raidcraft.api.config.ConfigLoader;
import de.raidcraft.api.config.ConfigurationBase;
import de.raidcraft.api.config.Setting;
import de.raidcraft.api.conversations.Conversations;
import de.raidcraft.api.conversations.actions.*;
import de.raidcraft.api.conversations.conversation.Conversation;
import de.raidcraft.api.npc.NPC_Manager;
import de.raidcraft.api.npc.RC_Traits;
import de.raidcraft.api.quests.Quests;
import de.raidcraft.conversations.actions.NpcEmoteAction;
import de.raidcraft.conversations.commands.ConversationCommands;
import de.raidcraft.conversations.listener.ChatListener;
import de.raidcraft.conversations.listener.ConversationListener;
import de.raidcraft.conversations.listener.NPCListener;
import de.raidcraft.conversations.listener.PlayerListener;
import de.raidcraft.conversations.npc.DisguiseTrait;
import de.raidcraft.conversations.npc.TalkCloseTask;
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

        Quests.registerQuestLoader(new ConfigLoader(this, "host", 50) {
            @Override
            public void loadConfig(String id, ConfigurationSection config) {
                Conversations.createConversationHost("Quests", id, config);
            }

            @Override
            public void unloadConfig(String id) {
                Conversations.removeConversationHost("Quests", id);
            }
        });

        Quests.registerQuestLoader(new ConfigLoader(this, "conv", 10) {
            @Override
            public void loadConfig(String id, ConfigurationSection config) {
                Conversations.loadConversation(id, config);
            }

            @Override
            public void unloadConfig(String id) {
                Conversations.unloadConversation(id);
            }
        });

        registerEvents(new ChatListener(this));
        registerEvents(new PlayerListener(this));
        registerEvents(new ConversationListener(this));

        // register NPC traits, trait listener and loadConfig all NPC's
        NPC_Manager.getInstance().registerTrait(TalkCloseTrait.class, RC_Traits.TALK_CLOSE);
        NPC_Manager.getInstance().registerTrait(DisguiseTrait.class, RC_Traits.DISGUISE);

        Bukkit.getPluginManager().registerEvents(new NPCListener(this), this);
        // loadConfig all persistent conversation hosts from the database after everything is properly registered
        Bukkit.getScheduler().runTaskLater(this, this::loadPersistantConversationHosts, 15 * 20L);

        // this starts the talk-close task
        TalkCloseTask.getInstance().regenerateAllTalkChunks();
    }

    @Override
    public void disable() {

        Conversations.disable(getConversationManager());
        getConversationManager().unload();
    }

    @Override
    public void reload() {

        NPC_Manager.getInstance().clear(getName());
        getConfiguration().reload();
        getConversationManager().reload();
        loadPersistantConversationHosts();
    }

    private void loadPersistantConversationHosts() {

        List<TPersistentHost> list = getRcDatabase().find(TPersistentHost.class).findList();
        for (TPersistentHost host : list) {
            getConversationManager().createConversationHost(host);
        }
    }

    private void registerActionAPI() {

        ActionAPI.register(this).global()
                .trigger(new HostTrigger())
                .action(new AbortConversationActions(), Conversation.class)
                .action(new ShowAnswersAction(), Conversation.class)
                .action(new ChangeStageAction(), Conversation.class)
                .action(new StartConversationAction())
                .action(new EndConversationAction())
                .action(new AbortConversationAction(), Conversation.class)
                .action(new SetVariableAction(), Conversation.class)
                .action(new SetConversationAction())
                .action(new UnsetConversationAction())
                .action(new ClearConversationAction())
                .action(new NpcEmoteAction())
                .requirement(new CompareVariableRequirement(), Conversation.class);
    }

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

        @Setting("debug.trigger.proximity")
        public boolean debug_proximity = true;
        @Setting("debug.trigger.interact")
        public boolean debug_interact = true;
        @Setting("debug.conv.startStage")
        public boolean debug_start = true;
        @Setting("debug.conv.abort")
        public boolean debug_abort = true;
        @Setting("debug.conv.end")
        public boolean debug_end = true;
        @Setting("debug.stage.trigger")
        public boolean debug_stage_trigger = true;
        @Setting("debug.stage.change")
        public boolean debug_stage_change = true;

        @Setting("talk-close.distance")
        public int talkCloseDistance = 3;
        @Setting("talk-close.cooldown")
        @Comment("cooldown in seconds to trigger talk close")
        public double talkCloseCooldown = 30;
        @Setting("talk-close.interval")
        @Comment("Interval in ticks to check talk close npcs.")
        public long talkCloseTaskInterval = 20L;

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
