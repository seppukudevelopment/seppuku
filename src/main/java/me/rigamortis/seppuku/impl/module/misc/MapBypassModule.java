package me.rigamortis.seppuku.impl.module.misc;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.player.EventPlayerUpdate;
import me.rigamortis.seppuku.api.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumHand;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 9/4/2019 @ 4:14 PM.
 */
public final class MapBypassModule extends Module {

    public MapBypassModule() {
        super("MapBypass", new String[] {"MapArtBypass", "MBypass"}, "Helps you create maps on 9b9t.com", "NONE", -1, ModuleType.MISC);
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        if(event.getStage() == EventStageable.EventStage.PRE) {
            final Minecraft mc = Minecraft.getMinecraft();
            for(int i = 0; i < 9; i++) {
                mc.player.connection.sendPacket(new CPacketHeldItemChange(i));
                for(int j = 0; j <= 10; j++) {
                    mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
                }
            }
        }
    }

}
