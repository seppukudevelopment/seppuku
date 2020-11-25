package me.rigamortis.seppuku.impl.module.world;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.player.EventRightClickBlock;
import me.rigamortis.seppuku.api.event.player.EventUpdateWalkingPlayer;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.MathUtil;
import me.rigamortis.seppuku.api.value.Value;
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

    public final Value<Mode> mode = new Value<Mode>("Mode", new String[]{"Mode", "M"}, "The nuker mode to use.", Mode.SELECTION);

    private enum Mode {
        SELECTION, ALL
    }

    public final Value<Float> distance = new Value<Float>("Distance", new String[]{"Dist", "D"}, "Maximum distance in blocks the nuker will reach.", 4.5f, 0.0f, 5.0f, 0.1f);
    public final Value<Boolean> fixed = new Value<Boolean>("Fixed distances", new String[]{"F", "fixed"}, "Use vertical and horizontal distances in blocks instead of distances relative to the camera.", false);
    public final Value<Float> Vdistance = new Value<Float>("Vertical Distance", new String[]{"VDist", "VD"}, "Maximum vertical distance in blocks the nuker will reach.", 4.5f, 0.0f, 5.0f, 0.1f);
    public final Value<Float> Hdistance = new Value<Float>("Horizontal Distance", new String[]{"HDist", "HD"}, "Maximum horizontal distance in blocks the nuker will reach.", 3f, 0.0f, 5.0f, 0.1f);

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
        return this.mode.getValue().name();
    }

    @Listener
    public void onWalkingUpdate(EventUpdateWalkingPlayer event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            BlockPos pos = null;

            switch (this.mode.getValue()) {
                case SELECTION:
                    pos = this.getClosestBlock(true);
                    break;
                case ALL:
                    pos = this.getClosestBlock(false);
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
        if (this.mode.getValue() == Mode.SELECTION) {
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

    private BlockPos getClosestBlock(boolean selection) {
        final Minecraft mc = Minecraft.getMinecraft();

        BlockPos ret = null;

        if (this.fixed.getValue()) {
            float maxVDist = this.Vdistance.getValue();
            float maxHDist = this.Hdistance.getValue();
            for (float x = 0; x <= maxHDist; x++) {
                for (float y = 0; y <= maxVDist; y++) {
                    for (float z = 0; z <= maxHDist; z++) {
                        for (int revX = 0; revX <= 1; revX++, x = -x) {
                            for (int revZ = 0; revZ <= 1; revZ++, z = -z) {
                                final BlockPos pos = new BlockPos(mc.player.posX + x, mc.player.posY + y, mc.player.posZ + z);
                                if (
                                        (mc.world.getBlockState(pos).getBlock() != Blocks.AIR &&
                                                !(mc.world.getBlockState(pos).getBlock() instanceof BlockLiquid)) &&
                                                canBreak(pos) &&
                                                ((selection && mc.world.getBlockState(pos).getBlock() == this.selected) || true)
                                ) {
                                    ret = pos;
                                }
                            }
                        }
                    }
                }
            }
        } else {
            float maxDist = this.distance.getValue();
            for (float x = maxDist; x >= -maxDist; x--) {
                for (float y = maxDist; y >= -maxDist; y--) {
                    for (float z = maxDist; z >= -maxDist; z--) {
                        final BlockPos pos = new BlockPos(mc.player.posX + x, mc.player.posY + y, mc.player.posZ + z);
                        final double dist = mc.player.getDistance(pos.getX(), pos.getY(), pos.getZ());
                        if (dist <= maxDist && (mc.world.getBlockState(pos).getBlock() != Blocks.AIR && !(mc.world.getBlockState(pos).getBlock() instanceof BlockLiquid)) && canBreak(pos) && ((selection && mc.world.getBlockState(pos).getBlock() == this.selected) || true)) {
                            if (pos.getY() >= mc.player.posY) {
                                maxDist = (float) dist;
                                ret = pos;
                            }
                        }
                    }
                }
            }
        }
        return ret;
    }

}
