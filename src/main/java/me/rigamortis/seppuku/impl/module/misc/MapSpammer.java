package me.rigamortis.seppuku.impl.module.misc;

import me.rigamortis.seppuku.api.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/*
Author sn01
11/12/2020
Atlas module adapted for Seppuku
 */



public final class MapSpammer extends Module {


    public MapSpammer() {
    super("MapSpam", new String[]{"MapBypass"}, "Spam maps on 9b9t.com", "NONE", -1, ModuleType.MISC);
    }


    @Listener
    public void onUpdate() {
        final Minecraft mc = Minecraft.getMinecraft();
        mc.player.connection.sendPacket(new CPacketHeldItemChange(1));
        mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(mc.player.getPosition().down(), EnumFacing.DOWN, EnumHand.MAIN_HAND, 0.0F, 0.0F, 0.0F));
        mc.player.connection.sendPacket(new CPacketHeldItemChange(0));
        mc.player.connection.sendPacket (new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));

        for (int i = 0; i <= 44; ++i) {
            if (mc.player.inventoryContainer.getSlot(i).getStack().getItem() == Items.FILLED_MAP) {
                mc.playerController.windowClick(mc.player.openContainer.windowId, i, 0, ClickType.THROW, (EntityPlayer) mc.player);
                mc.player.inventory.removeStackFromSlot(i);
            }
        }
    }
}


