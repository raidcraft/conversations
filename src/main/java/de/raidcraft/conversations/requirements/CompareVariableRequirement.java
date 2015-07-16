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
                    "operator: < <= == => > !=",
                    "value: <expected value>"
            }
    )
    public boolean test(Conversation conversation, ConfigurationSection config) {

        String operator = config.getString("operator", "eq");
        Object variable = conversation.get(config.getString("variable"));
        if (variable instanceof Double || variable instanceof Integer) {
            double compare = Double.parseDouble(variable.toString());
            double expected = config.getDouble("value");
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
        } else if (variable instanceof Boolean) {
            boolean var = (Boolean) variable;
            switch (operator) {
                case "==":
                    return var == config.getBoolean("value");
                case "!=":
                    return var != config.getBoolean("value");
            }
        } else if (variable instanceof String) {
            String var = (String) variable;
            switch (operator) {
                case "==":
                    return var.equalsIgnoreCase(config.getString("value"));
                case "!=":
                    return !var.equalsIgnoreCase(config.getString("value"));
            }
        }
        return false;
    }
}
