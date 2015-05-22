package de.raidcraft.conversations.npc;

import de.raidcraft.api.npc.RC_Traits;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dragonfire
 */
public class TalkCloseTrait extends Trait {

    private static List<NPC> talkCloseNpcs = new ArrayList<>();

    public TalkCloseTrait() {

        super(RC_Traits.TALK_CLOSE);
    }

    public static List<NPC> getNPCs() {

        return talkCloseNpcs;
    }

    @Override
    public void onSpawn() {

        talkCloseNpcs.add(getNPC());
        TalkCloseTask.getInstance().regenerateAllTalkChunks();
    }

    @Override
    public void onDespawn() {

        talkCloseNpcs.remove(getNPC());
        TalkCloseTask.getInstance().regenerateAllTalkChunks();
    }
}
