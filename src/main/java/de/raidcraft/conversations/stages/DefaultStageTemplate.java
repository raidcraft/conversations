package de.raidcraft.conversations.stages;

import lombok.EqualsAndHashCode;

/**
 * @author mdoering
 */
@EqualsAndHashCode(callSuper = true)
public class DefaultStageTemplate extends ConfiguredStageTemplate {

    public DefaultStageTemplate(String identifier) {

        super(identifier);
    }
}
