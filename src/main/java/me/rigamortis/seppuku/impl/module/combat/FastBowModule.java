package me.rigamortis.seppuku.impl.module.combat;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.player.EventUpdateWalkingPlayer;
import me.rigamortis.seppuku.api.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemBow;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.math.BlockPos;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 4/30/2019 @ 4:27 AM.
 */
public final class FastBowModule extends Module {

    public FastBowModule() {
        super("FastBow", new String[]{"FBow"}, "Releases the bow as fast as possible", "NONE", -1, ModuleType.COMBAT);
    }

    @Listener
    public void onWalkingUpdate(EventUpdateWalkingPlayer event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            final Minecraft mc = Minecraft.getMinecraft();

            if (mc.player.inventory.getCurrentItem().getItem() instanceof ItemBow) {
                if (mc.player.isHandActive() && mc.player.getItemInUseMaxCount() >= 3) {
                    mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, mc.player.getHorizontalFacing()));
                    mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(mc.player.getActiveHand()));
                    mc.player.stopActiveHand();
                }
            }
        }
    }

}
