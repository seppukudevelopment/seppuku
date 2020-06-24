package me.rigamortis.seppuku.impl.module.movement;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.player.EventPlayerUpdate;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * // todo; needs further testing
 *
 * @author cookiedragon234
 * @author Daniel E
 */
public class PullDownModule extends Module {
    private static final float VELOCITY_MAX = 10.0f;
    public final Value<Boolean> jumpDisables =
            new Value<Boolean>("JumpDisables", new String[]{"jump"}, "When enabled, holding the jump key will disable any pulldown events from triggering.", true);
    public final Value<Float> speed =
            new Value<Float>("Speed", new String[]{"velocity"}, "Speed multiplier at which the player will be pulled down at.", 4.0f,
                    0f, VELOCITY_MAX, 1f);

    public PullDownModule() {
        super("PullDown", new String[]{"FastFall"}, "Increase your downwards velocity when falling",
                "NONE", -1, ModuleType.MOVEMENT);
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            final Minecraft mc = Minecraft.getMinecraft();
            if (this.jumpDisables.getValue() && mc.gameSettings.keyBindJump.isKeyDown())
                return;

            if (mc.player.isElytraFlying() || mc.player.capabilities.isFlying ||
                    mc.player.onGround || mc.player.fallDistance <= 0.0f)
                return;

            final Vec3d playerPosition = mc.player.getPositionVector();
            if (!hullCollidesWithBlock(mc.player, playerPosition.subtract(0.0d,
                    3.0d, 0.0d)))
                mc.player.motionY = -this.speed.getValue();
        }
    }

    private boolean hullCollidesWithBlock(final Entity entity,
            final Vec3d nextPosition) {
        final AxisAlignedBB boundingBox = entity.getEntityBoundingBox();
        final Vec3d[] boundingBoxCorners = {
                new Vec3d(boundingBox.minX, boundingBox.minY, boundingBox.minZ),
                new Vec3d(boundingBox.minX, boundingBox.minY, boundingBox.maxZ),
                new Vec3d(boundingBox.maxX, boundingBox.minY, boundingBox.minZ),
                new Vec3d(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ)
        };

        final Vec3d entityPosition = entity.getPositionVector();
        for (final Vec3d entityBoxCorner : boundingBoxCorners) {
            final Vec3d nextBoxCorner = entityBoxCorner.subtract(entityPosition).add(nextPosition);
            final RayTraceResult rayTraceResult = entity.world.rayTraceBlocks(entityBoxCorner,
                    nextBoxCorner, true, false, true);
            if (rayTraceResult == null)
                continue;

            if (rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK)
                return true;
        }

        return false;
    }
}
