package de.raidcraft.conversations.conversations;

import org.bukkit.configuration.ConfigurationSection;

/**
 * @author mdoering
 */
public class DefaultConversationTemplate extends ConfiguredConversationTemplate {

    public DefaultConversationTemplate(String identifier, ConfigurationSection config) {

        super(identifier, config);
    }

    @Override
    protected void load(ConfigurationSection args) {

    }
}
