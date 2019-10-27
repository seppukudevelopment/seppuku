package me.rigamortis.seppuku.impl.module.movement;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.player.EventUpdateWalkingPlayer;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.MathUtil;
import me.rigamortis.seppuku.api.value.BooleanValue;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Author Seth
 * 6/4/2019 @ 10:18 PM.
 */
public final class ScaffoldModule extends Module {

    public final BooleanValue refill = new BooleanValue("Refill", new String[]{"ref"}, true);

    public final BooleanValue destroy = new BooleanValue("Destroy", new String[]{"Dest"}, false);

    private int[] blackList = new int[]{145, 130, 12, 252, 54, 146, 122, 13, 219, 220, 221, 222, 223, 224, 225, 226, 227, 228, 229, 230, 231, 232, 233, 234, 50};

    private List<BlockPos> blocks = new CopyOnWriteArrayList<BlockPos>();

    public ScaffoldModule() {
        super("Scaffold", new String[]{"Scaff"}, "Automatically places blocks where you are walking", "NONE", -1, ModuleType.MOVEMENT);
    }

    @Override
    public String getMetaData() {
        return "" + this.getBlockCount();
    }

    @Override
    public void onToggle() {
        super.onToggle();
        this.blocks.clear();
    }

    @Listener
    public void onWalkingUpdate(EventUpdateWalkingPlayer event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            final Minecraft mc = Minecraft.getMinecraft();

            if (mc.player.noClip) {
                return;
            }

            if (this.destroy.getBoolean()) {

                double maxDist = 4.5f;
                BlockPos closest = null;

                for (BlockPos pos : this.blocks) {
                    if (mc.world.getBlockState(pos).getBlock() != Blocks.AIR && this.canBreak(pos) && mc.player.getDistance(pos.getX(), pos.getY(), pos.getZ()) <= 4.5f && mc.player.getDistance(pos.getX(), pos.getY(), pos.getZ()) >= 2 && pos != mc.player.getPosition()) {
                        final double dist = mc.player.getDistance(pos.getX(), pos.getY(), pos.getZ());
                        if (dist <= maxDist) {
                            maxDist = dist;
                            closest = pos;
                        }
                    }
                }

                if (closest != null) {
                    mc.playerController.onPlayerDamageBlock(closest, EnumFacing.DOWN);
                    mc.player.swingArm(EnumHand.MAIN_HAND);
                }
            }

            if ((mc.player.movementInput.moveForward != 0 || mc.player.movementInput.moveStrafe != 0)) {
                final double[] dir = MathUtil.directionSpeed(1);

                if (mc.player.getHeldItemMainhand().getItem() != Items.AIR && mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock && canPlace(mc.player.getHeldItemMainhand())) {
                    final Vec3d block = getFirstBlock(dir);

                    if (block != null) {
                        final BlockPos pos = new BlockPos(block.x, block.y, block.z);
                        this.placeBlock(pos);

                        if (this.destroy.getBoolean() && this.canBreak(pos)) {
                            this.blocks.add(pos);
                        }
                    }
                } else {
                    final Vec3d block = getFirstBlock(dir);

                    if (this.refill.getBoolean() && block != null) {
                        final int slot = this.findStackHotbar();
                        if (slot != -1) {
                            mc.player.inventory.currentItem = slot;
                            mc.playerController.updateController();
                        } else {
                            final int invSlot = findStackInventory();
                            if (invSlot != -1) {
                                final int empty = findEmptyhotbar();
                                mc.playerController.windowClick(mc.player.inventoryContainer.windowId, invSlot, empty == -1 ? mc.player.inventory.currentItem : empty, ClickType.SWAP, mc.player);
                                mc.playerController.updateController();
                                mc.player.setVelocity(0, 0, 0);
                            }
                        }
                    }
                }
            }
        }
    }

    private int getBlockCount() {
        int count = 0;

        for (int i = 0; i < 36; i++) {
            final ItemStack stack = Minecraft.getMinecraft().player.inventory.getStackInSlot(i);
            if (canPlace(stack) && stack.getItem() instanceof ItemBlock) {
                count += stack.getCount();
            }
        }

        return count;
    }

    private int findEmptyhotbar() {
        for (int i = 0; i < 9; i++) {
            final ItemStack stack = Minecraft.getMinecraft().player.inventory.getStackInSlot(i);
            if (stack.getItem() == Items.AIR) {
                return i;
            }
        }
        return -1;
    }

    private int findStackInventory() {
        for (int i = 9; i < 36; i++) {
            final ItemStack stack = Minecraft.getMinecraft().player.inventory.getStackInSlot(i);
            if (canPlace(stack) && stack.getItem() instanceof ItemBlock) {
                return i;
            }
        }
        return -1;
    }

    private int findStackHotbar() {
        for (int i = 0; i < 9; i++) {
            final ItemStack stack = Minecraft.getMinecraft().player.inventory.getStackInSlot(i);
            if (canPlace(stack) && stack.getItem() instanceof ItemBlock) {
                return i;
            }
        }
        return -1;
    }

    private void placeBlock(BlockPos pos) {
        final Minecraft mc = Minecraft.getMinecraft();

        final Block north = mc.world.getBlockState(pos.add(0, 0, -1)).getBlock();
        final Block south = mc.world.getBlockState(pos.add(0, 0, 1)).getBlock();
        final Block east = mc.world.getBlockState(pos.add(1, 0, 0)).getBlock();
        final Block west = mc.world.getBlockState(pos.add(-1, 0, 0)).getBlock();
        final Block up = mc.world.getBlockState(pos.add(0, 1, 0)).getBlock();
        final Block down = mc.world.getBlockState(pos.add(0, -1, 0)).getBlock();

        if (up != null && up != Blocks.AIR && !(up instanceof BlockLiquid)) {
            final boolean activated = up.onBlockActivated(mc.world, pos, mc.world.getBlockState(pos), mc.player, EnumHand.MAIN_HAND, EnumFacing.DOWN, 0, 0, 0);

            if (activated) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
            }

            if (mc.playerController.processRightClickBlock(mc.player, mc.world, pos.add(0, 1, 0), EnumFacing.DOWN, new Vec3d(0d, 0d, 0d), EnumHand.MAIN_HAND) != EnumActionResult.FAIL) {
                mc.player.swingArm(EnumHand.MAIN_HAND);
            }

            if (activated) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            }
        }

        if (down != null && down != Blocks.AIR && !(down instanceof BlockLiquid)) {
            final boolean activated = down.onBlockActivated(mc.world, pos, mc.world.getBlockState(pos), mc.player, EnumHand.MAIN_HAND, EnumFacing.UP, 0, 0, 0);

            if (activated) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
            }

            if (mc.playerController.processRightClickBlock(mc.player, mc.world, pos.add(0, -1, 0), EnumFacing.UP, new Vec3d(0d, 0d, 0d), EnumHand.MAIN_HAND) != EnumActionResult.FAIL) {
                mc.player.swingArm(EnumHand.MAIN_HAND);
            }

            if (activated) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            }
        }

        if (north != null && north != Blocks.AIR && !(north instanceof BlockLiquid)) {
            final boolean activated = north.onBlockActivated(mc.world, pos, mc.world.getBlockState(pos), mc.player, EnumHand.MAIN_HAND, EnumFacing.UP, 0, 0, 0);

            if (activated) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
            }

            if (mc.playerController.processRightClickBlock(mc.player, mc.world, pos.add(0, 0, -1), EnumFacing.SOUTH, new Vec3d(0d, 0d, 0d), EnumHand.MAIN_HAND) != EnumActionResult.FAIL) {
                mc.player.swingArm(EnumHand.MAIN_HAND);
            }

            if (activated) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            }
        }

        if (south != null && south != Blocks.AIR && !(south instanceof BlockLiquid)) {
            final boolean activated = south.onBlockActivated(mc.world, pos, mc.world.getBlockState(pos), mc.player, EnumHand.MAIN_HAND, EnumFacing.UP, 0, 0, 0);

            if (activated) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
            }

            if (mc.playerController.processRightClickBlock(mc.player, mc.world, pos.add(0, 0, 1), EnumFacing.NORTH, new Vec3d(0d, 0d, 0d), EnumHand.MAIN_HAND) != EnumActionResult.FAIL) {
                mc.player.swingArm(EnumHand.MAIN_HAND);
            }

            if (activated) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            }
        }

        if (east != null && east != Blocks.AIR && !(east instanceof BlockLiquid)) {
            final boolean activated = east.onBlockActivated(mc.world, pos, mc.world.getBlockState(pos), mc.player, EnumHand.MAIN_HAND, EnumFacing.UP, 0, 0, 0);

            if (activated) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
            }

            if (mc.playerController.processRightClickBlock(mc.player, mc.world, pos.add(1, 0, 0), EnumFacing.WEST, new Vec3d(0d, 0d, 0d), EnumHand.MAIN_HAND) != EnumActionResult.FAIL) {
                mc.player.swingArm(EnumHand.MAIN_HAND);
            }

            if (activated) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            }
        }

        if (west != null && west != Blocks.AIR && !(west instanceof BlockLiquid)) {
            final boolean activated = west.onBlockActivated(mc.world, pos, mc.world.getBlockState(pos), mc.player, EnumHand.MAIN_HAND, EnumFacing.UP, 0, 0, 0);

            if (activated) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
            }

            if (mc.playerController.processRightClickBlock(mc.player, mc.world, pos.add(-1, 0, 0), EnumFacing.EAST, new Vec3d(0d, 0d, 0d), EnumHand.MAIN_HAND) != EnumActionResult.FAIL) {
                mc.player.swingArm(EnumHand.MAIN_HAND);
            }

            if (activated) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            }
        }
    }

    private boolean canPlace(ItemStack stack) {
        for (int id = 0; id < this.blackList.length; id++) {
            if (Item.getIdFromItem(stack.getItem()) == this.blackList[id]) {
                return false;
            }
        }
        return true;
    }

    private Vec3d getFirstBlock(double[] dir) {
        for (int i = 0; i <= ((int) 4.5f); i++) {
            Vec3d pos = new Vec3d(Minecraft.getMinecraft().player.posX + -dir[0] * i, Minecraft.getMinecraft().player.posY - 1, Minecraft.getMinecraft().player.posZ + -dir[1] * i);
            Vec3d before = new Vec3d(Minecraft.getMinecraft().player.posX + -dir[0] * (i - 1), Minecraft.getMinecraft().player.posY - 1, Minecraft.getMinecraft().player.posZ + -dir[1] * (i - 1));

            final Block firstBlock = Minecraft.getMinecraft().world.getBlockState(new BlockPos(before.x, before.y, before.z)).getBlock();
            final Block secondBlock = Minecraft.getMinecraft().world.getBlockState(new BlockPos(before.x, before.y, before.z)).getBlock();

            if ((firstBlock != Blocks.AIR) || !(firstBlock instanceof BlockLiquid) && (secondBlock == Blocks.AIR) || (secondBlock instanceof BlockLiquid)) {
                return before;
            }
        }
        return null;
    }

    private boolean canBreak(BlockPos pos) {
        final IBlockState blockState = Minecraft.getMinecraft().world.getBlockState(pos);
        final Block block = blockState.getBlock();

        return block.getBlockHardness(blockState, Minecraft.getMinecraft().world, pos) != -1 && !(block instanceof BlockLiquid);
    }

    private boolean blockExists(Vec3d pos) {
        final Block block = Minecraft.getMinecraft().world.getBlockState(new BlockPos(pos.x, pos.y, pos.z)).getBlock();
        return block != Blocks.AIR;
    }

}