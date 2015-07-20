package de.raidcraft.conversations.stages;

import de.raidcraft.api.action.action.Action;
import de.raidcraft.api.action.requirement.Requirement;
import de.raidcraft.api.conversations.answer.Answer;
import de.raidcraft.api.conversations.conversation.Conversation;
import de.raidcraft.api.conversations.conversation.ConversationTemplate;
import de.raidcraft.api.conversations.stage.Stage;
import de.raidcraft.api.conversations.stage.StageTemplate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author mdoering
 */
@Data
@EqualsAndHashCode(callSuper = false, of = {"identifier", "conversationTemplate"})
public abstract class AbstractStageTemplate implements StageTemplate {

    private final String identifier;
    private final ConversationTemplate conversationTemplate;
    @Getter
    private Optional<String[]> text;
    protected List<Requirement<?>> requirements = new ArrayList<>();
    protected List<Action<?>> actions = new ArrayList<>();
    protected List<Action<?>> randomActions = new ArrayList<>();
    protected List<Answer> answers = new ArrayList<>();
    protected boolean autoShowingAnswers = false;

    public AbstractStageTemplate(String identifier, ConversationTemplate conversationTemplate) {

        this.identifier = identifier;
        this.conversationTemplate = conversationTemplate;
    }

    protected void setText(String text) {

        if (text == null) {
            this.text = Optional.empty();
        } else {
            this.text = Optional.of(text.split("\\|"));
        }
    }

    protected void setText(String... text) {

        this.text = Optional.ofNullable(text);
    }

    @Override
    public Stage create(Conversation conversation) {

        return new SimpleStage(conversation, this);
    }
}
