package de.raidcraft.conversations.stages;

import de.raidcraft.api.conversations.answer.Answer;
import de.raidcraft.api.conversations.conversation.ConversationTemplate;

/**
 * @author mdoering
 */
public class DynamicStage extends AbstractStageTemplate {

    public DynamicStage(ConversationTemplate conversationTemplate, String text, Answer... answers) {

        super("UNKNOWN", conversationTemplate);
        setText(text);
        for (Answer answer : answers) {
            getAnswers().add(answer);
        }
    }
}
