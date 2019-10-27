package me.rigamortis.seppuku.impl.module.misc;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.player.EventPlayerUpdate;
import me.rigamortis.seppuku.api.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemDye;
import net.minecraft.util.EnumHand;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 5/8/2019 @ 12:49 AM.
 */
public final class DyeModule extends Module {

    public DyeModule() {
        super("Dye", new String[] {"dy"}, "Automatically dyes nearby sheep if holding a dye", "NONE", -1, ModuleType.MISC);
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            final Minecraft mc = Minecraft.getMinecraft();
            if(mc.player.inventory.getCurrentItem().getItem() instanceof ItemDye) {
                final EnumDyeColor color = EnumDyeColor.byDyeDamage(mc.player.inventory.getCurrentItem().getMetadata());

                for(Entity e : mc.world.loadedEntityList) {
                    if(e != null && e instanceof EntitySheep) {
                        final EntitySheep sheep = (EntitySheep) e;
                        if(sheep.getHealth() > 0) {
                            if (sheep.getFleeceColor() != color && !sheep.getSheared() && mc.player.getDistance(sheep) <= 4.5f) {
                                mc.playerController.interactWithEntity(mc.player, sheep, EnumHand.MAIN_HAND);
                            }
                        }
                    }
                }
            }
        }
    }

}
