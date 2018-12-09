package de.raidcraft.conversations.npc;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.disguise.Disguise;
import de.raidcraft.api.npc.RC_Traits;
import de.raidcraft.util.ConfigUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import me.libraryaddict.disguise.DisguiseAPI;
import net.citizensnpcs.api.trait.Trait;

import java.util.Optional;

@Data
@EqualsAndHashCode(callSuper = true)
public class DisguiseTrait extends Trait {

    private Disguise disguise;

    public DisguiseTrait() {
        super(RC_Traits.DISGUISE);
    }

    public void setDisguise(Disguise disguise) {
        this.disguise = disguise;
        if (disguise != null && getNPC() != null && getNPC().isSpawned()) {
            disguise.applyToEntity(getNPC().getEntity());
        }
    }

    public Optional<Disguise> getDisguise() {
        return Optional.ofNullable(this.disguise);
    }

    @Override
    public void onSpawn() {

        getDisguise().ifPresent(disguise -> disguise.applyToEntity(getNPC().getEntity()));
        if (!DisguiseAPI.isDisguised(getNPC().getEntity())) {
            RaidCraft.LOGGER.warning("Could not disguise " + getNPC().getName() + " with disguise " + getDisguise().orElse(null));
        }
    }
}
