package me.rigamortis.seppuku.impl.module.world;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.player.EventRightClickBlock;
import me.rigamortis.seppuku.api.event.player.EventUpdateWalkingPlayer;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.MathUtil;
import me.rigamortis.seppuku.api.value.NumberValue;
import me.rigamortis.seppuku.api.value.OptionalValue;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 6/10/2019 @ 2:31 PM.
 */
public final class NukerModule extends Module {

    public final OptionalValue mode = new OptionalValue("Mode", new String[]{"Mode", "M"}, 0, new String[]{"Selection", "All"});

    public final NumberValue distance = new NumberValue("Distance", new String[]{"Dist", "D"}, 4.5f, Float.class, 0.0f, 5.0f, 0.1f);

    private Block selected;

    public NukerModule() {
        super("Nuker", new String[]{"Nuke"}, "Automatically mines blocks within reach", "NONE", -1, ModuleType.WORLD);
    }

    @Override
    public void onToggle() {
        super.onToggle();
        this.selected = null;
    }

    @Override
    public String getMetaData() {
        return this.mode.getSelectedOption();
    }

    @Listener
    public void onWalkingUpdate(EventUpdateWalkingPlayer event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            BlockPos pos = null;

            switch (this.mode.getInt()) {
                case 0:
                    pos = this.getClosestBlockSelection();
                    break;
                case 1:
                    pos = this.getClosestBlockAll();
                    break;
            }

            if (pos != null) {
                final Minecraft mc = Minecraft.getMinecraft();

                final float[] angle = MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), new Vec3d(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f));
                Seppuku.INSTANCE.getRotationManager().setPlayerRotations(angle[0], angle[1]);

                if (canBreak(pos)) {
                    mc.playerController.onPlayerDamageBlock(pos, mc.player.getHorizontalFacing());
                    mc.player.swingArm(EnumHand.MAIN_HAND);
                }
            }
        }
    }

    @Listener
    public void clickBlock(EventRightClickBlock event) {
        if (this.mode.getInt() == 0) {
            final Block block = Minecraft.getMinecraft().world.getBlockState(event.getPos()).getBlock();
            if (block != null && block != this.selected) {
                this.selected = block;
                Seppuku.INSTANCE.logChat("Nuker block set to " + block.getLocalizedName());
                event.setCanceled(true);
            }
        }
    }

    private boolean canBreak(BlockPos pos) {
        final IBlockState blockState = Minecraft.getMinecraft().world.getBlockState(pos);
        final Block block = blockState.getBlock();

        return block.getBlockHardness(blockState, Minecraft.getMinecraft().world, pos) != -1;
    }

    private BlockPos getClosestBlockAll() {
        final Minecraft mc = Minecraft.getMinecraft();
        float maxDist = this.distance.getFloat();

        BlockPos ret = null;

        for (float x = maxDist; x >= -maxDist; x--) {
            for (float y = maxDist; y >= -maxDist; y--) {
                for (float z = maxDist; z >= -maxDist; z--) {
                    final BlockPos pos = new BlockPos(mc.player.posX + x, mc.player.posY + y, mc.player.posZ + z);
                    final double dist = mc.player.getDistance(pos.getX(), pos.getY(), pos.getZ());
                    if (dist <= maxDist && (mc.world.getBlockState(pos).getBlock() != Blocks.AIR && !(mc.world.getBlockState(pos).getBlock() instanceof BlockLiquid)) && canBreak(pos)) {
                        if (pos.getY() >= mc.player.posY) {
                            maxDist = (float) dist;
                            ret = pos;
                        }
                    }
                }
            }
        }

        return ret;
    }

    private BlockPos getClosestBlockSelection() {
        final Minecraft mc = Minecraft.getMinecraft();
        float maxDist = this.distance.getFloat();

        BlockPos ret = null;

        for (float x = maxDist; x >= -maxDist; x--) {
            for (float y = maxDist; y >= -maxDist; y--) {
                for (float z = maxDist; z >= -maxDist; z--) {
                    final BlockPos pos = new BlockPos(mc.player.posX + x, mc.player.posY + y, mc.player.posZ + z);
                    final double dist = mc.player.getDistance(pos.getX(), pos.getY(), pos.getZ());
                    if (dist <= maxDist && (mc.world.getBlockState(pos).getBlock() != Blocks.AIR && !(mc.world.getBlockState(pos).getBlock() instanceof BlockLiquid)) && mc.world.getBlockState(pos).getBlock() == this.selected && canBreak(pos)) {
                        if (pos.getY() >= mc.player.posY) {
                            maxDist = (float) dist;
                            ret = pos;
                        }
                    }
                }
            }
        }

        return ret;
    }

}
