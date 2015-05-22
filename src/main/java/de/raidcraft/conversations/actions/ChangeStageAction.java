package de.raidcraft.conversations.actions;

import de.raidcraft.api.action.action.Action;
import de.raidcraft.api.conversations.conversation.Conversation;
import de.raidcraft.api.conversations.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import java.util.Optional;

/**
 * @author mdoering
 */
@RequiredArgsConstructor
public class ChangeStageAction implements Action<Conversation> {

    private final String stage;

    public ChangeStageAction() {

        stage = null;
    }

    @Override
    public void accept(Conversation type) {

        MemoryConfiguration config = new MemoryConfiguration();
        config.set("stage", stage);
        accept(type, config);
    }

    @Override
    @Information(
            value = "stage",
            desc = "Changes the stage of the conversation.",
            conf = "stage: <identifier>"
    )
    @SuppressWarnings("unchecked")
    public void accept(Conversation conversation, ConfigurationSection config) {

        Optional<Stage> stage = conversation.getStage(config.getString("stage"));
        if (stage.isPresent()) {
            conversation.changeToStage(stage.get());
        }
    }
}
