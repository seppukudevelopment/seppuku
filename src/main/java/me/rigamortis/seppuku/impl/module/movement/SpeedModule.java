package me.rigamortis.seppuku.impl.module.movement;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.player.EventMove;
import me.rigamortis.seppuku.api.event.player.EventPlayerUpdate;
import me.rigamortis.seppuku.api.event.player.EventUpdateWalkingPlayer;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.MathUtil;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.init.MobEffects;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 4/19/2019 @ 10:40 AM.
 */
public final class SpeedModule extends Module {

    public final Value<Mode> mode = new Value<Mode>("Mode", new String[]{"Mode", "M"}, "The speed mode to use.", Mode.VANILLA);

    private enum Mode {
        VANILLA, BHOP
    }

    public final Value<Float> speed = new Value<Float>("Speed", new String[]{"Spd"}, "Speed multiplier, higher numbers equal faster motion.", 0.1f, 0.0f, 10.0f, 0.1f);

    private int tick;
    private double prevDistance;
    private double movementSpeed;

    public SpeedModule() {
        super("Speed", new String[]{"Spd"}, "Allows you to move faster.", "NONE", -1, ModuleType.MOVEMENT);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (Minecraft.getMinecraft().world != null) {
            this.movementSpeed = getDefaultSpeed();
            this.tick = 1;
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (Minecraft.getMinecraft().world != null) {
            this.movementSpeed = getDefaultSpeed();
            this.prevDistance = 0;
            this.tick = 4;
        }
    }

    @Override
    public String getMetaData() {
        return this.mode.getValue().name();
    }

    private double getDefaultSpeed() {
        final Minecraft mc = Minecraft.getMinecraft();
        double defaultSpeed = 0.2873D;

        if (mc.player.isPotionActive(MobEffects.SPEED)) {
            final int amplifier = mc.player.getActivePotionEffect(MobEffects.SPEED).getAmplifier();
            defaultSpeed *= (1.0D + 0.2D * (amplifier + 1));
        }

        if (mc.player.isPotionActive(MobEffects.JUMP_BOOST)) {
            final int amplifier = mc.player.getActivePotionEffect(MobEffects.SPEED).getAmplifier();
            defaultSpeed /= (1.0D + 0.2D * (amplifier + 1));
        }

        return defaultSpeed;
    }

    @Listener
    public void onMove(EventMove event) {
        if (this.mode.getValue() == Mode.BHOP) {
            final Minecraft mc = Minecraft.getMinecraft();

            if (MathUtil.round(mc.player.posY - (int) mc.player.posY, 3) == MathUtil.round(0.138D, 3)) {
                mc.player.motionY -= 1.0D;
                event.setY(event.getY() - 0.0931D);
                mc.player.posY -= 0.0931D;
            }
            if ((this.tick == 2) && ((mc.player.moveForward != 0.0F) || (mc.player.moveStrafing != 0.0F))) {
                event.setY((mc.player.motionY = 0.39936D));
                this.movementSpeed *= 1.547D;
            } else if (this.tick == 3) {
                final double difference = 0.66D * (this.prevDistance - this.getDefaultSpeed());
                this.movementSpeed = (this.prevDistance - difference);
            } else {
                if ((mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0.0D, mc.player.motionY, 0.0D)).size() > 0) || (mc.player.collidedVertically)) {
                    this.tick = 1;
                }
                this.movementSpeed = (this.prevDistance - this.prevDistance / 159.0D);
            }

            this.movementSpeed = Math.max(this.movementSpeed, this.getDefaultSpeed());
            final double[] direction = MathUtil.directionSpeed(this.movementSpeed);

            if (direction != null) {
                mc.player.motionX = direction[0];
                mc.player.motionZ = direction[1];
            }

            this.tick += 1;
        }
    }

    @Listener
    public void onWalkingUpdate(EventUpdateWalkingPlayer event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (this.mode.getValue() == Mode.BHOP) {
                final Minecraft mc = Minecraft.getMinecraft();
                final double deltaX = (mc.player.posX - mc.player.prevPosX);
                final double deltaZ = (mc.player.posZ - mc.player.prevPosZ);
                this.prevDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
            }
        }
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            final Minecraft mc = Minecraft.getMinecraft();

            final Entity riding = mc.player.getRidingEntity();

            if (riding != null) {
                final double[] dir = MathUtil.directionSpeed(this.speed.getValue());

                if (mc.player.movementInput.moveStrafe != 0 || mc.player.movementInput.moveForward != 0) {
                    riding.motionX = dir[0];
                    riding.motionZ = dir[1];
                } else {
                    riding.motionX = 0;
                    riding.motionZ = 0;
                }
            }
        }
    }
}
