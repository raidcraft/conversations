package de.raidcraft.conversations.actions;

import de.raidcraft.api.action.action.Action;
import de.raidcraft.api.conversations.conversation.Conversation;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author mdoering
 */
@RequiredArgsConstructor
public class SetVariableAction implements Action<Conversation> {

    @Override
    @Information(
            value = "variable.set",
            desc = "Sets the given variable for the current conversation or globally.",
            conf = {
                    "variable: <identifier>",
                    "value: <value of the variable>",
                    "local: false/true if saving to db for global access"
            }
    )
    public void accept(Conversation conversation, ConfigurationSection config) {

        if (config.getBoolean("local", false)) {
            conversation.set(config.getString("variable"), config.getString("value"));
        } else {
            conversation.setGlobal(config.getString("variable"), config.getString("value"));
        }
    }
}
