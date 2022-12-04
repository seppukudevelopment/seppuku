package me.rigamortis.seppuku.impl.module.combat;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.minecraft.EventRunTick;
import me.rigamortis.seppuku.api.event.network.EventReceivePacket;
import me.rigamortis.seppuku.api.event.world.EventRemoveEntity;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.notification.Notification;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketEntityStatus;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.util.ArrayList;
import java.util.List;


/**
 * @author noil
 * @since 12/4/22
 */
public class TotemNotifierModule extends Module {

    final Minecraft mc = Minecraft.getMinecraft();

    public TotemNotifierModule() {
        super("TotemNotifier", new String[]{"tm"}, "Notifies you when others pop totems", "NONE", -1, ModuleType.COMBAT);
    }

    /*
    entityplayermp.addStat(StatList.getObjectUseStats(Items.TOTEM_OF_UNDYING));
    CriteriaTriggers.USED_TOTEM.trigger(entityplayermp, itemstack);
    */

    @Listener
    public void receivePacket(EventReceivePacket event) {
        if (event.getStage().equals(EventStageable.EventStage.PRE) && event.getPacket() instanceof SPacketEntityStatus && mc.world != null) {
            final SPacketEntityStatus packetEntityStatus = (SPacketEntityStatus) event.getPacket();
            if (packetEntityStatus.getOpCode() == 35) { // totem pop status
                Seppuku.INSTANCE.getNotificationManager().addNotification("", packetEntityStatus.getEntity(mc.world).getName() + " just popped a totem.", Notification.Type.INFO, 2000);
            }
        }
    }
}
