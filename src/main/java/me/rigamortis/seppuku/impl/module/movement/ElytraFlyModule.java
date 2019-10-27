package me.rigamortis.seppuku.impl.module.movement;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventReceivePacket;
import me.rigamortis.seppuku.api.event.player.EventUpdateWalkingPlayer;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.MathUtil;
import me.rigamortis.seppuku.api.value.BooleanValue;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.math.Vec3d;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 5/2/2019 @ 12:43 AM.
 */
public final class ElytraFlyModule extends Module {

    //private OptionalValue mode = new OptionalValue("Mode", new String[]{"Mode", "M"}, 0, new String[]{"Vanilla", "NCP"});

    public final BooleanValue infiniteDura = new BooleanValue("InfiniteDurability", new String[]{"InfiniteDura", "dura", "inf", "infdura"}, true);

    public ElytraFlyModule() {
        super("ElytraFly", new String[]{"Elytra"}, "Allows you to fly with elytras", "NONE", -1, ModuleType.MOVEMENT);
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if(Minecraft.getMinecraft().player != null) {
            Minecraft.getMinecraft().player.capabilities.isFlying = false;
        }
    }

    @Listener
    public void onWalkingUpdate(EventUpdateWalkingPlayer event) {
        final Minecraft mc = Minecraft.getMinecraft();

        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() == Items.ELYTRA) {
                if (mc.gameSettings.keyBindJump.isKeyDown()) {
                    mc.player.motionY = 0.02f;
                }

                if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                    mc.player.motionY = -0.2f;
                }

                if(mc.player.ticksExisted % 8 == 0 && mc.player.posY <= 240) {
                    mc.player.motionY = 0.02f;
                }

                mc.player.capabilities.isFlying = true;
                mc.player.capabilities.setFlySpeed(0.025f);

                final double[] dir = MathUtil.directionSpeed(0.52f);

                if(mc.player.movementInput.moveStrafe != 0 || mc.player.movementInput.moveForward != 0) {
                    mc.player.motionX = dir[0];
                    mc.player.motionZ = dir[1];
                }else{
                    mc.player.motionX = 0;
                    mc.player.motionZ = 0;
                }

                if (this.infiniteDura.getBoolean()) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                }
            }
        }
        if (event.getStage() == EventStageable.EventStage.POST) {
            if (mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() == Items.ELYTRA) {
                if (this.infiniteDura.getBoolean()) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                }
            }
        }
    }

    @Listener
    public void receivePacket(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() instanceof SPacketChat) {
                final SPacketChat packet = (SPacketChat) event.getPacket();

                if (packet.getChatComponent().getUnformattedText().equalsIgnoreCase("See that bird? *rips wings off*")) {
                    event.setCanceled(true);
                }

                if (packet.getChatComponent().getUnformattedText().equalsIgnoreCase("You've been flying for a while.")) {
                    event.setCanceled(true);
                }

                if (packet.getChatComponent().getUnformattedText().equalsIgnoreCase("ElytraFly is disabled.")) {
                    event.setCanceled(true);
                }

                if (packet.getChatComponent().getUnformattedText().equalsIgnoreCase("Your wings are safe under the Newfag Assisted Flight Temporal Agreement.")) {
                    event.setCanceled(true);
                }
            }
        }
    }

}
