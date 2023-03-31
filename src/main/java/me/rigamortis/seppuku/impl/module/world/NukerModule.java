package me.rigamortis.seppuku.impl.module.world;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.player.EventRightClickBlock;
import me.rigamortis.seppuku.api.event.player.EventUpdateWalkingPlayer;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.task.rotation.RotationTask;
import me.rigamortis.seppuku.api.util.BlockUtil;
import me.rigamortis.seppuku.api.util.EntityUtil;
import me.rigamortis.seppuku.api.util.MathUtil;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.util.*;

/**
 * Author Seth
 * 6/10/2019 @ 2:31 PM.
 */
public final class NukerModule extends Module {

    public final Value<Mode> mode = new Value<Mode>("Mode", new String[]{"M"}, "The nuker mode to use", Mode.SELECTION);
    public final Value<MineMode> mineMode = new Value<MineMode>("MineMode", new String[]{"MM"}, "The way that nuker mines blocks", MineMode.NORMAL);
    public final Value<Float> distance = new Value<Float>("Distance", new String[]{"Dist", "D"}, "Maximum distance in blocks the nuker will reach", 4.5f, 0.0f, 5.0f, 0.1f);
    public final Value<Boolean> fixed = new Value<Boolean>("FixedDistance", new String[]{"Fixed", "fdist", "F"}, "Use vertical and horizontal distances in blocks instead of distances relative to the camera", false);
    public final Value<Float> vDistance = new Value<Float>("VerticalDistance", new String[]{"Vertical", "vdist", "VD"}, "Maximum vertical distance in blocks the nuker will reach", 4.5f, 0.0f, 5.0f, 0.1f);
    public final Value<Float> hDistance = new Value<Float>("HorizontalDistance", new String[]{"Horizontal", "hist", "HD"}, "Maximum horizontal distance in blocks the nuker will reach", 3f, 0.0f, 5.0f, 0.1f);

    public final Value<Integer> timeout = new Value<Integer>("Timeout", new String[]{"TO, t"}, "How long to wait (in ms) until trying to break a specific block again (PACKET Mode)", 1000, 0, Integer.MAX_VALUE, 1);
    public final Value<Float> minMineSpeed = new Value<Float>("MinMineSpeed", new String[]{"Min", "Speed", "MineSpeed"}, "How fast you should be able to mine a block for nuker to attempt to mine it (0-1, 0 to allow all blocks, 1 to only allow instantly minable blocks)", 0.2f, 0f, 1.0f, 0.1f);

    private final RotationTask rotationTask = new RotationTask("NukerTask", 2);

    private Block selected = null;
    private BlockPos currentPos = null;
    private Map<BlockPos, Long> attemptedBreaks = new HashMap<>();

    public NukerModule() {
        super("Nuker", new String[]{"Nuke"}, "Automatically mines blocks within reach", "NONE", -1, ModuleType.WORLD);
    }

    @Override
    public void onToggle() {
        super.onToggle();
        this.selected = null;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        Seppuku.INSTANCE.getRotationManager().finishTask(this.rotationTask);
    }

    @Override
    public String getMetaData() {
        return this.mode.getValue().name();
    }

    @Listener
    public void onWalkingUpdate(EventUpdateWalkingPlayer event) {
        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || mc.world == null)
            return;

        switch (event.getStage()) {
            case PRE:
                if (this.mineMode.getValue() == MineMode.PACKET) {
                    // PACKET mode does not need to rotate. The few servers it works on probably don't care about rotation.
                    // It also tries to break more than one block per event, and I am not sure how to handle that using the
                    // rotation task.
                    List<BlockPos> blocks = getSortedBlocks();

                    for (BlockPos pos : blocks) {
                        IBlockState state = mc.world.getBlockState(pos);

                        if (shouldBreak(pos)) {
                            if (!this.attemptedBreaks.containsKey(pos)) {
                                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, pos, EnumFacing.NORTH));
                                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, pos, EnumFacing.NORTH));

                                this.attemptedBreaks.put(pos, System.currentTimeMillis());
                            }
                        }
                    }

                    List<BlockPos> toRemove = new ArrayList<>();
                    for (BlockPos pos : attemptedBreaks.keySet()) {
                        if (System.currentTimeMillis() - attemptedBreaks.get(pos) >= timeout.getValue()) {
                            toRemove.add(pos);
                        }
                    }

                    for (BlockPos pos : toRemove) {
                        attemptedBreaks.remove(pos);
                    }
                } else {
                    this.currentPos = this.getClosestBlock();

                    if (this.currentPos != null) {
                        Seppuku.INSTANCE.getRotationManager().startTask(this.rotationTask);
                        if (this.rotationTask.isOnline()) {
                            final float[] angle = MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), new Vec3d(this.currentPos.getX() + 0.5f, this.currentPos.getY() + 0.5f, this.currentPos.getZ() + 0.5f));
                            Seppuku.INSTANCE.getRotationManager().setPlayerRotations(angle[0], angle[1]);
                        }
                    }
                }

                break;
            case POST:
                if (this.mode.getValue().equals(Mode.CREATIVE)) {
                    if (mc.player.capabilities.isCreativeMode) {
                        /* the amazing creative 'nuker' straight from the latch hacked client */
                        // TODO: Test moving to the iterable didn't break this
                        for (BlockPos blockPos : getBoxIterable()) {
                            final Block block = BlockUtil.getBlock(blockPos);
                            if (block == Blocks.AIR || !mc.world.getBlockState(blockPos).isFullBlock())
                                continue;

                            final Vec3d eyesPos = new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ);
                            final Vec3d posVec = new Vec3d(blockPos).add(0.5f, 0.5f, 0.5f);
                            double distanceSqPosVec = eyesPos.squareDistanceTo(posVec);

                            for (EnumFacing side : EnumFacing.values()) {
                                final Vec3d hitVec = posVec.add(new Vec3d(side.getDirectionVec()).scale(0.5f));
                                double distanceSqHitVec = eyesPos.squareDistanceTo(hitVec);

                                // check if hitVec is within range (6 blocks)
                                if (distanceSqHitVec > 36)
                                    continue;

                                // check if side is facing towards player
                                if (distanceSqHitVec >= distanceSqPosVec)
                                    continue;

                                // face block
                                final float[] rotations = EntityUtil.getRotations(hitVec.x, hitVec.y, hitVec.z);
                                Seppuku.INSTANCE.getRotationManager().setPlayerRotations(rotations[0], rotations[1]);

                                // damage block
                                if (mc.playerController.onPlayerDamageBlock(blockPos, side)) {
                                    mc.player.swingArm(EnumHand.MAIN_HAND);
                                }
                            }
                        }
                    }
                } else if (this.mineMode.getValue() != MineMode.PACKET) {
                    if (this.currentPos != null) {
                        if (this.rotationTask.isOnline()) {
                            if (SpeedMineModule.autoPos != null) {
                                if (this.currentPos.equals(SpeedMineModule.autoPos)) {
                                    return;
                                }
                            }

                            if (this.canBreak(this.currentPos)) {
                                mc.playerController.onPlayerDamageBlock(this.currentPos, mc.player.getHorizontalFacing());
                                mc.player.swingArm(EnumHand.MAIN_HAND);
                            }
                        }
                    } else {
                        Seppuku.INSTANCE.getRotationManager().finishTask(this.rotationTask);
                    }
                }
                break;
        }
    }

    @Listener
    public void clickBlock(EventRightClickBlock event) {
        if (this.mode.getValue() == Mode.SELECTION) {
            final Block block = Minecraft.getMinecraft().world.getBlockState(event.getPos()).getBlock();
            if (block != this.selected) {
                this.selected = block;
                Seppuku.INSTANCE.logChat("Nuker block set to " + block.getLocalizedName());
                event.setCanceled(true);
            }
        }
    }

    private boolean canBreak(BlockPos pos) {
        final IBlockState blockState = Minecraft.getMinecraft().world.getBlockState(pos);
        final Block block = blockState.getBlock();
        return block.getBlockHardness(blockState, Minecraft.getMinecraft().world, pos) >= minMineSpeed.getValue();
    }

    private boolean shouldBreak(BlockPos pos) {
        final Minecraft mc = Minecraft.getMinecraft();

        // TODO: Might want to double check that the block is within the distance value (getAllInBox is generous)

        // TODO: Replace SELECTION with a filter?
        if (this.mode.getValue() == Mode.SELECTION) {
            if (this.selected != null && !mc.world.getBlockState(pos).getBlock().equals(this.selected)) {
                return false;
            }
        }

        return mc.world.getBlockState(pos).getBlock() != Blocks.AIR &&
                !(mc.world.getBlockState(pos).getBlock() instanceof BlockLiquid) &&
                this.canBreak(pos) &&
                !pos.equals(SpeedMineModule.autoPos);
    }

    private Iterable<BlockPos> getBoxIterable () {
        final Minecraft mc = Minecraft.getMinecraft();
        AxisAlignedBB bb;

        if (this.fixed.getValue()) {
            bb = new AxisAlignedBB(
                    (int) mc.player.posX - hDistance.getValue(),
                    (int) mc.player.posY - vDistance.getValue(),
                    (int) mc.player.posZ - hDistance.getValue(),
                    (int) mc.player.posX + hDistance.getValue(),
                    (int) mc.player.posY + vDistance.getValue(),
                    (int) mc.player.posZ + hDistance.getValue());
        } else {
            bb = new AxisAlignedBB(
                    (int) mc.player.posX - distance.getValue(),
                    (int) mc.player.posY - distance.getValue(),
                    (int) mc.player.posZ - distance.getValue(),
                    (int) mc.player.posX + distance.getValue(),
                    (int) mc.player.posY + distance.getValue(),
                    (int) mc.player.posZ + distance.getValue());
        }

        return BlockPos.getAllInBox((int) bb.minX, (int) bb.minY, (int) bb.minZ, (int) bb.maxX, (int) bb.maxY, (int) bb.maxZ);
    }

    private BlockPos getClosestBlock() {
        final Minecraft mc = Minecraft.getMinecraft();

        BlockPos closest = null;
        double closestDist = Double.POSITIVE_INFINITY;
        for (BlockPos pos : getBoxIterable()) {
            double dist = pos.distanceSqToCenter(mc.player.posX, mc.player.getEyeHeight(), mc.player.posZ);

            if (shouldBreak(pos)) {
                if (dist < closestDist) {
                    closestDist = dist;
                    closest = pos;
                }
            }
        }

        return closest;
    }

    /*
     * Not very good performance wise. A better way to do this would be to directly iterate from the player's head,
     * but that is difficult, and we don't expect SUPER huge input for this. At most the player would probably have a
     * cube of 'radius' 6, +1 if the bounding box is aligned right, so we would have around 13^3 = 2197 blocks to
     * iterate and sort.
     */
    private List<BlockPos> getSortedBlocks() {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        List<BlockPos> ret = new ArrayList<>();

        for (BlockPos pos : getBoxIterable()) {
            ret.add(new BlockPos(pos));
        }

        ret.sort(Comparator.comparingDouble(o -> o.distanceSqToCenter(player.posX, player.posY, player.posZ)));
        return ret;
    }

    private enum Mode {
        SELECTION, ALL, CREATIVE
    }

    private enum MineMode {
        NORMAL, PACKET
    }
}
