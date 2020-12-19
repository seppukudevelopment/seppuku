package me.rigamortis.seppuku.api.util;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * @author noil
 */
public class EntityUtil {

    private static Minecraft mc = Minecraft.getMinecraft();

    public static Vec3d getInterpolatedPosition(Entity entity, double x, double y, double z) {
        return new Vec3d(entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * x, entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * y, entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * z);
    }

    public static Vec3d getInterpolatedPosition(Entity entity, Vec3d vec) {
        return getInterpolatedPosition(entity, vec.x, vec.y, vec.z);
    }

    public static Vec3d getInterpolatedPosition(Entity entity, float ticks) {
        return getInterpolatedPosition(entity, ticks, ticks, ticks);
    }

    public static EnumFacing getFacingDirectionToPosition(BlockPos position) {
        EnumFacing direction = null;
        if (!mc.world.getBlockState(position.add(0, 1, 0)).isFullBlock()) {
            direction = EnumFacing.UP;
        } else if (!mc.world.getBlockState(position.add(0, -1, 0)).isFullBlock()) {
            direction = EnumFacing.DOWN;
        } else if (!mc.world.getBlockState(position.add(1, 0, 0)).isFullBlock()) {
            direction = EnumFacing.EAST;
        } else if (!mc.world.getBlockState(position.add(-1, 0, 0)).isFullBlock()) {
            direction = EnumFacing.WEST;
        } else if (!mc.world.getBlockState(position.add(0, 0, 1)).isFullBlock()) {
            direction = EnumFacing.SOUTH;
        } else if (!mc.world.getBlockState(position.add(0, 0, 1)).isFullBlock()) {
            direction = EnumFacing.NORTH;
        }
        return direction;
    }

    public static float[] getRotations(double x, double y, double z) {
        double xSize = x - mc.player.posX;
        double ySize = y - (mc.player.posY + mc.player.getEyeHeight());
        double zSize = z - mc.player.posZ;
        double theta = MathHelper.sqrt(xSize * xSize + zSize * zSize);
        float yaw = (float) (Math.atan2(zSize, xSize) * 180.0D / Math.PI) - 90.0F;
        float pitch = (float) (-(Math.atan2(ySize, theta) * 180.0D / Math.PI));
        return new float[]{
                (mc.player.rotationYaw + MathHelper.wrapDegrees(yaw - mc.player.rotationYaw)) % 360F,
                (mc.player.rotationPitch + MathHelper.wrapDegrees(pitch - mc.player.rotationPitch)) % 360F,
        };
    }

    public static float[] getRotations(EntityLivingBase entity) {
        if (entity == null)
            return null;
        return getRotations(entity.posX, entity.posY + ((double) entity.getEyeHeight() / 2F), entity.posZ);
    }
}
