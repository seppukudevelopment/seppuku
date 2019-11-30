package me.rigamortis.seppuku.impl.module.combat;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.player.EventUpdateWalkingPlayer;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.MathUtil;
import me.rigamortis.seppuku.api.value.Value;
import me.rigamortis.seppuku.impl.module.player.FreeCamModule;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 5/15/2019 @ 9:20 AM.
 */
public final class NoCrystalModule extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();

    public final Value<Boolean> disable = new Value("Disable", new String[]{"dis"}, "Automatically disable after it places.", false);

    private int lastSlot;

    public NoCrystalModule() {
        super("NoCrystal", new String[]{"AntiCrystal", "FeetPlace"}, "Automatically places obsidian in 4 cardinal directions", "NONE", -1, ModuleType.COMBAT);
    }

    @Listener
    public void onWalkingUpdate(EventUpdateWalkingPlayer event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {

            final FreeCamModule freeCam = (FreeCamModule) Seppuku.INSTANCE.getModuleManager().find(FreeCamModule.class);

            if(freeCam != null && freeCam.isEnabled()) {
                return;
            }

            final Vec3d pos = MathUtil.interpolateEntity(mc.player, mc.getRenderPartialTicks());

            final BlockPos interpPos = new BlockPos(pos.x, pos.y, pos.z);

            final BlockPos north = interpPos.north();
            final BlockPos south = interpPos.south();
            final BlockPos east = interpPos.east();
            final BlockPos west = interpPos.west();

            final boolean canPlace = valid(north) || valid(south) || valid(east) || valid(west);

            if (hasStack(Blocks.OBSIDIAN)) {
                if ((mc.player.onGround && mc.player.movementInput.moveForward == 0 && mc.player.movementInput.moveStrafe == 0)) {
                    if (valid(north)) {
                        place(north);
                    }
                    if (valid(south)) {
                        place(south);
                    }
                    if (valid(east)) {
                        place(east);
                    }
                    if (valid(west)) {
                        place(west);
                    }

                    mc.player.inventory.currentItem = this.lastSlot;
                    mc.playerController.updateController();

                    if(this.disable.getValue()) {
                        this.toggle();
                    }
                }
            }else{
                if (canPlace && (mc.player.onGround && mc.player.movementInput.moveForward == 0 && mc.player.movementInput.moveStrafe == 0)) {
                    final int slot = findStackHotbar(Blocks.OBSIDIAN);
                    if (slot != -1) {
                        this.lastSlot = mc.player.inventory.currentItem;
                        mc.player.inventory.currentItem = slot;
                        mc.playerController.updateController();
                    }
                }
            }
        }
    }

    private boolean hasStack(Block type) {
        if (mc.player.inventory.getCurrentItem().getItem() instanceof ItemBlock) {
            final ItemBlock block = (ItemBlock) mc.player.inventory.getCurrentItem().getItem();
            if (block.getBlock() == type) {
                return true;
            }
        }
        return false;
    }

    private void place(BlockPos pos) {
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

    private int findStackHotbar(Block type) {
        for (int i = 0; i < 9; i++) {
            final ItemStack stack = Minecraft.getMinecraft().player.inventory.getStackInSlot(i);
            if (stack.getItem() instanceof ItemBlock) {
                final ItemBlock block = (ItemBlock) stack.getItem();

                if (block.getBlock() == type) {
                    return i;
                }
            }
        }
        return -1;
    }

    private boolean valid(BlockPos pos) {
        final Block block = mc.world.getBlockState(pos).getBlock();

        final Block up = mc.world.getBlockState(pos.add(0, 1, 0)).getBlock();
        final Block down = mc.world.getBlockState(pos.add(0, -1, 0)).getBlock();
        final Block north = mc.world.getBlockState(pos.add(0, 0, -1)).getBlock();
        final Block south = mc.world.getBlockState(pos.add(0, 0, 1)).getBlock();
        final Block east = mc.world.getBlockState(pos.add(1, 0, 0)).getBlock();
        final Block west = mc.world.getBlockState(pos.add(-1, 0, 0)).getBlock();

        return (block instanceof BlockAir)
                && mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos)).isEmpty()
                && ((up != null && up != Blocks.AIR && !(up instanceof BlockLiquid))
                || (down != null && down != Blocks.AIR && !(down instanceof BlockLiquid))
                || (north != null && north != Blocks.AIR && !(north instanceof BlockLiquid))
                || (south != null && south != Blocks.AIR && !(south instanceof BlockLiquid))
                || (east != null && east != Blocks.AIR && !(east instanceof BlockLiquid))
                || (west != null && west != Blocks.AIR && !(west instanceof BlockLiquid)));
    }


}
