package me.rigamortis.seppuku.impl.module.world;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.player.EventUpdateWalkingPlayer;
import me.rigamortis.seppuku.api.event.render.EventRender3D;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.task.rotation.RotationTask;
import me.rigamortis.seppuku.api.util.BlockUtil;
import me.rigamortis.seppuku.api.util.MathUtil;
import me.rigamortis.seppuku.api.util.RenderUtil;
import me.rigamortis.seppuku.api.util.Timer;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumSkyBlock;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * @author Seth
 * @author noil
 */
public final class AutoTorchModule extends Module {

    public final Value<Boolean> rotate = new Value<Boolean>("Rotate", new String[]{"rotation", "r", "rotate"}, "Rotate to place blocks.", true);
    public final Value<Boolean> swap = new Value<Boolean>("Swap", new String[]{"autoswap", "sw"}, "Automatically swaps to torches in your hotbar.", true);
    public final Value<Boolean> overlay = new Value<Boolean>("Overlay", new String[]{"overlay", "ov", "o"}, "Renders an overlay of the light level on blocks within your reach.", false);
    public final Value<Integer> light = new Value<>("Light", new String[]{"MaxLight", "MinLight", "LightLevel"}, "The minimum light level to place a light source.", 8, 0, 15, 1);
    public final Value<Float> placeDelay = new Value<Float>("Delay", new String[]{"PlaceDelay", "PlaceDel"}, "The delay(ms) between blocks being placed.", 100.0f, 0.0f, 500.0f, 1.0f);

    private final RotationTask rotationTask = new RotationTask("AutoTorch", 2);
    private final Minecraft mc = Minecraft.getMinecraft();
    private final Timer timer = new Timer();
    private BlockPos currentPos;

    private int previousHeldItem = -1;
    private int torchSlot = -1;

    public AutoTorchModule() {
        super("AutoTorch", new String[]{"ATorch", "AutoLight", "TorchMaster", "Torch+"}, "Automatically places torches in low light levels.", "NONE", -1, ModuleType.WORLD);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        if (mc.world == null)
            return;

        Seppuku.INSTANCE.getRotationManager().finishTask(this.rotationTask);
    }

    @Override
    public String getMetaData() {
        return "" + this.getTorchCount();
    }

    @Listener
    public void onUpdate(EventUpdateWalkingPlayer event) {
        this.torchSlot = this.findTorch(mc.player); // find a torch slot in the hotbar
        final boolean swap = this.swap.getValue();

        if (!swap) { // if swap is off, don't continue the code unless we are holding a torch
            if (mc.player.getHeldItemMainhand().getItem() != ItemBlock.getItemFromBlock(Blocks.TORCH)) {
                return;
            }
        } else {
            if (torchSlot == -1) { // no torches found anywhere in inventory, do nothing more
                return;
            }
        }

        switch (event.getStage()) {
            case PRE:
                if (this.timer.passed(this.placeDelay.getValue())) { // wait for our delay
                    this.currentPos = this.getPos(this.light.getValue()); // get our place position
                    if (this.currentPos != null) {
                        if (swap) { // swap items if enabled
                            if (this.previousHeldItem == -1) {
                                this.previousHeldItem = mc.player.inventory.currentItem;
                            }
                        }
                        if (this.rotate.getValue()) { // rotate the player if enabled
                            this.lookAtPosition(this.currentPos);
                        }
                    }
                    this.timer.reset(); // reset our delay timer
                }
                break;
            case POST:
                if (this.rotationTask.isOnline() || (this.currentPos != null && !this.rotate.getValue())) { // the position isn't null, so we need to place a torch there
                    // swap to torch
                    if (swap) {
                        mc.player.inventory.currentItem = this.torchSlot;
                    }

                    // right click the block on top of it
                    mc.playerController.processRightClickBlock(mc.player, mc.world, this.currentPos, EnumFacing.UP, new Vec3d(0.5F, 0.5F, 0.5F), EnumHand.MAIN_HAND);
                    // swing arm
                    mc.player.swingArm(EnumHand.MAIN_HAND);

                    // swap back to original item when finished
                    if (swap) {
                        if (this.previousHeldItem != -1) {
                            mc.player.inventory.currentItem = this.previousHeldItem;
                        }
                        this.torchSlot = -1;
                        this.previousHeldItem = -1;
                    }

                    // reset position
                    this.currentPos = null;

                    // finish rotation task
                    if (this.rotate.getValue()) {
                        Seppuku.INSTANCE.getRotationManager().finishTask(this.rotationTask);
                    }
                }
                break;
        }
    }

    @Listener
    public void onDrawWorld(EventRender3D event) {
        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.getRenderViewEntity() == null || !this.overlay.getValue())
            return;

        // begin our render constants
        RenderUtil.begin3D();

        // the distance to search for light levels within (currently using place reach distance, use the 'Reach' module to extend this range.)
        final float dist = mc.playerController.getBlockReachDistance();

        // loop through all the blocks around our player within 'dist'
        for (double y = Math.round(mc.player.posY + dist); y > Math.round(mc.player.posY - dist); y -= 1.0D) {
            for (double x = mc.player.posX - dist; x < mc.player.posX + dist; x += 1.0D) {
                for (double z = mc.player.posZ - dist; z < mc.player.posZ + dist; z += 1.0D) {
                    final BlockPos blockPos = new BlockPos(x, y, z);
                    final Block block = BlockUtil.getBlock(blockPos);
                    final IBlockState state = mc.world.getBlockState(blockPos);

                    if (block == Blocks.AIR || !state.isFullBlock())
                        continue;

                    final BlockPos aboveBlockPos = blockPos.up();
                    final Block aboveBlock = BlockUtil.getBlock(aboveBlockPos);
                    if (aboveBlock == Blocks.AIR || aboveBlock.isReplaceable(mc.world, aboveBlockPos)) {
                        final int light = mc.world.getChunk(aboveBlockPos).getLightFor(EnumSkyBlock.BLOCK, aboveBlockPos);
                        if (light < 15) {
                            float mappedColor = (float) MathUtil.map(light, 0.0D, 15, 0.0D, 255.0D);
                            final float[] color = new float[]{255.0F - mappedColor, mappedColor, 0.0F};
                            final AxisAlignedBB bb = new AxisAlignedBB(
                                    blockPos.getX() - mc.getRenderManager().viewerPosX, blockPos.getY() - mc.getRenderManager().viewerPosY + 1, blockPos.getZ() - mc.getRenderManager().viewerPosZ,
                                    blockPos.getX() + 1.0f - mc.getRenderManager().viewerPosX, blockPos.getY() - mc.getRenderManager().viewerPosY + 1, blockPos.getZ() + 1.0f - mc.getRenderManager().viewerPosZ);
                            GlStateManager.color(color[0] / 255.0F, color[1] / 255.0F, color[2] / 255.0F, 0.2F);
                            RenderUtil.drawFilledBox(bb);
                        }
                    }
                }
            }
        }

        RenderUtil.end3D();
    }

    private void lookAtPosition(BlockPos position) {
        Seppuku.INSTANCE.getRotationManager().startTask(this.rotationTask);
        if (this.rotationTask.isOnline()) {
            final float[] angle = MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), new Vec3d(position.getX() + 0.5f, position.getY() + 0.5f, position.getZ() + 0.5f));
            Seppuku.INSTANCE.getRotationManager().setPlayerRotations(angle[0], angle[1]);
        }
    }

    private BlockPos getPos(int maxLight) {
        final float dist = mc.playerController.getBlockReachDistance();
        double maxDist = dist;
        BlockPos pos = null;

        for (double y = Math.round(mc.player.posY + dist); y > Math.round(mc.player.posY - dist); y -= 1.0D) {
            for (double x = mc.player.posX - dist; x < mc.player.posX + dist; x += 1.0D) {
                for (double z = mc.player.posZ - dist; z < mc.player.posZ + dist; z += 1.0D) {
                    final BlockPos blockPos = new BlockPos(x, y, z);
                    final Block block = BlockUtil.getBlock(blockPos);
                    final IBlockState state = mc.world.getBlockState(blockPos);

                    if (block == Blocks.AIR || !state.isFullBlock())
                        continue;

                    final BlockPos aboveBlockPos = blockPos.up();
                    final Block aboveBlock = BlockUtil.getBlock(aboveBlockPos);
                    if (aboveBlock == Blocks.AIR || aboveBlock.isReplaceable(mc.world, aboveBlockPos)) {
                        final int light = mc.world.getChunk(blockPos).getLightFor(EnumSkyBlock.BLOCK, aboveBlockPos);
                        final double curDist = mc.player.getDistance(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                        if (light < maxLight && curDist <= maxDist) {
                            maxDist = curDist;
                            pos = blockPos;
                        }
                    }
                }
            }
        }
        return pos;
    }

    private int getTorchCount() {
        int torches = 0;

        if (Minecraft.getMinecraft().player == null)
            return torches;

        for (int index = 0; InventoryPlayer.isHotbar(index); index++) {
            final ItemStack stack = Minecraft.getMinecraft().player.inventory.getStackInSlot(index);
            if (this.isItemStackTorch(stack)) {
                torches += stack.getCount();
            }
        }

        return torches;
    }

    private boolean isItemStackTorch(final ItemStack itemStack) {
        return itemStack.getItem() == ItemBlock.getItemFromBlock(Blocks.TORCH);
    }

    private int findTorch(final EntityPlayerSP player) {
        for (int index = 0; InventoryPlayer.isHotbar(index); index++)
            if (this.isItemStackTorch(player.inventory.getStackInSlot(index)))
                return index;

        return -1;
    }
}
