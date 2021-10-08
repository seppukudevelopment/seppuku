package me.rigamortis.seppuku.impl.module.movement;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.player.EventMove;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.BlockUtil;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.Minecraft;
import net.minecraft.init.MobEffects;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * created by noil on 9/22/2019 at 11:01 AM
 * updated on 12/12/2020 - sn01
 */
public final class StrafeModule extends Module {

    public Value<Boolean> ground = new Value<Boolean>("Ground", new String[]{"Floor", "OnGround", "G"}, "When enabled, enables strafe movement while on ground.", false);
    public Value<Boolean> elytraCheck = new Value<Boolean>("ElytraCheck", new String[]{"FlyChecks", "Elytra"}, "Lets you use ElytraFly and Strafe at the same time without bugging out.", true);
    public Value<Boolean> liquidCheck = new Value<Boolean>("LiquidCheck", new String[]{"LiquidChecks", "Liquids", "Liquid", "Water", "Lava"}, "Attempts to fix bugs while swimming.", true);

    public StrafeModule() {
        super("Strafe", new String[]{"Strafe"}, "Unlocks full movement control while airborne, and optionally on ground too.", "NONE", -1, ModuleType.MOVEMENT);
    }

    @Listener
    public void onMove(EventMove event) {
        final Minecraft mc = Minecraft.getMinecraft();

        if (mc.player == null)
            return;

        if (mc.player.isSneaking() || mc.player.isOnLadder() || mc.player.isInWeb || mc.player.isInLava() || mc.player.isInWater() || mc.player.capabilities.isFlying)
            return;

        if (this.liquidCheck.getValue()) {
            if (BlockUtil.getBlock(mc.player.posX, mc.player.posY - 1, mc.player.posZ) instanceof BlockLiquid) {
                return;
            }
        }

        if (this.elytraCheck.getValue() && mc.player.isElytraFlying())
            return;

        // check to bypass option on ground or not
        if (!this.ground.getValue()) {
            if (mc.player.onGround)
                return;
        }

        // check for flight, could be an option maybe but it bugs out  packet fly
        final FlightModule flightModule = (FlightModule) Seppuku.INSTANCE.getModuleManager().find(FlightModule.class);
        if (flightModule != null && flightModule.isEnabled()) {
            return;
        }

        // movement data variables
        float playerSpeed = 0.2873f;
        float moveForward = mc.player.movementInput.moveForward;
        float moveStrafe = mc.player.movementInput.moveStrafe;
        float rotationPitch = mc.player.rotationPitch;
        float rotationYaw = mc.player.rotationYaw;

        // check for speed potion
        if (mc.player.isPotionActive(MobEffects.SPEED)) {
            final int amplifier = mc.player.getActivePotionEffect(MobEffects.SPEED).getAmplifier();
            playerSpeed *= (1.0f + 0.2f * (amplifier + 1));
        }

        // not movement input, stop all motion
        if (moveForward == 0.0f && moveStrafe == 0.0f) {
            event.setX(0.0d);
            event.setZ(0.0d);
        } else {
            if (moveForward != 0.0f) {
                if (moveStrafe > 0.0f) {
                    rotationYaw += ((moveForward > 0.0f) ? -45 : 45);
                } else if (moveStrafe < 0.0f) {
                    rotationYaw += ((moveForward > 0.0f) ? 45 : -45);
                }
                moveStrafe = 0.0f;
                if (moveForward > 0.0f) {
                    moveForward = 1.0f;
                } else if (moveForward < 0.0f) {
                    moveForward = -1.0f;
                }
            }
            event.setX((moveForward * playerSpeed) * Math.cos(Math.toRadians((rotationYaw + 90.0f))) + (moveStrafe * playerSpeed) * Math.sin(Math.toRadians((rotationYaw + 90.0f))));
            event.setZ((moveForward * playerSpeed) * Math.sin(Math.toRadians((rotationYaw + 90.0f))) - (moveStrafe * playerSpeed) * Math.cos(Math.toRadians((rotationYaw + 90.0f))));
        }

        // we need to ensure we don't interfere with safewalk's limitations, so we run it's checks again on the same event
        final SafeWalkModule safeWalkModule = (SafeWalkModule) Seppuku.INSTANCE.getModuleManager().find(SafeWalkModule.class);
        if (safeWalkModule != null && safeWalkModule.isEnabled()) {
            safeWalkModule.onMove(event);
        }
    }
}
