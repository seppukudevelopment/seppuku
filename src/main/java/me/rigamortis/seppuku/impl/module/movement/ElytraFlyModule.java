package me.rigamortis.seppuku.impl.module.movement;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventReceivePacket;
import me.rigamortis.seppuku.api.event.player.EventUpdateWalkingPlayer;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.MathUtil;
import me.rigamortis.seppuku.api.util.Timer;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.server.SPacketChat;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Authors Seth & noil
 * <p>
 * 5/2/2019 @ 12:43 AM.
 */
public final class ElytraFlyModule extends Module {

    public final Value<Mode> mode = new Value<Mode>("Mode", new String[]{"Mode", "M"}, "Mode to use for elytra flight.", Mode.VANILLA);

    private enum Mode {
        VANILLA, PACKET, BYPASS
    }

    public final Value<Float> speed = new Value<Float>("Speed", new String[]{"Spd"}, "Speed multiplier for elytra flight, higher values equals more speed.", 1.0f, 0.0f, 5.0f, 0.01f);

    public final Value<Boolean> autoStart = new Value<Boolean>("AutoStart", new String[]{"AutoStart", "start", "autojump"}, "Hold down the jump key to have an easy automated lift off.", true);
    public final Value<Boolean> disableInLiquid = new Value<Boolean>("DisableInLiquid", new String[]{"DisableInWater", "DisableInLava", "disableliquid", "liquidoff", "noliquid"}, "Disables all elytra flight when the player is in contact with liquid.", true);
    public final Value<Boolean> infiniteDurability = new Value<Boolean>("InfiniteDurability", new String[]{"InfiniteDura", "dura", "inf", "infdura"}, "Enables an old exploit that sends the start elytra-flying packet each tick.", false);
    public final Value<Boolean> noKick = new Value<Boolean>("NoKick", new String[]{"AntiKick", "Kick"}, "Bypass the server kicking you for flying while in elytra flight (Only works for Packet mode!).", true);

    private final Timer timer = new Timer();

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
        if (Minecraft.getMinecraft().player != null) {
            Minecraft.getMinecraft().player.capabilities.isFlying = false;
        }
    }

    @Listener
    public void onWalkingUpdate(EventUpdateWalkingPlayer event) {
        final Minecraft mc = Minecraft.getMinecraft();

        // ensure player has an elytra on before running any code
        if (mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() != Items.ELYTRA)
            return;

        switch (event.getStage()) {
            case PRE:
                // liquid check
                if (this.disableInLiquid.getValue() && (mc.player.isInWater() || mc.player.isInLava())) {
                    if (mc.player.isElytraFlying()) {
                        mc.getConnection().sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                    }
                    return;
                }

                // automatic jump start
                if (this.autoStart.getValue()) {
                    if (mc.gameSettings.keyBindJump.isKeyDown() && !mc.player.isElytraFlying()) { // jump is held, player is not elytra flying
                        if (mc.player.motionY < 0) { // player motion is falling
                            if (this.timer.passed(250)) { // 250 ms
                                mc.getConnection().sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                                this.timer.reset();
                            }
                        }
                    }
                }

                // the player's rotation yaw
                final double rotationYaw = Math.toRadians(mc.player.rotationYaw);

                // ensure the player is in the elytra flying state
                if (mc.player.isElytraFlying()) {
                    switch (this.mode.getValue()) {
                        case VANILLA:
                            final float speedScaled = this.speed.getValue() * 0.05f; // 5/100 of original value

                            if (mc.gameSettings.keyBindJump.isKeyDown()) {
                                mc.player.motionY += speedScaled;
                            }

                            if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                                mc.player.motionY -= speedScaled;
                            }

                            if (mc.gameSettings.keyBindForward.isKeyDown()) {
                                mc.player.motionX -= Math.sin(rotationYaw) * speedScaled;
                                mc.player.motionZ += Math.cos(rotationYaw) * speedScaled;
                            }

                            if (mc.gameSettings.keyBindBack.isKeyDown()) {
                                mc.player.motionX += Math.sin(rotationYaw) * speedScaled;
                                mc.player.motionZ -= Math.cos(rotationYaw) * speedScaled;
                            }
                            break;
                        case PACKET:
                            this.freezePlayer(mc.player);
                            this.runNoKick(mc.player);

                            final double[] directionSpeedPacket = MathUtil.directionSpeed(this.speed.getValue());

                            if (mc.player.movementInput.jump) {
                                mc.player.motionY = this.speed.getValue();
                            }

                            if (mc.player.movementInput.sneak) {
                                mc.player.motionY = -this.speed.getValue();
                            }

                            if (mc.player.movementInput.moveStrafe != 0 || mc.player.movementInput.moveForward != 0) {
                                mc.player.motionX = directionSpeedPacket[0];
                                mc.player.motionZ = directionSpeedPacket[1];
                            }

                            mc.getConnection().sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                            mc.getConnection().sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                            break;
                        case BYPASS: // Bypass / 9b9t
                            if (mc.gameSettings.keyBindJump.isKeyDown()) {
                                mc.player.motionY = 0.02f;
                            }

                            if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                                mc.player.motionY = -0.2f;
                            }

                            if (mc.player.ticksExisted % 8 == 0 && mc.player.posY <= 240) {
                                mc.player.motionY = 0.02f;
                            }

                            mc.player.capabilities.isFlying = true;
                            mc.player.capabilities.setFlySpeed(0.025f);

                            final double[] directionSpeedBypass = MathUtil.directionSpeed(0.52f);

                            if (mc.player.movementInput.moveStrafe != 0 || mc.player.movementInput.moveForward != 0) {
                                mc.player.motionX = directionSpeedBypass[0];
                                mc.player.motionZ = directionSpeedBypass[1];
                            } else {
                                mc.player.motionX = 0;
                                mc.player.motionZ = 0;
                            }
                            break;
                    }
                }

                if (this.infiniteDurability.getValue()) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                }
                break;
            case POST:
                if (this.infiniteDurability.getValue()) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                }
                break;
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

    private void freezePlayer(EntityPlayer player) {
        player.motionX = 0;
        player.motionY = 0;
        player.motionZ = 0;
    }

    private void runNoKick(EntityPlayer player) {
        if (this.noKick.getValue() && !player.isElytraFlying()) {
            if (player.ticksExisted % 4 == 0) {
                player.motionY = -0.04f;
            }
        }
    }
}
