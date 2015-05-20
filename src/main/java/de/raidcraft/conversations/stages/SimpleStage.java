package de.raidcraft.conversations.stages;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.action.action.Action;
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
import java.util.List;
import java.util.Optional;

/**
 * @author mdoering
 */
@Data
public class SimpleStage implements Stage {

    private final Conversation conversation;
    private final StageTemplate template;
    private final List<List<Answer>> answers = new ArrayList<>();
    private int currentPage = 0;

    public SimpleStage(Conversation conversation, StageTemplate template) {

        this.conversation = conversation;
        this.template = template;
        template.getAnswers().forEach(this::addAnswer);
    }

    @Override
    public Collection<Action<?>> getActions() {

        return getTemplate().getActions();
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
    public Optional<Answer> getAnswer(String input) {

        try {
            if (input == null || input.equals("")) return Optional.empty();
            int id = Integer.parseInt(input.trim()) - 1;
            if (currentPage < this.answers.size()) {
                List<Answer> answers = this.answers.get(currentPage);
                if (id < answers.size()) {
                    return Optional.of(answers.get(id));
                }
            }
        } catch (NumberFormatException e) {
            getConversation().sendMessage(ChatColor.RED + "Bitte gebe eine Zahl ein oder klicke auf die Antwort.");
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

        if (getText().isPresent()) {
            getConversation().sendMessage(getText().get());
        }
        RCConversationStageTriggeredEvent event = new RCConversationStageTriggeredEvent(getConversation(), this);
        if (this.answers.isEmpty()) {
            RaidCraft.callEvent(event);
            return this;
        }
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
        RaidCraft.callEvent(event);
        return this;
    }

    @Override
    public Stage trigger(boolean executeActions) {

        trigger();
        if (executeActions) {
            getActions(getConversation().getEntity().getClass()).forEach(action -> action.accept(getConversation().getEntity()));
        }
        return this;
    }
}
