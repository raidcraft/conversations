package de.raidcraft.conversations.answers;

import de.raidcraft.api.action.ActionAPI;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author mdoering
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ConfiguredAnswer extends SimpleAnswer {

    public ConfiguredAnswer(ConfigurationSection config) {

        super(config.getString("text"), ActionAPI.createActions(config.getConfigurationSection("actions")));
    }
}
