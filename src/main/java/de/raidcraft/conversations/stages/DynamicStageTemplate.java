package de.raidcraft.conversations.stages;

import de.raidcraft.api.conversations.answer.Answer;
import de.raidcraft.api.conversations.stage.AbstractStageTemplate;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author mdoering
 */
public class DynamicStageTemplate extends AbstractStageTemplate {

    public DynamicStageTemplate(String text, Answer... answers) {

        super("UNKNOWN");
        setText(text);
        for (Answer answer : answers) {
            getAnswers().add(answer);
        }
    }

    @Override
    public void loadConfig(ConfigurationSection config) {
    }
}
