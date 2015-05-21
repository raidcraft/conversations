package de.raidcraft.conversations.answers;

import de.raidcraft.api.conversations.conversation.Conversation;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author mdoering
 */
public class InputAnswer extends ConfiguredAnswer {

    private final String varName;

    public InputAnswer(String type, ConfigurationSection config) {

        super(type, config);
        this.varName = config.getString("var", "var");
    }

    @Override
    protected void load(ConfigurationSection args) {

    }

    @Override
    public void processInput(Conversation conversation, String input) {

        conversation.set(varName, input);
    }
}
