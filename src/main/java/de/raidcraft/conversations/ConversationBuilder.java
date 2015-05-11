package de.raidcraft.conversations;

import de.raidcraft.api.BasePlugin;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author mdoering
 */
@Data
public class ConversationBuilder<T extends BasePlugin> {

    private final T plugin;
    private final String name;

    public ConversationBuilder(T plugin, String name) {

        this.plugin = plugin;
        this.name = name;
    }

    public Stage withStartStage() {

        return new Stage(de.raidcraft.conversations.api.Stage.START_STAGE, this);
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    private class Stage extends ConversationBuilder<T> {

        private final String name;

        public Stage(String name, ConversationBuilder<T> conversationBuilder) {

            super(conversationBuilder.plugin, conversationBuilder.name);
            this.name = name;
        }
    }
}
