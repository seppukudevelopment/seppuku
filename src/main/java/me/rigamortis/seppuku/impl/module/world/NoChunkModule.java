package me.rigamortis.seppuku.impl.module.world;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventReceivePacket;
import me.rigamortis.seppuku.api.module.Module;
import net.minecraft.network.play.server.SPacketChunkData;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 6/2/2019 @ 1:30 PM.
 */
public final class NoChunkModule extends Module {

    public NoChunkModule() {
        super("NoChunk", new String[]{"AntiChunk"}, "Prevents processing of chunk data packets", "NONE", -1, ModuleType.WORLD);
    }

    @Listener
    public void onReceivePacket(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() instanceof SPacketChunkData) {
                event.setCanceled(true);
            }
        }
    }

}
