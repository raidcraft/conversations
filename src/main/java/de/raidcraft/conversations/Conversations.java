package de.raidcraft.conversations;

import de.raidcraft.api.BasePlugin;

/**
 * @author mdoering
 */
public class Conversations {

    public <T extends BasePlugin> ConversationBuilder createConversation(T plugin, String name) {

        return new ConversationBuilder<>(plugin, name);
    }
}
