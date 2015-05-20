package de.raidcraft.conversations.stages;

import de.raidcraft.api.conversations.Conversations;
import de.raidcraft.api.conversations.answer.Answer;
import de.raidcraft.api.conversations.conversation.ConversationTemplate;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mdoering
 */
public class DynamicStage extends ConfiguredStageTemplate {

    public DynamicStage(String identifier, ConversationTemplate conversationTemplate, ConfigurationSection config) {

        super(identifier, conversationTemplate, config);
    }

    @Override
    protected void load(ConfigurationSection args) {


    }

    @Override
    protected List<Answer> loadAnswers(ConfigurationSection config) {

        List<Answer> answers = new ArrayList<>();
        Answer answer = Conversations.getAnswer("Bla");
        answers.add(answer);

        return answers;
    }
}
