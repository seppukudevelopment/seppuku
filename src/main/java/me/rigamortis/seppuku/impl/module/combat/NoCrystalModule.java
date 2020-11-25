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
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * @author Seth
 * @since 5/15/2019 @ 9:20 AM.
 */
public final class NoCrystalModule extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();

    public final Value<Boolean> visible = new Value("Visible", new String[]{"Visible", "v"}, "When disabled, you will not see swing animations or sounds.", true);
    public final Value<Boolean> rotate = new Value("Rotate", new String[]{"rotation", "r", "rotate"}, "Rotate to place blocks", true);
    public final Value<Boolean> disable = new Value<Boolean>("Disable", new String[]{"dis"}, "Automatically disable after obsidian is placed.", false);
    public final Value<Boolean> sneak = new Value<Boolean>("PlaceOnSneak", new String[]{"sneak", "s", "pos", "sneakPlace"}, "When true, NoCrystal will only place while the player is sneaking.", false);
    public final Value<Float> placeDelay = new Value("Delay", new String[]{"PlaceDelay", "PlaceDel"}, "The delay between obsidian blocks being placed.", 100.0f, 0.0f, 1000.0f, 1.0f);


    private Timer placeTimer = new Timer();
    private int placeIndex = 0;

    public NoCrystalModule() {
        super("NoCrystal", new String[]{"AntiCrystal", "FeetPlace", "Surround"}, "Automatically places obsidian around you to avoid crystal damage", "NONE", -1, ModuleType.COMBAT);
    }

    @Listener
    public void onWalkingUpdate(EventUpdateWalkingPlayer event) {
        final boolean instant = placeDelay.getValue() == 0.0f;
        if (instant || this.placeTimer.passed(this.placeDelay.getValue())) {
            if (event.getStage() == EventStageable.EventStage.PRE) {

                final FreeCamModule freeCam = (FreeCamModule) Seppuku.INSTANCE.getModuleManager().find(FreeCamModule.class);
                if (freeCam != null && freeCam.isEnabled()) return;

                final Vec3d pos = MathUtil.interpolateEntity(mc.player, mc.getRenderPartialTicks());
                final float playerSpeed = (float) MathUtil.getDistance(pos, mc.player.posX, mc.player.posY, mc.player.posZ);

                final BlockPos interpPos = new BlockPos(pos.x, pos.y, pos.z);

                final BlockPos north = interpPos.north();
                final BlockPos south = interpPos.south();
                final BlockPos east = interpPos.east();
                final BlockPos west = interpPos.west();

                final BlockPos[] surroundBlocks = {north.down(), south.down(), east.down(), west.down(),
                        north, south, east, west};

                int lastSlot = 0;
                final int slot = findStackHotbar(Blocks.OBSIDIAN);
                if (slot != -1) {
                    // 0.005f: Absolute minimum velocity to register as standing still.
                    // Don't ask me why, I'm just a comment.
                    if ((mc.player.onGround && playerSpeed <= 0.005f) && (this.sneak.getValue() || (!mc.gameSettings.keyBindSneak.isKeyDown()))) {
                        lastSlot = mc.player.inventory.currentItem;
                        mc.player.inventory.currentItem = slot;
                        mc.playerController.updateController();

                        place(surroundBlocks[placeIndex]);

                        if (!instant) this.placeTimer.reset();
                        if (placeIndex >= surroundBlocks.length - 1) {
                            placeIndex = 0;
                            if (this.disable.getValue()) this.toggle();
                        } else placeIndex++;
                    }
                    if (!slotEqualsBlock(lastSlot, Blocks.OBSIDIAN)) mc.player.inventory.currentItem = lastSlot;
                    mc.playerController.updateController();
                }
            }
        }
    }

    private boolean slotEqualsBlock(int slot, Block type) {
        if (mc.player.inventory.getStackInSlot(slot).getItem() instanceof ItemBlock) {
            final ItemBlock block = (ItemBlock) mc.player.inventory.getStackInSlot(slot).getItem();
            return block.getBlock() == type;
        }
        return false;
    }

    private int findStackHotbar(Block type) {
        for (int i = 0; i < 9; i++) {
            if (slotEqualsBlock(i, type)) return i;
        }
        return -1;
    }

    private boolean valid(BlockPos pos) {
        // There are no entities to block placement,
        if (!mc.world.checkNoEntityCollision(new AxisAlignedBB(pos))) return false;
        // Check if the block is replaceable
        return mc.world.getBlockState(pos).getBlock().isReplaceable(mc.world, pos);
    }

    private void place(BlockPos pos) {
        final Block block = mc.world.getBlockState(pos).getBlock();
        final EnumFacing direction = calcSide(pos);
        final boolean activated = block.onBlockActivated(mc.world, pos, mc.world.getBlockState(pos), mc.player, EnumHand.MAIN_HAND, direction, 0, 0, 0);

        if (!valid(pos)) return;

        if (activated)
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
        if (direction != null) {
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
