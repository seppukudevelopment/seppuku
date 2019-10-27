package me.rigamortis.seppuku.impl.module.misc;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventReceivePacket;
import me.rigamortis.seppuku.api.module.Module;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketEffect;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.SoundCategory;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 8/11/2019 @ 1:15 AM.
 */
public final class NoGlobalSoundsModule extends Module {

    public NoGlobalSoundsModule() {
        super("NoGlobalSounds", new String[]{"NoSounds", "AntiGlobalSounds"}, "Prevents loud global sounds from playing client-side", "NONE", -1, ModuleType.MISC);
    }

    @Listener
    public void receivePacket(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() instanceof SPacketSoundEffect) {
                final SPacketSoundEffect packet = (SPacketSoundEffect) event.getPacket();
                if (packet.getCategory() == SoundCategory.WEATHER && packet.getSound() == SoundEvents.ENTITY_LIGHTNING_THUNDER) {
                    event.setCanceled(true);
                }
            }

            if (event.getPacket() instanceof SPacketEffect) {
                final SPacketEffect packet = (SPacketEffect) event.getPacket();

                if (packet.getSoundType() == 1038 || packet.getSoundType() == 1023 || packet.getSoundType() == 1028) {
                    event.setCanceled(true);
                }
            }
        }
    }

}
