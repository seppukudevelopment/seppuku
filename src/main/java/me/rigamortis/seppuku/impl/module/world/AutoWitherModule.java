package me.rigamortis.seppuku.impl.module.world;

import com.google.common.collect.Lists;
import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.player.EventRightClickBlock;
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
import net.minecraft.block.BlockSoulSand;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.util.List;

/**
 * @author noil
 */
public final class AutoWitherModule extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();

    public final Value<Boolean> rotate = new Value<Boolean>("Rotate", new String[]{"rotation", "r", "rotate"}, "Rotate to place blocks.", true);
    public final Value<Boolean> disable = new Value<Boolean>("Disable", new String[]{"dis", "autodisable", "autodis", "d"}, "Automatically disable after wither is placed.", false);
    public final Value<Boolean> sneak = new Value<Boolean>("PlaceOnSneak", new String[]{"sneak", "s", "pos", "sneakPlace"}, "When true, AutoWitherModule will only place while the player is sneaking.", false);
    public final Value<Float> placeDelay = new Value<Float>("Delay", new String[]{"PlaceDelay", "PlaceDel"}, "The delay(ms) between blocks being placed.", 100.0f, 0.0f, 500.0f, 1.0f);

    private final Timer placeTimer = new Timer();
    private final RotationTask rotationTask = new RotationTask("AutoWitherTask", 2);

    private FreeCamModule freeCamModule = null;
    private BlockPos beginBuildingPos = null;

    public AutoWitherModule() {
        super("AutoWither", new String[]{"Wither+", "Wither", "Withers"}, "Automatically builds withers.", "NONE", -1, ModuleType.WORLD);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        Seppuku.INSTANCE.getRotationManager().finishTask(this.rotationTask);
    }

    @Override
    public String getMetaData() {
        return "" + this.buildableWithers();
    }

    @Listener
    public void onRightClickBlock(EventRightClickBlock event) {
        if (mc.world != null && event.getPos() != null) {
            if (this.rotationTask.isOnline())
                Seppuku.INSTANCE.getRotationManager().finishTask(this.rotationTask);

            this.beginBuildingPos = event.getPos();
        }
    }

    @Listener
    public void onWalkingUpdate(EventUpdateWalkingPlayer event) {
        if (event.getStage() != EventStageable.EventStage.PRE)
            return;

        if (freeCamModule != null && freeCamModule.isEnabled())
            return;

        if (this.beginBuildingPos == null) {
            if (this.rotationTask.isOnline()) {
                Seppuku.INSTANCE.getRotationManager().finishTask(this.rotationTask);
            }
            return;
        }

        if (!mc.player.isSneaking() && this.sneak.getValue()) {
            if (this.rotationTask.isOnline()) {
                Seppuku.INSTANCE.getRotationManager().finishTask(this.rotationTask);
            }
            return;
        }

        BlockPos[] soulSandBlocks = new BlockPos[]{this.beginBuildingPos.up(), this.beginBuildingPos.up().up(), this.beginBuildingPos.up().up().north(), this.beginBuildingPos.up().up().south()};
        BlockPos[] skullBlocks = new BlockPos[]{this.beginBuildingPos.up().up().up(), this.beginBuildingPos.up().up().up().north(), this.beginBuildingPos.up().up().up().south()};

        final List<BlockPos> soulSandToPlace = Lists.newArrayListWithCapacity(4);
        final List<BlockPos> skullsToPlace = Lists.newArrayListWithCapacity(3);

        // find missing soul sand
        for (int i = 0; i < soulSandBlocks.length; i++) {
            BlockPos blockPos = soulSandBlocks[i];
            if (!this.valid(blockPos, false))
                continue;

            soulSandToPlace.add(blockPos);
        }

        // find missing skulls
        for (int j = 0; j < skullBlocks.length; j++) {
            BlockPos blockPos = skullBlocks[j];
            if (!this.valid(blockPos, true))
                continue;

            skullsToPlace.add(blockPos);
        }

        if (soulSandToPlace.size() != 0) { // we have soul sand to place
            final HandSwapContext handSwapContextSoulSand = new HandSwapContext(
                    mc.player.inventory.currentItem, this.findSoulSandInHotbar(mc.player));
            if (handSwapContextSoulSand.getNewSlot() != -1) {
                Seppuku.INSTANCE.getRotationManager().startTask(this.rotationTask);
                if (this.rotationTask.isOnline()) {
                    // swap to soul sand
                    handSwapContextSoulSand.handleHandSwap(false, mc);

                    for (BlockPos blockPos : soulSandToPlace) {
                        if (!this.valid(blockPos, false))
                            continue;

                        if (this.placeDelay.getValue() <= 0.0f) {
                            this.place(blockPos);
                        } else if (placeTimer.passed(this.placeDelay.getValue())) {
                            this.place(blockPos);
                            this.placeTimer.reset();
                        }
                    }

                    // swap back to original
                    handSwapContextSoulSand.handleHandSwap(true, mc);
                }
            } else {
                Seppuku.INSTANCE.getRotationManager().finishTask(this.rotationTask);
            }
        } else {
            //Seppuku.INSTANCE.getRotationManager().finishTask(this.rotationTask);
            if (skullsToPlace.size() != 0) {
                final HandSwapContext handSwapContextSkull = new HandSwapContext(
                        mc.player.inventory.currentItem, this.findWitherSkullInHotbar(mc.player));
                if (handSwapContextSkull.getNewSlot() != -1) {
                    Seppuku.INSTANCE.getRotationManager().startTask(this.rotationTask);
                    if (this.rotationTask.isOnline()) {
                        // swap to skull
                        handSwapContextSkull.handleHandSwap(false, mc);

                        for (BlockPos blockPos : skullsToPlace) {
                            if (!this.valid(blockPos, true))
                                continue;

                            if (this.placeDelay.getValue() <= 0.0f) {
                                this.place(blockPos);
                            } else if (placeTimer.passed(this.placeDelay.getValue())) {
                                this.place(blockPos);
                                this.placeTimer.reset();
                            }
                        }

                        // swap back to original
                        handSwapContextSkull.handleHandSwap(true, mc);
                    }
                } else {
                    Seppuku.INSTANCE.getRotationManager().finishTask(this.rotationTask);
                }
            } else {
                Seppuku.INSTANCE.getRotationManager().finishTask(this.rotationTask);
            }
        }

        if (soulSandToPlace.size() == 0 && skullsToPlace.size() == 0) { // no more blocks to place
            this.beginBuildingPos = null;
            if (this.disable.getValue()) {
                this.toggle(); // auto disable
            }
        }
    }

    @Listener
    public void onLoadWorld(EventLoadWorld event) {
        if (event.getWorld() != null) {
            freeCamModule = (FreeCamModule) Seppuku.INSTANCE.getModuleManager().find(FreeCamModule.class);
        }
    }

    private boolean isItemStackWitherSkull(final ItemStack itemStack) {
        if (itemStack.getItem() instanceof ItemSkull) {
            return itemStack.getMetadata() == 1; /* wither skull */
        }

        return false;
    }

    private int findWitherSkullInHotbar(final EntityPlayerSP player) {
        for (int index = 0; InventoryPlayer.isHotbar(index); index++)
            if (this.isItemStackWitherSkull(player.inventory.getStackInSlot(index)))
                return index;

        return -1;
    }

    private boolean isItemStackSoulSand(final ItemStack itemStack) {
        if (itemStack.getItem() instanceof ItemBlock)
            return ((ItemBlock) itemStack.getItem()).getBlock() instanceof BlockSoulSand;

        return false;
    }

    private int findSoulSandInHotbar(final EntityPlayerSP player) {
        for (int index = 0; InventoryPlayer.isHotbar(index); index++)
            if (this.isItemStackSoulSand(player.inventory.getStackInSlot(index)))
                return index;

        return -1;
    }

    private int getWitherSkullCount() {
        int skulls = 0;
        for (int i = 0; i < 45; i++) {
            final ItemStack itemStack = mc.player.inventory.getStackInSlot(i);
            if (itemStack.getItem() instanceof ItemSkull) {
                if (itemStack.getMetadata() == 1) {
                    skulls += itemStack.getCount();
                }
            }
        }

        return skulls;
    }

    private int buildableWithers() {
        int buildable = 0;

        if (mc.player == null || mc.world == null)
            return buildable;

        final int soulSand = InventoryUtil.getBlockCount(Blocks.SOUL_SAND);
        final int skulls = this.getWitherSkullCount();

        if (soulSand >= 4 && skulls >= 3) {
            final int soulSandDivided = soulSand / 4;
            final int skullsDivided = skulls / 3;

            if (skullsDivided < soulSandDivided)
                return skullsDivided;
            else if (soulSandDivided < skullsDivided)
                return soulSandDivided;
            else
                return 1;
        }

        return buildable;
    }

    private boolean valid(BlockPos pos, boolean isSkull) {
        // There are no entities colliding with block placement
        if (!mc.world.checkNoEntityCollision(new AxisAlignedBB(pos)))
            return false;

        // Player is too far from distance
        if (mc.player.getDistance(pos.getX(), pos.getY(), pos.getZ()) > 6.0f)
            return false;

        // Check if the block is replaceable
        final Block block = mc.world.getBlockState(pos).getBlock();

        if (block.isReplaceable(mc.world, pos)) {
            if (isSkull) {
                return !(block == Blocks.SKULL);
            } else {
                return !(block == Blocks.SOUL_SAND);
            }
        }

        return false;
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

        if (rotate.getValue()) {
            final float[] angle = MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), new Vec3d(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f));
            Seppuku.INSTANCE.getRotationManager().setPlayerRotations(angle[0], angle[1]);
        }

        mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(sideOffset, otherSide, EnumHand.MAIN_HAND, 0.5F, 0.5F, 0.5F));
        mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));

        //mc.playerController.processRightClickBlock(mc.player, mc.world, sideOffset, otherSide, new Vec3d(0.5F, 0.5F, 0.5F), EnumHand.MAIN_HAND);
        //mc.player.swingArm(EnumHand.MAIN_HAND);

        /*if (!visible.getValue()) {
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(sideOffset, otherSide, EnumHand.MAIN_HAND, 0.5F, 0.5F, 0.5F));
            mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
        } else {
            mc.playerController.processRightClickBlock(mc.player, mc.world, sideOffset, otherSide, new Vec3d(0.5F, 0.5F, 0.5F), EnumHand.MAIN_HAND);
            mc.player.swingArm(EnumHand.MAIN_HAND);
        }*/

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
}
