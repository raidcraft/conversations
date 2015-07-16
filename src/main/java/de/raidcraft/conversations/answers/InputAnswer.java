package de.raidcraft.conversations.answers;

import de.raidcraft.api.conversations.answer.ConfiguredAnswer;
import de.raidcraft.api.conversations.conversation.Conversation;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author mdoering
 */
public class InputAnswer extends ConfiguredAnswer {

    private final String varName;

    public InputAnswer(String type, ConfigurationSection config) {

        super(type, config);
        this.varName = config.getString("var", "input");
    }

    @Override
    protected void load(ConfigurationSection args) {

    }

    @Override
    public boolean processInput(Conversation conversation, String input) {

        conversation.set(varName, input);
        conversation.setLastInput(input);
        return true;
    }
}
