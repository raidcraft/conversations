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

import java.util.*;
import java.util.stream.Collectors;

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

        new ArrayList<>(TalkCloseTrait.getNPCs()).forEach(this::generateTalkChunk);
        taskid = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, -1, plugin.getConfiguration().talkCloseTaskInterval);
    }

    private ConversationHost getNearestConversationHost(Player player, Collection<ConversationHost> validHosts) {

        double currentRange = Double.MAX_VALUE;
        ConversationHost nearest_npc = null;
        double tmpDistance = -1;
        UUID lastNpcTalk = playerTalkedMap.get(player.getUniqueId());
        for (ConversationHost npc : this.talkChunks.get(new ChunkLocation(player.getLocation())).stream()
                .map(npc -> plugin.getConversationManager().getConversationHost(npc).orElse(null))
                .filter(Objects::nonNull)
                .filter(npcConversationHost -> npcConversationHost.hasTrait(TalkCloseTrait.class))
                .collect(Collectors.toList())) {
            if (lastNpcTalk == npc.getUniqueId() || !validHosts.contains(npc)) {
                continue;
            }
            Optional<TalkCloseTrait> trait = npc.getTrait(TalkCloseTrait.class);

            if (!LocationUtil.isWithinRadius(player.getLocation(), npc.getLocation(), trait.get().getRadius())) {
                continue;
            }

            tmpDistance = npc.getLocation().distance(player.getLocation());
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
            // if no npc in your current chunk
            if (!talkChunks.containsKey(new ChunkLocation(player.getLocation().getChunk()))) {
                continue;
            }
            // lets fire our host proximity events
            List<ConversationHost> hosts = plugin.getConversationManager().getNearbyHosts(player.getLocation(), plugin.getConfiguration().maxHostProximityRange);
            List<ConversationHost> validHosts = new ArrayList<>();
            for (ConversationHost host : hosts) {
                Optional<String> identifier = host.getIdentifier();
                if (identifier.isPresent()) {
                    ConversationHostProximityEvent event = new ConversationHostProximityEvent(
                            identifier.get(),
                            host,
                            LocationUtil.getBlockDistance(player.getLocation(), host.getLocation()),
                            player);
                    RaidCraft.callEvent(event);
                    if (!event.isCancelled()) {
                        validHosts.add(host);
                    }
                }
            }

            Optional<Conversation> activeConversation = conversationManager.getActiveConversation(player);
            if (activeConversation.isPresent()) {
                playerTalkedMap.put(player.getUniqueId(), activeConversation.get().getHost().getUniqueId());
                continue;
            }
            // check distance
            ConversationHost nearest_npc = getNearestConversationHost(player, validHosts);
            if (nearest_npc == null) {
                return;
            }
            // talk
            nearest_npc.startConversation(player);
            playerTalkedMap.put(player.getUniqueId(), nearest_npc.getUniqueId());
        }
    }
}
