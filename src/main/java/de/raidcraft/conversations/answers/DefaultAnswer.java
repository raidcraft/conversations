package de.raidcraft.conversations.answers;

import de.raidcraft.api.conversations.answer.ConfiguredAnswer;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author mdoering
 */
public class DefaultAnswer extends ConfiguredAnswer {

    public DefaultAnswer(String type, ConfigurationSection config) {

        super(type, config);
    }

    @Override
    protected void load(ConfigurationSection args) {

    }
}
