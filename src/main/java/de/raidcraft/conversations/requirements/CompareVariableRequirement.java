package de.raidcraft.conversations.requirements;

import de.raidcraft.api.action.requirement.Requirement;
import de.raidcraft.api.conversations.conversation.Conversation;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author mdoering
 */
public class CompareVariableRequirement implements Requirement<Conversation> {

    @Override
    @Information(
            value = "variable.compare",
            desc = "Compares the given variable against the given value.",
            conf = {
                    "variable: <identifier>",
                    "value: <expected value>",
                    "operator: < <= == => >"
            }
    )
    public boolean test(Conversation conversation, ConfigurationSection config) {

        String operator = config.getString("operator", "eq");
        String variable = conversation.getString(config.getString("variable"));
        String value = config.getString("value");
        try {
            double compare = Double.parseDouble(variable);
            double expected = Double.parseDouble(value);
            switch (operator) {
                case ">":
                case "gt":
                    return compare > expected;
                case "ge":
                case "=>":
                case ">=":
                    return compare >= expected;
                case "<=":
                case "=<":
                case "le":
                    return compare <= expected;
                case "<":
                case "lt":
                    return compare < expected;
                case "eq":
                case "=":
                case "==":
                default:
                    return compare == expected;
            }
        } catch (NumberFormatException e) {
            return value.equalsIgnoreCase(variable);
        }
    }
}
