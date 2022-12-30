package me.rigamortis.seppuku.impl.module.player;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventSendPacket;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public class FastProjectile extends Module {
    public FastProjectile() {
        super("FastProjectile", new String[]{"FProj"}, "Arrow Go Zoom", "NONE", -1, ModuleType.PLAYER);
    }

    public final Value<Integer> power = new Value<>("Power", new String[]{"power", "strength", "intensity", "frequency"}, "Speed of fast projectile", 1, 0, 50, 1);
    public final Value<Integer> bowThreshold = new Value<>("Bow Threshold", new String[]{"threshold"}, "How long must bow be charged to initiate fast projectile", 1, 0, 20, 1);

    @Listener
    public void sendPacket(EventSendPacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() instanceof CPacketPlayerDigging) {
                final CPacketPlayerDigging packet = (CPacketPlayerDigging) event.getPacket();
                if (packet.getAction() == CPacketPlayerDigging.Action.RELEASE_USE_ITEM) {
                    doFastProjectile();
                }
            } else if (event.getPacket() instanceof CPacketPlayerTryUseItem) {
                doFastProjectile();
            }
        }
    }

    private void doFastProjectile() {
        final Minecraft mc = Minecraft.getMinecraft();
        Item item = mc.player.inventory.getCurrentItem().getItem();
        if (item.equals(Items.BOW) && mc.player.getItemInUseMaxCount() < bowThreshold.getValue()) return;
        mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
        for (int i = 0; i < (10 * power.getValue()); i++) {
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX + 1.57e-6, mc.player.posY + 1.57e-9, mc.player.posZ + 1.57e-6, false));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX + 1.57e-7, mc.player.posY + 1.57e-10, mc.player.posZ + 1.57e-7, true));

        }
        mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));

    }

}
