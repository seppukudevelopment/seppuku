package me.rigamortis.seppuku.impl.module.misc;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.player.EventPlayerUpdate;
import me.rigamortis.seppuku.api.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.util.EnumHand;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 5/8/2019 @ 12:39 AM.
 */
public final class BreedModule extends Module {

    public BreedModule() {
        super("Breed", new String[] {"bred"}, "Automatically breeds nearby animals if holding the correct breeding item", "NONE", -1, ModuleType.MISC);
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            final Minecraft mc = Minecraft.getMinecraft();
            for(Entity e : mc.world.loadedEntityList) {
                if(e != null && e instanceof EntityAnimal) {
                    final EntityAnimal animal = (EntityAnimal) e;
                    if(animal.getHealth() > 0) {
                        if (!animal.isChild() && !animal.isInLove() && mc.player.getDistance(animal) <= 4.5f && animal.isBreedingItem(mc.player.inventory.getCurrentItem())) {
                            mc.playerController.interactWithEntity(mc.player, animal, EnumHand.MAIN_HAND);
                        }
                    }
                }
            }
        }
    }

}
