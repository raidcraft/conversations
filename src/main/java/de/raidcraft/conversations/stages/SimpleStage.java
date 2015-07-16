package de.raidcraft.conversations.stages;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.action.action.Action;
import de.raidcraft.api.action.action.ActionHolder;
import de.raidcraft.api.action.requirement.Requirement;
import de.raidcraft.api.conversations.answer.Answer;
import de.raidcraft.api.conversations.conversation.Conversation;
import de.raidcraft.api.conversations.events.RCConversationStageTriggeredEvent;
import de.raidcraft.api.conversations.stage.Stage;
import de.raidcraft.api.conversations.stage.StageTemplate;
import lombok.Data;
import mkremins.fanciful.FancyMessage;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author mdoering
 */
@Data
public class SimpleStage implements Stage {

    private final Conversation conversation;
    private final StageTemplate template;
    private final List<List<Answer>> answers = new ArrayList<>();
    private int currentPage = 0;
    private boolean abortActions = false;

    public SimpleStage(Conversation conversation, StageTemplate template) {

        this.conversation = conversation;
        this.template = template;
        template.getAnswers().forEach(this::addAnswer);
    }

    @Override
    public void abortActionExecution() {

        abortActions = true;
        getAnswers().forEach(Answer::abortActionExecution);
    }

    @Override
    public Collection<Requirement<?>> getRequirements() {

        return getTemplate().getRequirements();
    }

    @Override
    public Collection<Action<?>> getActions() {

        return getTemplate().getActions();
    }

    @Override
    public List<Action<?>> getRandomActions() {

        return getTemplate().getRandomActions();
    }

    @Override
    public List<Answer> getAnswers() {

        return answers.stream().flatMap(List::stream).collect(Collectors.toList());
    }

    @Override
    public Stage clearAnswers() {

        answers.clear();
        return this;
    }

    @Override
    public Stage addAnswer(Answer answer) {

        List<Answer> answers = new ArrayList<>();
        if (!this.answers.isEmpty()) {
            answers = this.answers.remove(this.answers.size() - 1);
        }
        if (answers.size() < StageTemplate.MAX_ANSWERS) {
            answers.add(answer);
        }
        this.answers.add(answers);
        return this;
    }

    @Override
    public Stage showAnswers() {

        while (currentPage >= this.answers.size()) currentPage--;

        List<Answer> answers = this.answers.get(currentPage);
        int i;
        for (i = 0; i < answers.size(); i++) {
            Answer answer = answers.get(i);
            FancyMessage message;
            if (answer.getMessage().isPresent()) {
                message = answer.getMessage().get();
            } else {
                message = new FancyMessage(i + 1 + ": ").color(ChatColor.AQUA)
                        .then(answer.getText()).color(answer.getColor());
            }
            getConversation().sendMessage(message.command("/conversations answer " + (i + 1)));
        }
        if (this.answers.size() > 1) {
            if (currentPage > 1) {
                getConversation().sendMessage(new FancyMessage(i + 1 + ": ").color(ChatColor.AQUA)
                        .then("Zurück zu Seite " + (currentPage)).color(ChatColor.GRAY)
                        .command("/conversations page " + (currentPage - 1)));
                i++;
            }
            if (currentPage > 1 && currentPage < this.answers.size() - 1) {
                getConversation().sendMessage(new FancyMessage(i + 1 + ": ").color(ChatColor.AQUA)
                        .then("Nächste Seite " + (currentPage + 1)).color(ChatColor.GRAY)
                        .command("/conversations page " + (currentPage + 1)));
            }
        }
        return this;
    }

    @Override
    public Optional<Answer> getAnswer(String input) {

        try {
            if (input == null || input.equals("")) return Optional.empty();
            for (Answer answer : this.answers.stream().flatMap(List::stream).collect(Collectors.toList())) {
                if (answer.processInput(getConversation(), input)) {
                    return Optional.of(answer);
                }
            }
            // if not answer processed the input we will try to select by the id
            int id = Integer.parseInt(input.trim()) - 1;
            if (currentPage < this.answers.size()) {
                List<Answer> answers = this.answers.get(currentPage);
                if (id < answers.size()) {
                    return Optional.of(answers.get(id));
                }
            }
        } catch (NumberFormatException e) {
            getConversation().sendMessage(ChatColor.GRAY + "Ich habe deine Antwort nicht verstanden: Bitte gebe eine Zahl ein oder klicke auf die Antwort.");
        }
        return Optional.empty();
    }

    @Override
    public boolean changePage(int page) {

        currentPage = page;
        while (currentPage >= this.answers.size()) currentPage--;
        if (currentPage < 0) {
            currentPage = 0;
            return false;
        }
        trigger();
        return currentPage == page;
    }

    @Override
    public Stage trigger() {

        return trigger(true);
    }

    @Override
    public Stage trigger(boolean executeActions) {

        if (getText().isPresent()) {
            getConversation().sendMessage(getText().get());
        }
        RCConversationStageTriggeredEvent event = new RCConversationStageTriggeredEvent(getConversation(), this);
        if (!this.answers.isEmpty() && getTemplate().isAutoShowingAnswers()) {
            showAnswers();
        }
        RaidCraft.callEvent(event);

        if (executeActions) {
            abortActions = false;
            // lets execute all player actions and then all conversation actions
            for (Action<Object> action : getActions(Conversation.class)) {
                if (abortActions) break;
                action.accept(getConversation());
            }
            for (Action<Object> action : getActions(getConversation().getOwner().getClass())) {
                if (abortActions) break;
                action.accept(getConversation().getOwner());
            }

            List<Action<Object>> randomActions = ActionHolder.getFilteredActions(getRandomActions(), getConversation().getOwner().getClass());
            Collections.shuffle(randomActions);
            Optional<Action<Object>> any = randomActions.stream().findAny();
            if (!abortActions && any.isPresent()) {
                any.get().accept(getConversation().getOwner());
            }
        }
        return this;
    }
}
