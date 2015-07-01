package de.raidcraft.conversations.actions;

import de.raidcraft.api.action.action.Action;
import de.raidcraft.api.conversations.conversation.Conversation;
import de.raidcraft.api.conversations.stage.Stage;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Optional;

/**
 * @author mdoering
 */
public class ShowAnswersAction implements Action<Conversation> {

    @Override
    @Information(
            value = "answers.show",
            desc = "Shows the answers of the current stage in the chat."
    )
    @SuppressWarnings("unchecked")
    public void accept(Conversation conversation, ConfigurationSection config) {

        Optional<Stage> currentStage = (Optional<Stage>) conversation.getCurrentStage();
        if (currentStage.isPresent()) {
            currentStage.get().showAnswers();
        }
    }
}
