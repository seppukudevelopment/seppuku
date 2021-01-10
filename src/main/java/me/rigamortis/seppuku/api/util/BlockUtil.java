package me.rigamortis.seppuku.api.util;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

/**
 * @author noil
 */
public class BlockUtil {

    private static final Minecraft mc = Minecraft.getMinecraft();

    public static Block getBlock(double x, double y, double z) {
        return mc.world.getBlockState(new BlockPos(x, y, z)).getBlock();
    }

    public static Block getBlock(BlockPos blockPos) {
        return mc.world.getBlockState(blockPos).getBlock();
    }

    public static List<BlockPos> getSphere(double x, double y, double z, int radius) {
        final List<BlockPos> list = new ArrayList<>();
        for (int x_ = -radius; x_ <= radius; x_++) {
            for (int y_ = -radius; y_ <= radius; y_++) {
                for (int z_ = -radius; z_ <= radius; z_++) {
                    double radiusX = (x_ + x);
                    double radiusY = (y_ + y);
                    double radiusZ = (z_ + z);
                    double diameterX = (x - radiusX) * (x - radiusX);
                    double diameterY = (y - radiusY) * (y - radiusY);
                    double diameterZ = (z - radiusZ) * (z - radiusZ);
                    double distance = Math.sqrt(diameterX + diameterY + diameterZ);
                    if (distance <= (radius)) {
                        list.add(new BlockPos(x_ + x, y_ + y, z_ + z));
                    }
                }
            }
        }
        return list;
    }

    public static boolean rayTrace(Vec3d to) {
        return (mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ), to, false, true, false) == null);
    }

    public static boolean rayTrace(BlockPos to) {
        return (mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(to.getX(), to.getY(), to.getZ()), false, true, false) == null);
    }
}
