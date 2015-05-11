package de.raidcraft.conversations.api;

import de.raidcraft.api.action.action.ActionHolder;
import de.raidcraft.api.action.requirement.RequirementHolder;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

/**
 * @author mdoering
 */
public interface Stage extends RequirementHolder<Player>, ActionHolder<Player> {

    public static final String START_STAGE = "start";

    /**
     * Gets the text that is displayed for this stage.
     * If no text is display the optional will be empty.
     *
     * @return display text
     */
    public Optional<String> getText();

    /**
     * Gets the answer based on the players input. If no answer is found
     * an empty optional will be returned.
     *
     * @param input to process
     * @return optional answer
     */
    public Optional<Answer> getAnswer(String input);

    /**
     * Gets all answers of this stage.
     *
     * @return list of answers
     */
    public List<Answer> getAnswers();

    /**
     * Adds the given answer to the stage.
     *
     * @param answer to add
     * @return this stage
     */
    public Stage addAnswer(Answer answer);

    /**
     * Triggers this stage displaying the text and executing all actions.
     *
     * @param conversation to trigger stage for
     * @return triggered stage
     */
    public Stage trigger(Conversation conversation);

    /**
     * Triggers this stage displaying the text and executing all actions.
     *
     * @param conversation to trigger stage for
     * @param executeActions if false no actions will be executed
     * @return triggered stage
     */
    public Stage trigger(Conversation conversation, boolean executeActions);
}
