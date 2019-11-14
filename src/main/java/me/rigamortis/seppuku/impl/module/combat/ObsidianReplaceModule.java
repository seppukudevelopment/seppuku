package me.rigamortis.seppuku.impl.module.combat;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventReceivePacket;
import me.rigamortis.seppuku.api.event.network.EventSendPacket;
import me.rigamortis.seppuku.api.event.player.EventUpdateWalkingPlayer;
import me.rigamortis.seppuku.api.event.render.EventRender3D;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.MathUtil;
import me.rigamortis.seppuku.api.util.RenderUtil;
import me.rigamortis.seppuku.api.value.OptionalValue;
import me.rigamortis.seppuku.impl.module.player.FreeCamModule;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Author Seth
 * 8/14/2019 @ 6:01 PM.
 */
public final class ObsidianReplaceModule extends Module {

    public final OptionalValue mode = new OptionalValue("Mode", new String[]{"Mode", "M"}, 0, new String[]{"Manual", "Automatic"});

    private final Minecraft mc = Minecraft.getMinecraft();

    private List<BlockPos> blocks = new CopyOnWriteArrayList<>();

    private int lastSlot;

    public ObsidianReplaceModule() {
        super("ObsidianReplace", new String[]{"ObbyRep", "ObbyReplace", "ObbRep", "ObsidianRep"}, "Automatically replaces broken obsidian near you", "NONE", -1, ModuleType.COMBAT);
    }

    @Listener
    public void onToggle() {
        super.onToggle();
        this.blocks.clear();
    }

    @Listener
    public void onWalkingUpdate(EventUpdateWalkingPlayer event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {

            final FreeCamModule freeCam = (FreeCamModule) Seppuku.INSTANCE.getModuleManager().find(FreeCamModule.class);

            if(freeCam != null && freeCam.isEnabled()) {
                return;
            }

            if (this.hasStack(Blocks.OBSIDIAN)) {
                boolean valid = false;

                for (BlockPos pos : this.blocks) {
                    if (pos != null) {
                        final double dist = mc.player.getDistance(pos.getX(), pos.getY(), pos.getZ());
                        if (dist <= 5.0f) {
                            if (this.valid(pos)) {
                                this.place(pos);
                                valid = true;
                            }
                        }
                    }
                }

                if (valid) {
                    mc.player.inventory.currentItem = this.lastSlot;
                    mc.playerController.updateController();
                }
            } else {
                if (this.canPlace()) {
                    final int slot = this.findStackHotbar(Blocks.OBSIDIAN);
                    if (slot != -1) {
                        this.lastSlot = mc.player.inventory.currentItem;
                        mc.player.inventory.currentItem = slot;
                        mc.playerController.updateController();
                    }
                }
            }
        }
    }

    @Override
    public String getMetaData() {
        return this.mode.getSelectedOption();
    }

    private boolean canPlace() {
        for (BlockPos pos : this.blocks) {
            if (pos != null) {
                final double dist = mc.player.getDistance(pos.getX(), pos.getY(), pos.getZ());
                if (dist <= 5.0f) {
                    if (this.valid(pos)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Listener
    public void recievePacket(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (this.mode.getInt() == 1) {
                if (event.getPacket() instanceof SPacketBlockChange) {
                    final SPacketBlockChange packet = (SPacketBlockChange) event.getPacket();
                    if (packet.getBlockState().getBlock() instanceof BlockAir) {
                        final double dist = mc.player.getDistance(packet.getBlockPosition().getX(), packet.getBlockPosition().getY(), packet.getBlockPosition().getZ());
                        if (dist <= 5.0f) {
                            if (!this.blocks.contains(packet.getBlockPosition())) {
                                this.blocks.add(packet.getBlockPosition());
                            }
                        }
                    }
                }
            }
        }
    }

    @Listener
    public void sendPacket(EventSendPacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if(this.mode.getInt() == 0) {
                if (event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock) {
                    final CPacketPlayerTryUseItemOnBlock packet = (CPacketPlayerTryUseItemOnBlock) event.getPacket();
                    final ItemStack stack = mc.player.inventory.getCurrentItem();
                    if (stack != null && stack.getItem() != Items.AIR && stack.getItem() instanceof ItemBlock) {
                        final ItemBlock itemBlock = (ItemBlock) stack.getItem();
                        if (itemBlock.getBlock() instanceof BlockObsidian) {
                            BlockPos pos = packet.getPos();

                            switch (packet.getDirection()) {
                                case NORTH:
                                    pos = pos.add(0, 0, -1);
                                    break;
                                case SOUTH:
                                    pos = pos.add(0, 0, 1);
                                    break;
                                case EAST:
                                    pos = pos.add(1, 0, 0);
                                    break;
                                case WEST:
                                    pos = pos.add(-1, 0, 0);
                                    break;
                                case UP:
                                    pos = pos.add(0, 1, 0);
                                    break;
                                case DOWN:
                                    pos = pos.add(0, -1, 0);
                                    break;
                            }

                            if (!this.blocks.contains(pos)) {
                                this.blocks.add(pos);
                            }
                        }
                    }
                }
            }
        }
    }

    @Listener
    public void render3D(EventRender3D event) {
        for (BlockPos pos : this.blocks) {
            if (pos != null) {
                final double dist = mc.player.getDistance(pos.getX(), pos.getY(), pos.getZ());
                if (dist <= 5.0f) {
                    final IBlockState iblockstate = mc.world.getBlockState(pos);

                    if (iblockstate.getMaterial() != Material.AIR && mc.world.getWorldBorder().contains(pos)) {
                        final Vec3d interp = MathUtil.interpolateEntity(mc.player, mc.getRenderPartialTicks());
                        RenderUtil.drawBoundingBox(iblockstate.getSelectedBoundingBox(mc.world, pos).grow(0.0020000000949949026D).offset(-interp.x, -interp.y, -interp.z), 1.5f, 0xFF9900EE);
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

        if (!(block instanceof BlockAir) && !(block instanceof BlockLiquid)) {
            return false;
        }

        for (Entity entity : mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos))) {
            if (!(entity instanceof EntityItem) && !(entity instanceof EntityXPOrb)) {
                return false;
            }
        }

        final Block up = mc.world.getBlockState(pos.add(0, 1, 0)).getBlock();
        final Block down = mc.world.getBlockState(pos.add(0, -1, 0)).getBlock();
        final Block north = mc.world.getBlockState(pos.add(0, 0, -1)).getBlock();
        final Block south = mc.world.getBlockState(pos.add(0, 0, 1)).getBlock();
        final Block east = mc.world.getBlockState(pos.add(1, 0, 0)).getBlock();
        final Block west = mc.world.getBlockState(pos.add(-1, 0, 0)).getBlock();

        return ((up != null && up != Blocks.AIR && !(up instanceof BlockLiquid))
                || (down != null && down != Blocks.AIR && !(down instanceof BlockLiquid))
                || (north != null && north != Blocks.AIR && !(north instanceof BlockLiquid))
                || (south != null && south != Blocks.AIR && !(south instanceof BlockLiquid))
                || (east != null && east != Blocks.AIR && !(east instanceof BlockLiquid))
                || (west != null && west != Blocks.AIR && !(west instanceof BlockLiquid)));
    }

}
