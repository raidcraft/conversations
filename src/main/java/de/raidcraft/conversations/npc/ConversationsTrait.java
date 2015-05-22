package de.raidcraft.conversations.npc;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.conversations.conversation.ConversationTemplate;
import de.raidcraft.api.items.CustomItemException;
import de.raidcraft.api.npc.RC_Traits;
import de.raidcraft.conversations.RCConversationsPlugin;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.Equipment;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.List;
import java.util.Optional;

/**
 * Author: Philip, Dragonfire
 */
public class ConversationsTrait extends Trait {

    @Persist
    private String conversationName;

    public ConversationsTrait() {

        super(RC_Traits.CONSERVATION);
    }

    public void setConversationName(String conversationName) {

        this.conversationName = conversationName;
    }

    public String getConversationName() {

        return conversationName;
    }

    @Override
    public void onSpawn() {
        // hotfix: config is delayed loaded, because not all actions are registred
        // delay npc setup
        RCConversationsPlugin plugin = RaidCraft.getComponent(RCConversationsPlugin.class);
        Optional<ConversationTemplate> template = plugin.getConversationManager().getLoadedConversationTemplate(getConversationName());
        if (!template.isPresent()) {
            return;
        }
        ConfigurationSection settings = template.get().getHostSettings();
        // add metakeys
        if (getNPC().getEntity() != null) {
            List<String> metakeys = settings.getStringList("entity-metakeys");
            for (String meta : metakeys) {
                getNPC().getEntity().setMetadata(meta, new FixedMetadataValue(plugin, true));
            }
        }

        if (settings.getBoolean("talk-close", false)) {
            getNPC().addTrait(TalkCloseTrait.class);
            getNPC().getTrait(TalkCloseTrait.class).linkToNPC(getNPC());
        }

        ConfigurationSection equipment = settings.getConfigurationSection("npc-equipment");
        Equipment equipmentTrait = getNPC().getTrait(Equipment.class);
        if (equipment != null && equipmentTrait != null) {
            String itemName;
            try {
                itemName = equipment.getString("hand");
                if (itemName != null && !itemName.equals("")) {
                    equipmentTrait.set(0, RaidCraft.getItem(itemName));
                }
            } catch (CustomItemException e) {
                e.printStackTrace();
            }

            try {
                itemName = equipment.getString("helmet");
                if (itemName != null && !itemName.equals("")) {
                    equipmentTrait.set(1, RaidCraft.getItem(itemName));
                }
            } catch (CustomItemException e) {
                e.printStackTrace();
            }

            try {
                itemName = equipment.getString("chestplate");
                if (itemName != null && !itemName.equals("")) {
                    equipmentTrait.set(2, RaidCraft.getItem(itemName));
                }
            } catch (CustomItemException e) {
                e.printStackTrace();
            }

            try {
                itemName = equipment.getString("leggings");
                if (itemName != null && !itemName.equals("")) {
                    equipmentTrait.set(3, RaidCraft.getItem(itemName));
                }
            } catch (CustomItemException e) {
                e.printStackTrace();
            }

            try {
                itemName = equipment.getString("boots");
                if (itemName != null && !itemName.equals("")) {
                    equipmentTrait.set(4, RaidCraft.getItem(itemName));
                }
            } catch (CustomItemException e) {
                e.printStackTrace();
            }
        }
    }
}
