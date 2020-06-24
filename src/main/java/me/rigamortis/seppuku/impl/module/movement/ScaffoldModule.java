package me.rigamortis.seppuku.impl.module.movement;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.player.EventUpdateWalkingPlayer;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.MathUtil;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
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

    public final Value<Boolean> refill = new Value<Boolean>("Refill", new String[]{"ref"}, "If the held item is empty or not a block, fill the slot with a block from the inventory when the scaffold is triggered to place.", true);
    public final Value<Boolean> destroy = new Value<Boolean>("Destroy", new String[]{"Dest"}, "When enabled, after placing the block, forces the player to swing/destroy at the same position.", false);

    private int[] blackList = new int[]{145, 130, 12, 252, 54, 146, 122, 13, 219, 220, 221, 222, 223, 224, 225, 226, 227, 228, 229, 230, 231, 232, 233, 234, 50};

    private List<BlockPos> blocks = new CopyOnWriteArrayList<BlockPos>();

    public ScaffoldModule() {
        super("Scaffold", new String[]{"Scaff"}, "Automatically places blocks where you are walking.", "NONE", -1, ModuleType.MOVEMENT);
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

            if (this.destroy.getValue()) {
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

            if ((mc.player.movementInput.moveForward != 0 || mc.player.movementInput.moveStrafe != 0 || mc.player.movementInput.jump) && !mc.player.movementInput.sneak) {
                final double[] dir = MathUtil.directionSpeed(1);

                if (mc.player.getHeldItemMainhand().getItem() != Items.AIR && mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock && canPlace(mc.player.getHeldItemMainhand())) {
                    final Vec3d block = getFirstBlock(dir);

                    if (block != null) {
                        final BlockPos pos = new BlockPos(block.x, block.y, block.z);
                        this.placeBlock(pos);

                        if (this.destroy.getValue() && this.canBreak(pos)) {
                            this.blocks.add(pos);
                        }
                    }
                } else {
                    final Vec3d block = getFirstBlock(dir);

                    if (this.refill.getValue() && block != null) {
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
        
        BlockPos[][] posit = {{pos.add(0, 0, 1), pos.add(0, 0, -1)}, {pos.add(0, 1, 0), pos.add(0, -1, 0)}, {pos.add(1, 0, 0),pos.add(-1, 0, 0)}};
        EnumFacing[][] facing = {{EnumFacing.NORTH, EnumFacing.SOUTH}, {EnumFacing.DOWN, EnumFacing.UP}, {EnumFacing.WEST, EnumFacing.EAST}}; // Facing reversed as blocks are placed while facing in the opposite direction

        for (int i=0; i<6; i++) {
            final Block block = mc.world.getBlockState(posit[i/2][i%2]).getBlock();
            final boolean activated = block.onBlockActivated(mc.world, pos, mc.world.getBlockState(pos), mc.player, EnumHand.MAIN_HAND, EnumFacing.UP, 0, 0, 0);
            if (block != null && block != Blocks.AIR && !(block instanceof BlockLiquid)) {
                if (activated)
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                if (mc.playerController.processRightClickBlock(mc.player, mc.world, posit[i/2][i%2], facing[i/2][i%2], new Vec3d(0d, 0d, 0d), EnumHand.MAIN_HAND) != EnumActionResult.FAIL)
                    mc.player.swingArm(EnumHand.MAIN_HAND);
                if (activated)
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
        final Minecraft mc = Minecraft.getMinecraft();
        Vec3d pos = new Vec3d(mc.player.posX, mc.player.posY - 1, mc.player.posZ);
        Vec3d dirpos = new Vec3d(mc.player.posX + dir[0], mc.player.posY - 1, mc.player.posZ + dir[1]);
        if (mc.world.getBlockState(new BlockPos(pos.x, pos.y, pos.z)).getBlock() == Blocks.AIR)
            return pos;
        if (mc.world.getBlockState(new BlockPos(dirpos.x, dirpos.y, dirpos.z)).getBlock() == Blocks.AIR)
            if (mc.world.getBlockState(new BlockPos(pos.x, dirpos.y, dirpos.z)).getBlock() == Blocks.AIR && mc.world.getBlockState(new BlockPos(dirpos.x, dirpos.y, pos.z)).getBlock() == Blocks.AIR) {
                return new Vec3d(dirpos.x, pos.y, pos.z);
            } else {
                return dirpos;
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
