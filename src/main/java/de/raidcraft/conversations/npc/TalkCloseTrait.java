package de.raidcraft.conversations.npc;

import de.raidcraft.api.npc.RC_Traits;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dragonfire
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TalkCloseTrait extends Trait {

    private static List<NPC> talkCloseNpcs = new ArrayList<>();

    private int radius = 5;

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
