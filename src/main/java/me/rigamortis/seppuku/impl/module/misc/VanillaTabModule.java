package me.rigamortis.seppuku.impl.module.misc;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventReceivePacket;
import me.rigamortis.seppuku.api.module.Module;
import net.minecraft.network.play.server.SPacketPlayerListHeaderFooter;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 8/12/2019 @ 1:59 AM.
 */
public final class VanillaTabModule extends Module {

    public VanillaTabModule() {
        super("VanillaTab", new String[]{"VTab", "VanillaT"}, "Removes the Header and Footer from the tab menu.", "NONE", -1, ModuleType.MISC);
    }

    @Listener
    public void recievePacket(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if(event.getPacket() instanceof SPacketPlayerListHeaderFooter) {
                event.setCanceled(true);
            }
        }
    }

}
