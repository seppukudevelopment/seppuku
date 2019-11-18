package me.rigamortis.seppuku.impl.module.movement;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventReceivePacket;
import me.rigamortis.seppuku.api.event.player.EventUpdateWalkingPlayer;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.MathUtil;
import me.rigamortis.seppuku.api.util.Timer;
import me.rigamortis.seppuku.api.value.BooleanValue;
import me.rigamortis.seppuku.api.value.NumberValue;
import me.rigamortis.seppuku.api.value.OptionalValue;
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

    public final OptionalValue mode = new OptionalValue("Mode", new String[]{"Mode", "M"}, 0, new String[]{"Vanilla", "Packet", "Bypass"});

    public final NumberValue<Float> speed = new NumberValue<Float>("Speed", new String[]{"Spd"}, 1.0f, Float.class, 0.0f, 5.0f, 0.01f);

    public final BooleanValue autoStart = new BooleanValue("AutoStart", new String[]{"AutoStart", "start", "autojump"}, true);
    public final BooleanValue disableInLiquid = new BooleanValue("DisableInLiquid", new String[]{"DisableInWater", "DisableInLava", "disableliquid", "liquidoff", "noliquid"}, true);
    public final BooleanValue infiniteDurability = new BooleanValue("InfiniteDurability", new String[]{"InfiniteDura", "dura", "inf", "infdura"}, false);
    public final BooleanValue noKick = new BooleanValue("NoKick", new String[]{"AntiKick", "Kick"}, true);

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
                if (this.disableInLiquid.getBoolean() && (mc.player.isInWater() || mc.player.isInLava())) {
                    if (mc.player.isElytraFlying()) {
                        mc.getConnection().sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                    }
                    return;
                }

                // automatic jump start
                if (this.autoStart.getBoolean()) {
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
                    switch (mode.getInt()) {
                        case 0: // Vanilla
                            final float speedScaled = this.speed.getFloat() * 0.05f; // 5/100 of original value

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
                        case 1: // Packet
                            this.freezePlayer(mc.player);
                            this.runNoKick(mc.player);

                            final double[] directionSpeedPacket = MathUtil.directionSpeed(this.speed.getFloat());

                            if (mc.player.movementInput.jump) {
                                mc.player.motionY = this.speed.getFloat();
                            }

                            if (mc.player.movementInput.sneak) {
                                mc.player.motionY = -this.speed.getFloat();
                            }

                            if (mc.player.movementInput.moveStrafe != 0 || mc.player.movementInput.moveForward != 0) {
                                mc.player.motionX = directionSpeedPacket[0];
                                mc.player.motionZ = directionSpeedPacket[1];
                            }

                            mc.getConnection().sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                            mc.getConnection().sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                            break;
                        case 2: // Bypass / 9b9t
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

                if (this.infiniteDurability.getBoolean()) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                }
                break;
            case POST:
                if (this.infiniteDurability.getBoolean()) {
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
        if (this.noKick.getBoolean() && !player.isElytraFlying()) {
            if (player.ticksExisted % 4 == 0) {
                player.motionY = -0.04f;
            }
        }
    }
}
