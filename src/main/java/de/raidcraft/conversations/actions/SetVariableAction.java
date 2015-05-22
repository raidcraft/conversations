package de.raidcraft.conversations.actions;

import de.raidcraft.api.action.action.Action;
import de.raidcraft.api.conversations.conversation.Conversation;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

/**
 * @author mdoering
 */
@RequiredArgsConstructor
public class SetVariableAction implements Action<Conversation> {

    private final String variable;
    private final Object value;
    private final boolean local;

    public SetVariableAction() {

        variable = null;
        value = null;
        local = true;
    }

    @Override
    public void accept(Conversation type) {

        MemoryConfiguration config = new MemoryConfiguration();
        config.set("variable", variable);
        config.set("value", value);
        config.set("local", local);
        accept(type, config);
    }

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
