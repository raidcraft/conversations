package de.raidcraft.conversations.stages;

import de.raidcraft.api.conversations.conversation.ConversationTemplate;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author mdoering
 */
public class DefaultStageTemplate extends ConfiguredStageTemplate {

    public DefaultStageTemplate(String identifier, ConversationTemplate conversationTemplate, ConfigurationSection config) {

        super(identifier, conversationTemplate, config);
    }

    @Override
    protected void load(ConfigurationSection args) {

    }
}
