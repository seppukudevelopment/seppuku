package me.rigamortis.seppuku.impl.module.misc;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.player.EventPlayerUpdate;
import me.rigamortis.seppuku.api.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.item.ItemShears;
import net.minecraft.util.EnumHand;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 5/8/2019 @ 12:46 AM.
 */
public final class ShearModule extends Module {

    public ShearModule() {
        super("Shear", new String[] {"sher"}, "Automatically shears nearby sheep if holding shears", "NONE", -1, ModuleType.MISC);
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            final Minecraft mc = Minecraft.getMinecraft();
            if(mc.player.inventory.getCurrentItem().getItem() instanceof ItemShears) {
                for(Entity e : mc.world.loadedEntityList) {
                    if(e != null && e instanceof EntitySheep) {
                        final EntitySheep sheep = (EntitySheep) e;
                        if(sheep.getHealth() > 0) {
                            if (!sheep.isChild() && !sheep.getSheared() && mc.player.getDistance(sheep) <= 4.5f) {
                                mc.playerController.interactWithEntity(mc.player, sheep, EnumHand.MAIN_HAND);
                            }
                        }
                    }
                }
            }
        }
    }

}
