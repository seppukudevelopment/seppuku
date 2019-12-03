package me.rigamortis.seppuku.impl.module.combat;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.player.EventUpdateWalkingPlayer;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.MathUtil;
import me.rigamortis.seppuku.api.util.Timer;
import me.rigamortis.seppuku.api.value.Value;
import me.rigamortis.seppuku.impl.module.player.FreeCamModule;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
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

    public final Value<Boolean> disable = new Value<Boolean>("Disable", new String[]{"dis"}, "Automatically disable after it places.", false);
    public final Value<Boolean> sneak = new Value<Boolean> ("PlaceOnSneak", new String[]{"sneak", "s", "pos", "sneakPlace"}, "When false, NoCrystal will not place while the player is sneaking.", false);
    public final Value<Float> placeDelay = new Value("Delay", new String[]{"PlaceDelay", "PlaceDel"}, "The delay between obsidian blocks being placed.", 100.0f, 0.0f, 1000.0f, 1.0f);


    private Timer placeTimer = new Timer();
    private int placeIndex;

    public NoCrystalModule() {
        super("NoCrystal", new String[]{"AntiCrystal", "FeetPlace", "Surround"}, "Automatically places obsidian in 4 cardinal directions", "NONE", -1, ModuleType.COMBAT);
    }

    @Listener
    public void onWalkingUpdate(EventUpdateWalkingPlayer event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            final FreeCamModule freeCam = (FreeCamModule) Seppuku.INSTANCE.getModuleManager().find(FreeCamModule.class);

            if(freeCam != null && freeCam.isEnabled()) {
                return;
            }

            final Vec3d pos = MathUtil.interpolateEntity(mc.player, mc.getRenderPartialTicks());
            final float playerSpeed = (float) MathUtil.getDistance(pos, mc.player.posX, mc.player.posY, mc.player.posZ);

            final BlockPos interpPos = new BlockPos(pos.x, pos.y, pos.z);

            final BlockPos north = interpPos.north();
            final BlockPos south = interpPos.south();
            final BlockPos east = interpPos.east();
            final BlockPos west = interpPos.west();

            final BlockPos northBelow = interpPos.north().down();
            final BlockPos southBelow = interpPos.south().down();
            final BlockPos eastBelow = interpPos.east().down();
            final BlockPos westBelow = interpPos.west().down();

            int lastSlot;
            final int slot = findStackHotbar(Blocks.OBSIDIAN);
            if (hasStack(Blocks.OBSIDIAN) || slot != -1) {
                if ((mc.player.onGround && playerSpeed <= 0.005f)
                        && (this.sneak.getValue() || (!mc.gameSettings.keyBindSneak.isKeyDown()))) {

                    lastSlot = mc.player.inventory.currentItem;
                    mc.player.inventory.currentItem = slot;
                    mc.playerController.updateController();
                    if (this.placeTimer.passed(this.placeDelay.getValue())) {
                        switch (placeIndex) {
                            // Place supporting blocks
                            case 0:
                                if (valid(northBelow)) {
                                    place(northBelow, EnumFacing.SOUTH);
                                    break;
                                }
                            case 1:
                                if (valid(southBelow)) {
                                    place(southBelow, EnumFacing.NORTH);
                                    break;
                                }
                            case 2:
                                if (valid(eastBelow)) {
                                    place(eastBelow, EnumFacing.WEST);
                                    break;
                                }
                            case 3:
                                if (valid(westBelow)) {
                                    place(westBelow, EnumFacing.EAST);
                                    break;
                                }
                            // Place protecting blocks
                            case 4:
                                if (valid(north)) {
                                    place(north, EnumFacing.SOUTH);
                                    break;
                                }
                            case 5:
                                if (valid(south)) {
                                    place(south, EnumFacing.NORTH);
                                    break;
                                }
                            case 6:
                                if (valid(east)) {
                                    place(east, EnumFacing.WEST);
                                    break;
                                }
                            case 7:
                                if (valid(west)) {
                                    place(west, EnumFacing.EAST);
                                }
                                placeIndex = 0;
                                if (this.disable.getValue()) {
                                    this.toggle();
                                }
                                break;
                        }
                        this.placeTimer.reset();
                        placeIndex++;
                    }
                    if (!slotEqualsBlock(lastSlot, Blocks.OBSIDIAN)) {
                        mc.player.inventory.currentItem = lastSlot;
                    }
                    mc.playerController.updateController();
                }
            }
        }
    }

    private boolean hasStack(Block type) {
        if (mc.player.inventory.getCurrentItem().getItem() instanceof ItemBlock) {
            final ItemBlock block = (ItemBlock) mc.player.inventory.getCurrentItem().getItem();
            return block.getBlock() == type;
        }
        return false;
    }

    private boolean slotEqualsBlock (int slot, Block type) {
        if (mc.player.inventory.getStackInSlot(slot).getItem() instanceof ItemBlock) {
            final ItemBlock block = (ItemBlock) mc.player.inventory.getStackInSlot(slot).getItem();
            return block.getBlock() == type;
        }

        return false;
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

    private boolean valid (BlockPos pos) {
        // There are no entities to block placement,
        if (!mc.world.checkNoEntityCollision(new AxisAlignedBB(pos))) {
            return false;
        }

        // Check if the block is replaceable
        return mc.world.getBlockState(pos).getBlock().isReplaceable(mc.world, pos);
    }

    private void place (BlockPos pos, EnumFacing direction) {
        final Block block = mc.world.getBlockState(pos).getBlock();
        final boolean activated = block.onBlockActivated(mc.world, pos, mc.world.getBlockState(pos), mc.player, EnumHand.MAIN_HAND, direction, 0, 0, 0);

        if (activated) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
        }

        this.mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, direction, EnumHand.MAIN_HAND, 0.0F, 0.0F, 0.0F));

        if (activated) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
        }
    }
}
