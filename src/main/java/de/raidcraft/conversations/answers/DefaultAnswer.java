package de.raidcraft.conversations.answers;

import org.bukkit.configuration.ConfigurationSection;

/**
 * @author mdoering
 */
public class DefaultAnswer extends ConfiguredAnswer {

    public DefaultAnswer(ConfigurationSection config) {

        super(config);
    }

    @Override
    protected void load(ConfigurationSection args) {

    }
}
