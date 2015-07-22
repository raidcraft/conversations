package de.raidcraft.conversations.npc;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.conversations.conversation.Conversation;
import de.raidcraft.api.conversations.events.ConversationHostProximityEvent;
import de.raidcraft.api.conversations.host.ConversationHost;
import de.raidcraft.conversations.ConversationManager;
import de.raidcraft.conversations.RCConversationsPlugin;
import de.raidcraft.util.ChunkLocation;
import de.raidcraft.util.LocationUtil;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * @author Dragonfire
 */
// TODO: only register, unregister task if no player online
public class TalkCloseTask implements Runnable {

    private static TalkCloseTask INSTANCE = null;
    private Map<ChunkLocation, Set<NPC>> talkChunks = new HashMap<>();
    private Map<UUID, UUID> playerTalkedMap = new HashMap<>();
    private RCConversationsPlugin plugin;
    private int taskid = -1;
    // TODO: move to config
    // must be < 16
    private double distance = 5;
    private double talkCloseCooldown = 30;

    private TalkCloseTask() {

        plugin = RaidCraft.getComponent(RCConversationsPlugin.class);
        distance = plugin.getConfiguration().talkCloseDistance;
        talkCloseCooldown = plugin.getConfiguration().talkCloseCooldown;
    }

    public static TalkCloseTask getInstance() {

        if (INSTANCE == null) {
            INSTANCE = new TalkCloseTask();
        }
        return INSTANCE;
    }

    private void generateTalkChunk(NPC npc) {

        ChunkLocation[] locs = this.getChunTalkArea(npc.getEntity().getLocation());
        for (ChunkLocation chunk : locs) {
            if (this.talkChunks.containsKey(chunk)) {
                this.talkChunks.get(chunk).add(npc);
            } else {
                HashSet<NPC> npcs = new HashSet<>();
                npcs.add(npc);
                this.talkChunks.put(chunk, npcs);
            }
        }
    }

    private ChunkLocation[] getChunTalkArea(Location loc) {

        ChunkLocation[] locs = new ChunkLocation[5];
        locs[0] = new ChunkLocation(loc);
        loc.add(distance, 0, 0);
        locs[1] = new ChunkLocation(loc);
        loc.add(-2 * distance, 0, 0);
        locs[2] = new ChunkLocation(loc);
        loc.add(distance, 0, distance);
        locs[3] = new ChunkLocation(loc);
        loc.add(0, 0, -2 * distance);
        locs[3] = new ChunkLocation(loc);
        return locs;
    }

    // usefull for a reload
    // TODO: call this if npc moved
    public void regenerateAllTalkChunks() {

        if (taskid > 0) {
            Bukkit.getScheduler().cancelTask(taskid);
        }
        talkChunks.clear();
        if (TalkCloseTrait.getNPCs().size() == 0) {
            return;
        }
        new ArrayList<>(TalkCloseTrait.getNPCs()).forEach(this::generateTalkChunk);
        taskid = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                plugin, this, -1, 20L * 10);
    }

    private NPC getNPCinRange(Player player) {

        double currentRange = Double.MAX_VALUE;
        NPC nearest_npc = null;
        double tmpDistance = -1;
        UUID lastNpcTalk = playerTalkedMap.get(player.getUniqueId());
        for (NPC npc : this.talkChunks.get(new ChunkLocation(player.getLocation()))) {
            if (lastNpcTalk == npc.getUniqueId()) {
                continue;
            }
            tmpDistance = npc.getEntity().getLocation().distance(player.getLocation());
            if (tmpDistance < currentRange) {
                currentRange = tmpDistance;
                nearest_npc = npc;
            }
        }
        return (currentRange <= distance) ? nearest_npc : null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run() {
        //  iter over all player
        ConversationManager conversationManager = plugin.getConversationManager();
        for (Player player : Bukkit.getOnlinePlayers()) {
            // lets fire our host proximity events
            List<ConversationHost> hosts = plugin.getConversationManager().getNearbyHosts(player.getLocation(), plugin.getConfiguration().maxHostProximityRange);
            for (ConversationHost host : hosts) {
                Optional<String> identifier = host.getIdentifier();
                if (identifier.isPresent()) {
                    RaidCraft.callEvent(new ConversationHostProximityEvent(
                            identifier.get(),
                            host,
                            LocationUtil.getBlockDistance(player.getLocation(), host.getLocation()),
                            player));
                }
            }
            // if no npc in your current chunk
            if (!talkChunks.containsKey(new ChunkLocation(player.getLocation().getChunk()))) {
                continue;
            }
            Optional<Conversation> activeConversation = conversationManager.getActiveConversation(player);
            if (activeConversation.isPresent()) {
                playerTalkedMap.put(player.getUniqueId(), activeConversation.get().getHost().getUniqueId());
                continue;
            }
            // check distance
            NPC nearest_npc = getNPCinRange(player);
            if (nearest_npc == null) {
                return;
            }
            // talk
            Optional<ConversationHost<NPC>> host = conversationManager.getConversationHost(nearest_npc);
            if (host.isPresent() && nearest_npc.hasTrait(TalkCloseTrait.class)) {
                host.get().startConversation(player);
                playerTalkedMap.put(player.getUniqueId(), nearest_npc.getUniqueId());
            }
        }
    }
}
