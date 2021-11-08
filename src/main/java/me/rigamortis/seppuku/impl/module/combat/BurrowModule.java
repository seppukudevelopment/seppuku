package me.rigamortis.seppuku.impl.module.combat;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.player.EventUpdateWalkingPlayer;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.notification.Notification;
import me.rigamortis.seppuku.api.task.hand.HandSwapContext;
import me.rigamortis.seppuku.api.task.rotation.RotationTask;
import me.rigamortis.seppuku.api.util.InventoryUtil;
import me.rigamortis.seppuku.api.util.MathUtil;
import me.rigamortis.seppuku.api.util.Timer;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * @author noil
 */
public final class BurrowModule extends Module {

    public enum Mode {
        JUMP, GLITCH, TP, INSTA
    }

    public final Value<Mode> mode = new Value<>("Mode", new String[]{"m"}, "The current mode to use.", Mode.JUMP);
    public final Value<Float> glitchY = new Value<>("ModeGlitchY", new String[]{"mgy", "glitchy"}, "Using GLITCH mode, this will be your player's motionY", 0.5f, 0.1f, 1.5f, 0.1f);
    public final Value<Integer> tpHeight = new Value<>("ModeTPHeight", new String[]{"mtph", "mth", "tpheight", "tpy"}, "Using TP mode, this will be how many blocks above the player to TP", 20, -30, 30, 1);
    public final Value<Float> delay = new Value<>("ModeDelay", new String[]{"del", "d"}, "Delay(ms) to wait for placing obsidian after the initial jump. Not used in INSTA mode", 200.0f, 1.0f, 500.0f, 1.0f);
    public final Value<Boolean> smartTp = new Value<>("ModeSmartTp", new String[]{"smart", "adaptivetp", "a", "atp"}, "Searches for an air block to tp to for INSTA mode", true);
    public final Value<Boolean> noVoid = new Value<>("ModeNoVoid", new String[]{"nonegative", "nv"}, "Doesn't tp you under Y 0", true);

    public final Value<Boolean> rotate = new Value<>("Rotate", new String[]{"rot", "r"}, "Rotate the players head to place the block", true);
    public final Value<Boolean> center = new Value<>("Center", new String[]{"centered", "c", "cen"}, "Centers the player on their current block when beginning to place", false);
    public final Value<Boolean> offGround = new Value<>("OffGround", new String[]{"offg", "og", "o"}, "Forces player onGround to false when enabled", true);
    public final Value<Boolean> sneaking = new Value<>("Sneaking", new String[]{"sneak", "s", "fs", "sneaking"}, "Forces player to sneak when enabled", false);

    private final Timer timer = new Timer();
    private final RotationTask rotationTask = new RotationTask("BurrowTask", 9); // 9 == high priority
    private BlockPos burrowPos;

    public BurrowModule() {
        super("Burrow", new String[]{"burow", "burro", "brrw"}, "Places obsidian inside yourself", "NONE", -1, ModuleType.COMBAT);
    }

    @Override
    public void onEnable() {
        super.onEnable();

        final Minecraft mc = Minecraft.getMinecraft();
        burrowPos = new BlockPos(mc.player.posX, Math.ceil(mc.player.posY), mc.player.posZ);
        // ceil so you can burrow while standing on an echest

        if (doChecks()) {
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

            if (this.mode.getValue() == Mode.INSTA) { // these 4 lines aren't mine, they're from https://github.com/ciruu1/InstantBurrow
                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.41999998688698D, mc.player.posZ, true));
                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.7531999805211997D, mc.player.posZ, true));
                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.00133597911214D, mc.player.posZ, true));
                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.16610926093821D, mc.player.posZ, true));
            } else {
                mc.player.jump(); // jump
                this.timer.reset(); // start timer
            }
        } else {
            this.toggle();
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
                if (this.timer.passed(this.delay.getValue()) || this.mode.getValue() == Mode.INSTA) {
                    // get our hand swap context and ensure we have obsidian
                    final HandSwapContext handSwapContext = new HandSwapContext(
                            mc.player.inventory.currentItem, InventoryUtil.findObsidianInHotbar(mc.player));

                    Seppuku.INSTANCE.getRotationManager().startTask(this.rotationTask);
                    if (this.rotationTask.isOnline()) {
                        // swap to obby
                        handSwapContext.handleHandSwap(false, mc);

                        if (this.place(burrowPos, mc)) { // we've attempted to place the block
                            if (this.offGround.getValue()) {
                                mc.player.onGround = false; // set onground to false
                            }

                            switch (this.mode.getValue()) {
                                case JUMP:
                                    mc.player.jump(); // attempt another jump to flag ncp
                                    break;
                                case GLITCH:
                                    mc.player.motionY = this.glitchY.getValue();
                                    break;
                                default: // tp and insta mode are the same
                                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, (this.smartTp.getValue() ? adaptiveTpHeight() : mc.player.posY + this.tpHeight.getValue()), mc.player.posZ, mc.player.onGround));
                                    break;
                            }
                        }

                        // swap back to original
                        handSwapContext.handleHandSwap(true, mc);

                        this.toggle(); // toggle off the module
                    }
                }
                break;
            case POST:
                break;
        }
    }

    private int adaptiveTpHeight() {
        final Minecraft mc = Minecraft.getMinecraft();

        int airblock = (-1 * Math.abs(tpHeight.getValue()));
        if (noVoid.getValue() && airblock + mc.player.posY < 1) {
            airblock = 3;
        } // set lowest tp height

        while (airblock < Math.abs(tpHeight.getValue())) {
            if (Math.abs(airblock) < 3 || !mc.world.isAirBlock(burrowPos.offset(EnumFacing.UP, airblock)) || !mc.world.isAirBlock(burrowPos.offset(EnumFacing.UP, airblock + 1))) {
                airblock++;
            } else {
                return burrowPos.getY() + airblock;
            }
        }

        return 69420; // if there isn't any room
    }

    private boolean doChecks() {
        final Minecraft mc = Minecraft.getMinecraft();

        if (mc.player != null) {
            if (InventoryUtil.getBlockCount(Blocks.OBSIDIAN) == 0) { // check for obsidian
                Seppuku.INSTANCE.getNotificationManager().addNotification("", "You don't have any obsidian to use " + this.getDisplayName(), Notification.Type.WARNING, 3000);
                return false;
            } else if (InventoryUtil.findObsidianInHotbar(mc.player) == -1) {
                Seppuku.INSTANCE.getNotificationManager().addNotification("", "No obsidian in hotbar", Notification.Type.WARNING, 3000);
                return false;
            }

            if (mode.getValue() == Mode.INSTA || mode.getValue() == Mode.TP) {
                if (adaptiveTpHeight() == 69420) { // check if there is room to tp
                    Seppuku.INSTANCE.getNotificationManager().addNotification("", "Not enough space to use " + this.getDisplayName(), Notification.Type.WARNING, 3000);
                    return false;
                }
            }

            if (mc.world.getBlockState(burrowPos).getBlock().equals(Blocks.OBSIDIAN)) { // is the player already burrowed
                return false;
            }

            if (!mc.world.isAirBlock(burrowPos.offset(EnumFacing.UP, 2))) { // is the player trapped
                Seppuku.INSTANCE.getNotificationManager().addNotification("", "Not enough space to use " + this.getDisplayName(), Notification.Type.WARNING, 3000);
                return false;
            }

            for (final Entity entity : mc.world.loadedEntityList) {
                if (!(entity instanceof EntityItem) && !entity.equals(mc.player)) {
                    if (new AxisAlignedBB(burrowPos).intersects(entity.getEntityBoundingBox())) { // is there another player in the hole
                        Seppuku.INSTANCE.getNotificationManager().addNotification("", "Not enough space to use " + this.getDisplayName(), Notification.Type.WARNING, 3000);
                        return false;
                    }
                }
            }

            return true;
        }

        return false;
    }

    private boolean place(final BlockPos pos, final Minecraft mc) {
        final Block block = mc.world.getBlockState(pos).getBlock();

        final EnumFacing direction = MathUtil.calcSide(pos);
        if (direction == null)
            return false;

        final boolean activated = block.onBlockActivated(mc.world, pos, mc.world.getBlockState(pos), mc.player, EnumHand.MAIN_HAND, direction, 0, 0, 0);

        if (activated || sneaking.getValue())
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));

        final EnumFacing otherSide = direction.getOpposite();
        final BlockPos sideOffset = pos.offset(direction);

        if (rotate.getValue()) {
            final float[] angle = MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), new Vec3d(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f));
            Seppuku.INSTANCE.getRotationManager().setPlayerRotations(angle[0], angle[1]);
        }

        mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(sideOffset, otherSide, EnumHand.MAIN_HAND, 0.5F, 0.5F, 0.5F));
        mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));

        if (activated || sneaking.getValue())
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));

        return true;
    }
}
