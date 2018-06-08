package de.raidcraft.conversations.stages;

import de.raidcraft.api.conversations.answer.Answer;
import de.raidcraft.api.conversations.conversation.ConversationTemplate;
import de.raidcraft.api.conversations.stage.AbstractStageTemplate;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author mdoering
 */
public class DynamicStageTemplate extends AbstractStageTemplate {

    public DynamicStageTemplate(ConversationTemplate conversationTemplate, String text, Answer... answers) {

        super("UNKNOWN", conversationTemplate);
        setText(text);
        for (Answer answer : answers) {
            getAnswers().add(answer);
        }
    }

    @Override
    public void loadConfig(ConfigurationSection config) {
    }
}
