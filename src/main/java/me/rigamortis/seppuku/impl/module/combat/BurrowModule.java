package me.rigamortis.seppuku.impl.module.combat;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.player.EventUpdateWalkingPlayer;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.task.hand.HandSwapContext;
import me.rigamortis.seppuku.api.task.rotation.RotationTask;
import me.rigamortis.seppuku.api.util.InventoryUtil;
import me.rigamortis.seppuku.api.util.MathUtil;
import me.rigamortis.seppuku.api.util.Timer;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * @author noil
 */
public final class BurrowModule extends Module {

    public final Value<Float> delay = new Value<>("Delay", new String[]{"del", "d"}, "Delay(ms) to wait for placing obsidian after the initial jump.", 200.0f, 1.0f, 500.0f, 1.0f);
    public final Value<Boolean> rotate = new Value<>("Rotate", new String[]{"rot", "r"}, "Rotate the players head to place the block.", true);
    public final Value<Boolean> center = new Value<Boolean>("Center", new String[]{"centered", "c", "cen"}, "Centers the player on their current block when beginning to place.", false);

    private final Timer timer = new Timer();
    private final RotationTask rotationTask = new RotationTask("BurrowTask", 9); // 9 == high priority

    public BurrowModule() {
        super("Burrow", new String[]{"burow", "burro", "brrw"}, "Places obsidian on yourself.", "NONE", -1, ModuleType.COMBAT);
    }

    @Override
    public void onEnable() {
        super.onEnable();

        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.player != null) {
            // attempt to center
            if (this.center.getValue()) {
                final double[] newPos = {Math.floor(mc.player.posX) + 0.5d, mc.player.posY, Math.floor(mc.player.posZ) + 0.5d};
                final CPacketPlayer.Position middleOfPos = new CPacketPlayer.Position(newPos[0], newPos[1], newPos[2], mc.player.onGround);
                if (!mc.world.isAirBlock(new BlockPos(newPos[0], newPos[1], newPos[2]).down())) {
                    if (mc.player.posX != middleOfPos.x && mc.player.posZ != middleOfPos.z) {
                        mc.player.connection.sendPacket(middleOfPos);
                        mc.player.setPosition(newPos[0], newPos[1], newPos[2]);
                    }
                }
            }

            mc.player.jump(); // jump
            this.timer.reset(); // start timer
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        Seppuku.INSTANCE.getRotationManager().finishTask(this.rotationTask);
    }

    @Listener
    public void onUpdateWalkingPlayer(EventUpdateWalkingPlayer event) {
        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || mc.world == null)
            return;

        switch (event.getStage()) {
            case PRE:
                if (this.timer.passed(this.delay.getValue())) {
                    if (InventoryUtil.getBlockCount(Blocks.OBSIDIAN) > 0) {
                        // get our hand swap context and ensure we have obsidian
                        final HandSwapContext handSwapContext = new HandSwapContext(
                                mc.player.inventory.currentItem, InventoryUtil.findObsidianInHotbar(mc.player));
                        if (handSwapContext.getNewSlot() == -1) {
                            Seppuku.INSTANCE.getRotationManager().finishTask(this.rotationTask);
                            return;
                        }

                        Seppuku.INSTANCE.getRotationManager().startTask(this.rotationTask);
                        if (this.rotationTask.isOnline()) {
                            // swap to obby
                            handSwapContext.handleHandSwap(false, mc);

                            // get our block pos to place at
                            final BlockPos positionToPlaceAt = new BlockPos(mc.player.getPositionVector()).down();
                            if (this.place(positionToPlaceAt, mc)) { // we've attempted to place the block
                                mc.player.onGround = false; // set onground to false
                                mc.player.jump(); // attempt another jump to flag ncp
                            }

                            // swap back to original
                            handSwapContext.handleHandSwap(true, mc);

                            this.toggle(); // toggle off the module
                        }
                    }
                }
                break;
            case POST:
                break;
        }
    }

    private EnumFacing calcSide(BlockPos pos) {
        for (EnumFacing side : EnumFacing.values()) {
            BlockPos sideOffset = pos.offset(side);
            IBlockState offsetState = Minecraft.getMinecraft().world.getBlockState(sideOffset);
            if (!offsetState.getBlock().canCollideCheck(offsetState, false)) continue;
            if (!offsetState.getMaterial().isReplaceable()) return side;
        }
        return null;
    }

    private boolean place(final BlockPos pos, final Minecraft mc) {
        final Block block = mc.world.getBlockState(pos).getBlock();

        final EnumFacing direction = this.calcSide(pos);
        if (direction == null)
            return false;

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

        if (activated)
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));

        return true;
    }
}
