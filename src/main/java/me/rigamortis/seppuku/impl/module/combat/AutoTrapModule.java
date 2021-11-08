package me.rigamortis.seppuku.impl.module.combat;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.player.EventUpdateWalkingPlayer;
import me.rigamortis.seppuku.api.event.world.EventLoadWorld;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.task.hand.HandSwapContext;
import me.rigamortis.seppuku.api.task.rotation.RotationTask;
import me.rigamortis.seppuku.api.util.InventoryUtil;
import me.rigamortis.seppuku.api.util.MathUtil;
import me.rigamortis.seppuku.api.util.Timer;
import me.rigamortis.seppuku.api.value.Value;
import me.rigamortis.seppuku.impl.module.player.FreeCamModule;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.util.ArrayList;
import java.util.List;

/**
 * @author noil
 */
public final class AutoTrapModule extends Module {


    public final Value<Float> range = new Value<>("Range", new String[]{"Dist"}, "The minimum range to trap", 4.5f, 0.0f, 6.0f, 0.1f);
    public final Value<Float> placeDelay = new Value<Float>("Delay", new String[]{"PlaceDelay", "PlaceDel"}, "The delay(ms) between obsidian blocks being placed", 0.0f, 0.0f, 500.0f, 1.0f);

    public final Value<Boolean> self = new Value<Boolean>("Self", new String[]{"local", "localplayer", "me"}, "Keeps yourself trapped inside with the enemy", false);
    public final Value<Float> selfDistance = new Value<Float>("SelfDistance", new String[]{"SelfDist", "LocalDist", "sd", "ld"}, "The distance from the target to start trapping", 2.0f, 0.0f, 6.0f, 0.1f);
    public final Value<Boolean> extended = new Value<Boolean>("Extended", new String[]{"extend", "e", "big"}, "Enlarges the size of the trap", true);
    public final Value<Boolean> visible = new Value<Boolean>("Visible", new String[]{"Visible", "v"}, "Casts a ray to the placement position, forces the placement when disabled", true);
    public final Value<Boolean> rotate = new Value<>("Rotate", new String[]{"R"}, "Rotate the player's head and body while trapping", true);
    public final Value<Boolean> swing = new Value<>("Swing", new String[]{"S"}, "Swing the player's arm while trapping", true);
    public final Value<Boolean> disable = new Value<Boolean>("Disable", new String[]{"dis", "autodisable", "autodis", "d"}, "Disable after trap is placed", false);
    public final Value<Boolean> sneak = new Value<Boolean>("PlaceOnSneak", new String[]{"sneak", "s", "pos", "sneakPlace"}, "When true, AutoTrap will only place while the player is sneaking", false);
    public final Value<Boolean> overrideTask = new Value<Boolean>("OverrideTask", new String[]{"ot", "otask", "override"}, "Override rotation task system and force placements", true);

    private final Minecraft mc = Minecraft.getMinecraft();
    private final Timer placeTimer = new Timer();
    private final RotationTask rotationTask = new RotationTask("AutoTrapTask", 5);

    public Entity currentTarget = null;
    private FreeCamModule freeCamModule = null;

    public AutoTrapModule() {
        super("AutoTrap", new String[]{"Trap"}, "Automatically traps nearby players", "NONE", -1, ModuleType.COMBAT);
        this.placeTimer.reset();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        Seppuku.INSTANCE.getRotationManager().finishTask(this.rotationTask);
        this.currentTarget = null;
    }

    @Listener
    public void onWalkingUpdate(EventUpdateWalkingPlayer event) {
        if (mc.player == null || mc.world == null)
            return;

        if (freeCamModule != null && freeCamModule.isEnabled())
            return;

        switch (event.getStage()) {
            case PRE:
                // ensure we have obsidian in inventory
                if (InventoryUtil.getBlockCount(Blocks.OBSIDIAN) <= 0) {
                    this.currentTarget = null;
                    if (this.rotationTask.isOnline()) {
                        Seppuku.INSTANCE.getRotationManager().finishTask(this.rotationTask);
                    }
                    return;
                }

                // find a target
                this.currentTarget = this.findTarget();

                if (this.currentTarget != null) {
                    if (this.overrideTask.getValue()) {
                        if (this.rotationTask.isOnline()) {
                            Seppuku.INSTANCE.getRotationManager().finishTask(this.rotationTask);
                        }
                    }
                }
                break;
            case POST:
                if (this.currentTarget != null) {
                    final Vec3d targetPos = MathUtil.interpolateEntity(this.currentTarget, mc.getRenderPartialTicks());
                    final BlockPos interpolatedPos = new BlockPos(targetPos.x, targetPos.y, targetPos.z);
                    final BlockPos north = interpolatedPos.north();
                    final BlockPos south = interpolatedPos.south();
                    final BlockPos east = interpolatedPos.east();
                    final BlockPos west = interpolatedPos.west();

                    BlockPos[] surroundBlocks;
                    if (this.extended.getValue()) {
                        surroundBlocks = new BlockPos[]{north.down(), south.down(), east.down(), west.down(),
                                north, south, east, west, north.up(), south.up(), east.up(), west.up(), north.up().up(), interpolatedPos.up().up()};
                    } else {
                        surroundBlocks = new BlockPos[]{interpolatedPos.up().up()};
                    }

                    final List<BlockPos> blocksToPlace = new ArrayList<>();

                    // find missing blocks (starting from under the player first and going upwards)
                    for (int i = 0; i < surroundBlocks.length; i++) {
                        final BlockPos blockPos = surroundBlocks[i];

                        if (!this.valid(blockPos))
                            continue;

                        blocksToPlace.add(blockPos);
                    }

                    if (blocksToPlace.size() != 0) { // we have blocks to place
                        // begin rotation task
                        Seppuku.INSTANCE.getRotationManager().startTask(this.rotationTask);

                        final HandSwapContext handSwapContext = new HandSwapContext(
                                mc.player.inventory.currentItem, InventoryUtil.findObsidianInHotbar(mc.player));
                        if (handSwapContext.getNewSlot() == -1) {
                            Seppuku.INSTANCE.getRotationManager().finishTask(this.rotationTask);
                            return;
                        }

                        if (!mc.player.isSneaking() && this.sneak.getValue()) {
                            if (this.rotationTask.isOnline()) {
                                Seppuku.INSTANCE.getRotationManager().finishTask(this.rotationTask);
                            }
                            return;
                        }

                        if (this.rotationTask.isOnline() || this.overrideTask.getValue()) {
                            // swap to obsidian
                            handSwapContext.handleHandSwap(false, mc);

                            for (BlockPos blockPos : blocksToPlace) {
                                if (!this.valid(blockPos))
                                    continue;

                                if (this.placeDelay.getValue() <= 0.0f) {
                                    this.place(blockPos);
                                } else if (placeTimer.passed(this.placeDelay.getValue())) {
                                    this.place(blockPos);
                                    this.placeTimer.reset();
                                }
                            }

                            // swap back to original
                            handSwapContext.handleHandSwap(true, mc);
                        }
                    } else {
                        Seppuku.INSTANCE.getRotationManager().finishTask(this.rotationTask);
                    }

                    if (this.disable.getValue()) {
                        if (blocksToPlace.size() == 0) // no more blocks
                            this.toggle(); // auto disable
                    }
                } else {
                    Seppuku.INSTANCE.getRotationManager().finishTask(this.rotationTask);
                }
                break;
        }
    }

    @Listener
    public void onLoadWorld(EventLoadWorld event) {
        if (event.getWorld() != null) {
            freeCamModule = (FreeCamModule) Seppuku.INSTANCE.getModuleManager().find(FreeCamModule.class);
        }
    }

    private boolean valid(BlockPos pos) {
        // check faces for air (need to do this to place properly)
        int airFaces = 0;
        for (int i = 0; i < EnumFacing.values().length; i++) {
            if (mc.world.getBlockState(pos.offset(EnumFacing.values()[i])).getBlock() instanceof BlockAir) {
                airFaces++;
            }
        }
        if (airFaces == EnumFacing.values().length) {
            return false;
        }

        // there are no entities colliding with block placement
        final AxisAlignedBB axisAlignedBB = new AxisAlignedBB(pos);

        if (this.currentTarget != null) {
            if (!mc.world.getEntitiesWithinAABBExcludingEntity(null, axisAlignedBB).isEmpty()) {
                return false;
            }
        }

        if (!mc.world.checkNoEntityCollision(axisAlignedBB))
            return false;


        // player is too far from distance
        if (mc.player.getDistance(pos.getX(), pos.getY(), pos.getZ()) > this.range.getValue())
            return false;

        // check if the block is replaceable
        final Block block = mc.world.getBlockState(pos).getBlock();
        return block.isReplaceable(mc.world, pos) && !(block == Blocks.OBSIDIAN) && !(block == Blocks.BEDROCK);
    }

    private void place(BlockPos pos) {
        final Block block = mc.world.getBlockState(pos).getBlock();

        final EnumFacing direction = this.calcSide(pos);
        if (direction == null)
            return;

        final boolean activated = block.onBlockActivated(mc.world, pos, mc.world.getBlockState(pos), mc.player, EnumHand.MAIN_HAND, direction, 0, 0, 0);

        if (activated)
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));

        final EnumFacing otherSide = direction.getOpposite();
        final BlockPos sideOffset = pos.offset(direction);

        if (this.rotate.getValue()) {
            final float[] angle = MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), new Vec3d(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f));
            Seppuku.INSTANCE.getRotationManager().setPlayerRotations(angle[0], angle[1]);
        }

        if (!this.visible.getValue()) {
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(sideOffset, otherSide, EnumHand.MAIN_HAND, 0.5F, 0.5F, 0.5F));
            mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
        } else {
            mc.playerController.processRightClickBlock(mc.player, mc.world, sideOffset, otherSide, new Vec3d(0.5F, 0.5F, 0.5F), EnumHand.MAIN_HAND);

            if (this.swing.getValue()) {
                mc.player.swingArm(EnumHand.MAIN_HAND);
            }
        }

        if (activated)
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
    }

    private EnumFacing calcSide(BlockPos pos) {
        for (EnumFacing side : EnumFacing.values()) {
            BlockPos sideOffset = pos.offset(side);
            IBlockState offsetState = mc.world.getBlockState(sideOffset);
            if (!offsetState.getBlock().canCollideCheck(offsetState, false)) continue;
            if (!offsetState.getMaterial().isReplaceable()) return side;
        }
        return null;
    }

    private Entity findTarget() {
        Entity ent = null;
        float maxDist = this.range.getValue();

        for (Entity e : mc.world.loadedEntityList) {
            if (e != null) {
                if (this.checkFilter(e)) {
                    float currentDist = mc.player.getDistance(e);
                    if (currentDist <= maxDist) {
                        maxDist = currentDist;
                        ent = e;
                    }
                }
            }
        }

        return ent;
    }

    private boolean checkFilter(Entity entity) {
        boolean ret = false;

        if (entity instanceof EntityPlayer && entity != mc.player && Seppuku.INSTANCE.getFriendManager().isFriend(entity) == null && !entity.getName().equals(mc.player.getName())) {
            ret = true;
        }

        if (entity instanceof EntityLivingBase) {
            final EntityLivingBase entityLivingBase = (EntityLivingBase) entity;
            if (entityLivingBase.getHealth() <= 0) {
                ret = false;
            }

            if (!this.self.getValue()) { // don't trap ourselves with them
                if (entityLivingBase.getDistance(mc.player) <= this.selfDistance.getValue()) {
                    ret = false;
                }
            }
        }

        return ret;
    }

    public RotationTask getRotationTask() {
        return rotationTask;
    }

    public Entity getCurrentTarget() {
        return currentTarget;
    }

    public void setCurrentTarget(Entity currentTarget) {
        this.currentTarget = currentTarget;
    }
}
