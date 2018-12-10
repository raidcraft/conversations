package de.raidcraft.conversations.npc;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.disguise.Disguise;
import de.raidcraft.api.npc.RC_Traits;
import de.raidcraft.util.ConfigUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import me.libraryaddict.disguise.DisguiseAPI;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.npc.skin.SkinnableEntity;

import java.util.Optional;

@Data
@EqualsAndHashCode(callSuper = true)
public class DisguiseTrait extends Trait {

    private Disguise disguise;
    private boolean appliedSkin = false;

    public DisguiseTrait() {
        super(RC_Traits.DISGUISE);
    }

    public void setDisguise(Disguise disguise) {
        this.disguise = disguise;
        disguise();
    }

    public Optional<Disguise> getDisguise() {
        return Optional.ofNullable(this.disguise);
    }

    @Override
    public void onSpawn() {

        disguise();
    }

    private void disguise() {

        if (getNPC() == null || !getNPC().isSpawned() || !(getNPC().getEntity() instanceof SkinnableEntity)) {
            return;
        }

        // citizens will respawn the NPC after changing its skin
        // not checking this will cause an endless loop
        if (appliedSkin) return;

        getDisguise().ifPresent(disguise -> {
            appliedSkin = true;
//            disguise.applyToEntity(getNPC().getEntity());
            SkinnableEntity skinnableEntity = (SkinnableEntity) getNPC().getEntity();
            skinnableEntity.setSkinPersistent(disguise.getSkinOwner(), disguise.getSkinSignature(), disguise.getSkinTexture());

//            if (!DisguiseAPI.isDisguised(getNPC().getEntity())) {
//                RaidCraft.LOGGER.warning("Could not disguise " + getNPC().getName() + " with disguise " + getDisguise().orElse(null));
//            }
        });
    }
}
