package me.rigamortis.seppuku.impl.module.combat;

import com.google.common.collect.Lists;
import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventSendPacket;
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
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemChorusFruit;
import net.minecraft.network.play.client.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.util.List;

/**
 * @author Seth
 * @author noil
 */
public final class NoCrystalModule extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();

    public final Value<Boolean> visible = new Value<Boolean>("Visible", new String[]{"Visible", "v"}, "When disabled, you will not see swing animations or sounds.", true);
    public final Value<Boolean> rotate = new Value<Boolean>("Rotate", new String[]{"rotation", "r", "rotate"}, "Rotate to place blocks.", true);
    public final Value<Boolean> center = new Value<Boolean>("Center", new String[]{"centered", "c", "cen"}, "Centers the player on their current block when beginning to place.", true);
    public final Value<Boolean> extended = new Value<Boolean>("Extended", new String[]{"extend", "e", "big"}, "Enlarges the size of the fortress.", false);
    public final Value<Boolean> disable = new Value<Boolean>("Disable", new String[]{"dis", "autodisable", "autodis", "d"}, "Automatically disable after obsidian is placed.", false);
    public final Value<Boolean> sneak = new Value<Boolean>("PlaceOnSneak", new String[]{"sneak", "s", "pos", "sneakPlace"}, "When true, NoCrystal will only place while the player is sneaking.", false);
    public final Value<Float> range = new Value<Float>("Range", new String[]{"MaxRange", "MaximumRange"}, "The maximum block reaching range to continue building in.", 6.0f, 1.0f, 10.0f, 0.5f);
    public final Value<Float> placeDelay = new Value<Float>("Delay", new String[]{"PlaceDelay", "PlaceDel"}, "The delay(ms) between obsidian blocks being placed.", 100.0f, 0.0f, 500.0f, 1.0f);

    private final Timer placeTimer = new Timer();
    private final Timer chorusTpTimer = new Timer();
    private final RotationTask rotationTask = new RotationTask("NoCrystalTask", 8);

    private FreeCamModule freeCamModule = null;

    public NoCrystalModule() {
        super("NoCrystal", new String[]{"AntiCrystal", "FeetPlace", "Surround"}, "Automatically places obsidian around you to avoid crystal damage", "NONE", -1, ModuleType.COMBAT);
        this.placeTimer.reset();
        this.chorusTpTimer.reset();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        Seppuku.INSTANCE.getRotationManager().finishTask(this.rotationTask);
    }

    @Listener
    public void onPacketSend(EventSendPacket event) {
        if (event.getStage().equals(EventStageable.EventStage.PRE)) {
            if (event.getPacket() instanceof CPacketPlayerTryUseItem) {
                final CPacketPlayerTryUseItem packetPlayerTryUseItem = (CPacketPlayerTryUseItem) event.getPacket();
                if (packetPlayerTryUseItem.hand == EnumHand.MAIN_HAND) {
                    if (mc.player.getHeldItemMainhand().getItem() instanceof ItemChorusFruit) {
                        this.chorusTpTimer.reset();
                    }
                }
            }
        }
    }

    @Listener
    public void onWalkingUpdate(EventUpdateWalkingPlayer event) {
        if (event.getStage() != EventStageable.EventStage.PRE)
            return;

        if (freeCamModule != null && freeCamModule.isEnabled())
            return;

        final Vec3d pos = MathUtil.interpolateEntity(mc.player, mc.getRenderPartialTicks());
        final float playerSpeed = (float) MathUtil.getDistance(pos, mc.player.posX, mc.player.posY, mc.player.posZ);

        if (!mc.player.onGround || playerSpeed > 0.005f)
            return;

        final BlockPos interpolatedPos = new BlockPos(pos.x, pos.y, pos.z);
        final BlockPos north = interpolatedPos.north();
        final BlockPos south = interpolatedPos.south();
        final BlockPos east = interpolatedPos.east();
        final BlockPos west = interpolatedPos.west();

        BlockPos[] surroundBlocks;
        if (this.extended.getValue()) {
            // ..x..
            // .xxx.  x = to place block
            // xx@xx  . = to ignore
            // .xxx.  @ = player
            // ..x..
            surroundBlocks = new BlockPos[]{north.down(), south.down(), east.down(), west.down(),
                    north, south, east, west, north.east(), north.west(), south.east(), south.west(),
                    north.north(), south.south(), east.east(), west.west()};
        } else {
            // ..x..  x = to place block
            // .x@x.  . = to ignore
            // ..x..  @ = player
            surroundBlocks = new BlockPos[]{north.down(), south.down(), east.down(), west.down(),
                    north, south, east, west};
        }

        final List<BlockPos> blocksToPlace = Lists.newArrayListWithCapacity(16);

        // find missing blocks (starting from under the player first and going upwards)
        for (int i = 0; i < surroundBlocks.length; i++) {
            BlockPos blockPos = surroundBlocks[i];
            if (!this.valid(blockPos))
                continue;

            blocksToPlace.add(blockPos);
        }

        if (blocksToPlace.size() != 0) { // we have blocks to place
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

            Seppuku.INSTANCE.getRotationManager().startTask(this.rotationTask);
            if (this.rotationTask.isOnline()) {
                // swap to obby
                handSwapContext.handleHandSwap(false, mc);

                if (this.center.getValue() && this.chorusTpTimer.passed(1000)) {
                    final double[] newPos = {Math.floor(mc.player.posX) + 0.5d, mc.player.posY, Math.floor(mc.player.posZ) + 0.5d};
                    final CPacketPlayer.Position middleOfPos = new CPacketPlayer.Position(newPos[0], newPos[1], newPos[2], mc.player.onGround);
                    if (!mc.world.isAirBlock(new BlockPos(newPos[0], newPos[1], newPos[2]).down())) {
                        if (mc.player.posX != middleOfPos.x && mc.player.posZ != middleOfPos.z) {
                            mc.player.connection.sendPacket(middleOfPos);
                            mc.player.setPosition(newPos[0], newPos[1], newPos[2]);
                        }
                    }
                }

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
    }

    @Listener
    public void onLoadWorld(EventLoadWorld event) {
        if (event.getWorld() != null) {
            freeCamModule = (FreeCamModule) Seppuku.INSTANCE.getModuleManager().find(FreeCamModule.class);
        }
    }

    private boolean valid(BlockPos pos) {
        // There are no entities colliding with block placement
        if (!mc.world.checkNoEntityCollision(new AxisAlignedBB(pos)))
            return false;

        // Player is too far from distance
        if (mc.player.getDistance(pos.getX(), pos.getY(), pos.getZ()) > this.range.getValue())
            return false;

        // Check if the block is replaceable
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

        if (rotate.getValue()) {
            final float[] angle = MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), new Vec3d(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f));
            Seppuku.INSTANCE.getRotationManager().setPlayerRotations(angle[0], angle[1]);
        }

        if (!visible.getValue()) {
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(sideOffset, otherSide, EnumHand.MAIN_HAND, 0.5F, 0.5F, 0.5F));
            mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
        } else {
            mc.playerController.processRightClickBlock(mc.player, mc.world, sideOffset, otherSide, new Vec3d(0.5F, 0.5F, 0.5F), EnumHand.MAIN_HAND);
            mc.player.swingArm(EnumHand.MAIN_HAND);
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
}
