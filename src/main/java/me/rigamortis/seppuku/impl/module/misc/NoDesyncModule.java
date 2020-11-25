package me.rigamortis.seppuku.impl.module.misc;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventReceivePacket;
import me.rigamortis.seppuku.api.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.SoundCategory;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 6/6/2019 @ 3:12 AM.
 */
public final class NoDesyncModule extends Module {

    public NoDesyncModule() {
        super("NoDesync", new String[]{"NoDes", "AntiDesync"}, "Prevents the client from desyncing in some situations", "NONE", -1, ModuleType.MISC);
    }

    @Override
    public void onToggle() {
        super.onToggle();
    }

    @Listener
    public void receivePacket(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() instanceof SPacketSoundEffect) {
                final SPacketSoundEffect packet = (SPacketSoundEffect) event.getPacket();
                if (packet.getCategory() == SoundCategory.BLOCKS && packet.getSound() == SoundEvents.ENTITY_GENERIC_EXPLODE) {
                    final Minecraft mc = Minecraft.getMinecraft();
                    if (mc.world != null) {
                        for (int i = mc.world.loadedEntityList.size() - 1; i > 0; i--) {
                            Entity entity = mc.world.loadedEntityList.get(i);
                            if (entity != null) {
                                if (entity.isEntityAlive() && entity instanceof EntityEnderCrystal) {
                                    if (entity.getDistance(packet.getX(), packet.getY(), packet.getZ()) <= 6.0f) {
                                        entity.setDead();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
