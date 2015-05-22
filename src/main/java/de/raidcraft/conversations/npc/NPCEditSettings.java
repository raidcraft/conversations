package de.raidcraft.conversations.npc;

/**
 * Data Class for editing an NPC in admin mode.
 *
 * @author Philip Urban
 */
public class NPCEditSettings {

    private String newConversation;

    public NPCEditSettings(String newConversation) {

        this.newConversation = newConversation;
    }

    public String getNewConversation() {

        return newConversation;
    }
}
