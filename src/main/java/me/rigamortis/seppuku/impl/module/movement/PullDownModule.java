package me.rigamortis.seppuku.impl.module.movement;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.player.EventPlayerUpdate;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.BooleanValue;
import me.rigamortis.seppuku.api.value.NumberValue;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.util.ArrayList;
import java.util.List;

/**
 * // todo; needs further testing
 *
 * @author cookiedragon234
 * @author Daniel E
 */
public class PullDownModule extends Module {
    private final List<RayTraceResult> rayTraceResults = new ArrayList<>(8);
    private static final float VELOCITY_MAX = 10.0f;
    public final BooleanValue jumpDisables =
            new BooleanValue("JumpDisables", new String[]{"jump"}, true);
    public final NumberValue<Float> speed =
            new NumberValue<>("Speed", new String[]{"velocity"}, 4.0f, Float.class,
                    0f, VELOCITY_MAX, 1f);

    public PullDownModule() {
        super("PullDown", new String[]{"FastFall"}, "Increase your downwards velocity when falling",
                "NONE", -1, ModuleType.MOVEMENT);
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            final Minecraft mc = Minecraft.getMinecraft();
            if (jumpDisables.getBoolean() && mc.gameSettings.keyBindJump.isKeyDown())
                return;

            if (mc.player.isElytraFlying() || mc.player.capabilities.isFlying ||
                    mc.player.onGround || mc.player.fallDistance <= 0.0f)
                return;

            final Vec3d playerPosition = mc.player.getPositionVector();
            final boolean doesPlayerCollide = recomputeHullTraces(mc.player, playerPosition
                    .subtract(0.0d, 3.0d, 0.0d)).stream()
                    .anyMatch(this::hitsCollidableBlock);
            if (!doesPlayerCollide)
                mc.player.motionY = -speed.getFloat();
        }
    }

    private boolean hitsCollidableBlock(final RayTraceResult rayTraceResult) {
        return rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK;
    }

    private List<RayTraceResult> recomputeHullTraces(final Entity entity, final Vec3d nextPosition) {
        rayTraceResults.clear();

        final AxisAlignedBB boundingBox = entity.getEntityBoundingBox();
        final Vec3d[] boundingBoxCorners = {
                new Vec3d(boundingBox.minX, boundingBox.minY, boundingBox.minZ),
                new Vec3d(boundingBox.minX, boundingBox.minY, boundingBox.maxZ),
                new Vec3d(boundingBox.minX, boundingBox.maxY, boundingBox.minZ),
                new Vec3d(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ),
                new Vec3d(boundingBox.maxX, boundingBox.minY, boundingBox.minZ),
                new Vec3d(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ),
                new Vec3d(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ),
                new Vec3d(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ)
        };

        final Vec3d entityPosition = entity.getPositionVector();
        for (final Vec3d entityBoxCorner : boundingBoxCorners) {
            final Vec3d nextBoxCorner = entityBoxCorner.subtract(entityPosition).add(nextPosition);
            final RayTraceResult rayTraceResult = entity.world.rayTraceBlocks(entityBoxCorner,
                    nextBoxCorner, true, false, true);
            if (rayTraceResult == null)
                continue;

            rayTraceResults.add(rayTraceResult);
        }

        return rayTraceResults;
    }
}
