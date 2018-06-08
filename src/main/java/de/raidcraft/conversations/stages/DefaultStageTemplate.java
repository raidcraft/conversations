package de.raidcraft.conversations.stages;

import de.raidcraft.api.conversations.conversation.ConversationTemplate;
import lombok.EqualsAndHashCode;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author mdoering
 */
@EqualsAndHashCode(callSuper = true)
public class DefaultStageTemplate extends ConfiguredStageTemplate {

    public DefaultStageTemplate(String identifier, ConversationTemplate conversationTemplate, ConfigurationSection config) {

        super(identifier, conversationTemplate);
    }
}
