package me.rigamortis.seppuku.impl.module.combat;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.player.EventDestroyBlock;
import me.rigamortis.seppuku.api.event.player.EventUpdateWalkingPlayer;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.task.hand.HandSwapContext;
import me.rigamortis.seppuku.api.task.rotation.RotationTask;
import me.rigamortis.seppuku.api.util.EntityUtil;
import me.rigamortis.seppuku.api.util.InventoryUtil;
import me.rigamortis.seppuku.api.util.MathUtil;
import me.rigamortis.seppuku.api.util.Timer;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * @author noil
 */
public final class ChestFarmerModule extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();

    public final Value<Boolean> pickOnlyMode = new Value<Boolean>("PickaxeOnly", new String[]{"pickonly", "onlypick", "onlypicks", "onlypickaxes", "pickaxesonly", "po"}, "Only run this module while a pickaxe is being held", true);
    public final Value<Boolean> visible = new Value<Boolean>("Visible", new String[]{"Visible", "v"}, "Casts a ray to the placement position, forces the placement when disabled", true);
    public final Value<Boolean> rotate = new Value<Boolean>("Rotate", new String[]{"rotation", "r", "rotate"}, "Rotate to place the chest", true);
    public final Value<Boolean> swing = new Value<Boolean>("Swing", new String[]{"Arm"}, "Swing the player's arm while placing the chest", true);
    public final Value<Boolean> sneak = new Value<Boolean>("PlaceOnSneak", new String[]{"sneak", "s", "pos", "sneakPlace"}, "When true, ChestFarmer will only place while the player is sneaking", false);
    public final Value<Boolean> moving = new Value<Boolean>("FarmWhileMoving", new String[]{"moving", "fwm"}, "When true, ChestFarmer will farm while walking and jumping", false);
    public final Value<Integer> range = new Value<Integer>("Range", new String[]{"MaxRange", "MaximumRange"}, "Range around you to mine ender chests", 3, 1, 6, 1);
    public final Value<Float> placeDelay = new Value<Float>("Delay", new String[]{"PlaceDelay", "PlaceDel"}, "The delay(ms) between chests being placed", 100.0f, 0.0f, 500.0f, 1.0f);
    public final Value<Integer> safeLimit = new Value<Integer>("SafeLimit", new String[]{"limit", "sl", "l"}, "The amount of ender chests we will keep and not farm", 16, 0, 128, 1);

    private final Timer placeTimer = new Timer();
    private final RotationTask placeRotationTask = new RotationTask("ChestFarmerPlaceTask", 3);
    private final RotationTask mineRotationTask = new RotationTask("ChestFarmerMineTask", 3);

    private BlockPos currentWorkingPos = null;
    private boolean breaking = false;

    public ChestFarmerModule() {
        //String displayName, String[] alias, String desc, String key, int color, ModuleType type
        super("ChestFarmer", new String[]{"chestfarmer", "autoenderchest", "autochest", "cf"}, "Automatically place and mine ender chests for obsidian", "NONE", -1, ModuleType.COMBAT);
    }

    @Override
    public void onToggle() {
        super.onToggle();
        this.currentWorkingPos = null;
        Seppuku.INSTANCE.getRotationManager().finishTask(this.placeRotationTask);
        Seppuku.INSTANCE.getRotationManager().finishTask(this.mineRotationTask);
    }

    @Listener
    public void onWalkingUpdate(EventUpdateWalkingPlayer event) {
        if (mc.world == null || mc.player == null)
            return;

        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (InventoryUtil.getBlockCount(Blocks.ENDER_CHEST) <= this.safeLimit.getValue()) {
                Seppuku.INSTANCE.getNotificationManager().addNotification("", this.getDisplayName() + ": Safe limit reached, toggling off.");
                this.toggle();
                return;
            }

            if (this.pickOnlyMode.getValue()) {
                if (!(mc.player.getHeldItemMainhand().getItem() instanceof ItemPickaxe)) {
                    this.currentWorkingPos = null;
                    if (this.placeRotationTask.isOnline()) {
                        Seppuku.INSTANCE.getRotationManager().finishTask(this.placeRotationTask);
                    }
                    if (this.mineRotationTask.isOnline()) {
                        Seppuku.INSTANCE.getRotationManager().finishTask(this.mineRotationTask);
                    }
                    return;
                }
            }

            final Vec3d pos = MathUtil.interpolateEntity(mc.player, mc.getRenderPartialTicks());
            final float playerSpeed = (float) MathUtil.getDistance(pos, mc.player.posX, mc.player.posY, mc.player.posZ);

            if (!this.moving.getValue()) {
                if (!mc.player.onGround || playerSpeed > 0.005f) {
                    return;
                }
            }

            final BlockPos interpolatedPos = new BlockPos(pos.x, pos.y, pos.z);
            final BlockPos north = interpolatedPos.north();
            final BlockPos south = interpolatedPos.south();
            final BlockPos east = interpolatedPos.east();
            final BlockPos west = interpolatedPos.west();

            final BlockPos[] possibleBlocks = new BlockPos[]{north.down(), south.down(), east.down(), west.down(),
                    north, south, east, west};

            if (this.currentWorkingPos == null) {
                if (this.mineRotationTask.isOnline()) {
                    Seppuku.INSTANCE.getRotationManager().finishTask(this.mineRotationTask);
                }

                // find a chest location (starting from under the player first and going upwards)
                for (final BlockPos blockPos : possibleBlocks) {
                    if (!this.valid(blockPos))
                        continue;

                    this.currentWorkingPos = blockPos;
                }
            } else { // we have blocks to place
                if (!mc.player.isHandActive()) {
                    final HandSwapContext handSwapContext = new HandSwapContext(
                            mc.player.inventory.currentItem, InventoryUtil.findEnderChestInHotbar(mc.player));

                    if (handSwapContext.getNewSlot() == -1) {
                        Seppuku.INSTANCE.getRotationManager().finishTask(this.placeRotationTask);
                        return;
                    }

                    if (!mc.player.isSneaking() && this.sneak.getValue()) {
                        if (this.placeRotationTask.isOnline()) {
                            Seppuku.INSTANCE.getRotationManager().finishTask(this.placeRotationTask);
                        }
                        return;
                    }

                    if (this.valid(this.currentWorkingPos) && !this.mineRotationTask.isOnline()) {
                        Seppuku.INSTANCE.getRotationManager().startTask(this.placeRotationTask);
                        if (this.placeRotationTask.isOnline()) {
                            // swap to obsidian
                            handSwapContext.handleHandSwap(false, mc);

                            if (this.placeDelay.getValue() <= 0.0f) {
                                this.place(this.currentWorkingPos);
                            } else if (placeTimer.passed(this.placeDelay.getValue())) {
                                this.place(this.currentWorkingPos);
                                this.placeTimer.reset();
                            }

                            // swap back to original
                            handSwapContext.handleHandSwap(true, mc);

                            Seppuku.INSTANCE.getRotationManager().finishTask(this.placeRotationTask);
                        }
                    }
                }
            }
        }

        if (event.getStage() == EventStageable.EventStage.POST) {
            if (this.currentWorkingPos != null) {
                for (TileEntity tileEntity : mc.world.loadedTileEntityList) {
                    if (tileEntity instanceof TileEntityEnderChest) {
                        if (this.currentWorkingPos.getX() == tileEntity.getPos().getX() &&
                                this.currentWorkingPos.getY() == tileEntity.getPos().getY() &&
                                this.currentWorkingPos.getZ() == tileEntity.getPos().getZ() &&
                                mc.player.getDistance(tileEntity.getPos().getX(), tileEntity.getPos().getY(), tileEntity.getPos().getZ()) < this.range.getValue()) {
                            Seppuku.INSTANCE.getRotationManager().startTask(this.mineRotationTask);
                            if (this.mineRotationTask.isOnline()) {
                                if (this.rotate.getValue()) {
                                    final float[] rotations = EntityUtil.getRotations(tileEntity.getPos().getX(), tileEntity.getPos().getY(), tileEntity.getPos().getZ());
                                    Seppuku.INSTANCE.getRotationManager().setPlayerRotations(rotations[0], rotations[1]);
                                }
                                mc.playerController.onPlayerDamageBlock(tileEntity.getPos(), mc.player.getHorizontalFacing());
                                if (this.swing.getValue()) {
                                    mc.player.swingArm(EnumHand.MAIN_HAND);
                                }
                            }
                        }
                    }
                }
            }

            if (this.mineRotationTask.isOnline()) {
                if (mc.world.loadedTileEntityList.stream().noneMatch(tileEntity -> tileEntity instanceof TileEntityEnderChest)) {
                    this.currentWorkingPos = null;
                    Seppuku.INSTANCE.getRotationManager().finishTask(this.mineRotationTask);
                }

                mc.world.loadedTileEntityList.stream().filter(tileEntity -> tileEntity instanceof TileEntityEnderChest && mc.player.getDistance(tileEntity.getPos().getX(), tileEntity.getPos().getY(), tileEntity.getPos().getZ()) > this.range.getValue()).forEach(tileEntity -> {
                    if (this.currentWorkingPos.getX() == tileEntity.getPos().getX() &&
                            this.currentWorkingPos.getY() == tileEntity.getPos().getY() &&
                            this.currentWorkingPos.getZ() == tileEntity.getPos().getZ()) {
                        this.currentWorkingPos = null;
                        Seppuku.INSTANCE.getRotationManager().finishTask(this.mineRotationTask);
                    }
                });
            }
        }
    }

    @Listener
    public void onDestroyBlock(EventDestroyBlock event) {
        if (event.getPos() != null) {
            if (event.getPos().getX() == this.currentWorkingPos.getX() &&
                    event.getPos().getY() == this.currentWorkingPos.getY() &&
                    event.getPos().getZ() == this.currentWorkingPos.getZ()) {
                this.currentWorkingPos = null;
                Seppuku.INSTANCE.getRotationManager().finishTask(this.mineRotationTask);
            }
        }
    }

    private boolean canBreak(BlockPos pos) {
        final IBlockState blockState = Minecraft.getMinecraft().world.getBlockState(pos);
        final Block block = blockState.getBlock();

        return block.getBlockHardness(blockState, Minecraft.getMinecraft().world, pos) != -1;
    }

    private boolean valid(BlockPos pos) {
        // there are no entities colliding with block placement
        if (!mc.world.checkNoEntityCollision(new AxisAlignedBB(pos)))
            return false;

        // player is too far from distance
        if (mc.player.getDistance(pos.getX(), pos.getY(), pos.getZ()) > this.range.getValue())
            return false;

        // check if the block is replaceable
        final Block block = mc.world.getBlockState(pos).getBlock();
        return block.isReplaceable(mc.world, pos) && !(block == Blocks.OBSIDIAN) && !(block == Blocks.BEDROCK) && !(block == Blocks.ENDER_CHEST);
    }

    private void place(BlockPos pos) {
        final Block block = mc.world.getBlockState(pos).getBlock();

        final EnumFacing direction = MathUtil.calcSide(pos);
        if (direction == null)
            return;

        final boolean activated = block.onBlockActivated(mc.world, pos, mc.world.getBlockState(pos), mc.player, EnumHand.MAIN_HAND, direction, 0, 0, 0);

        if (activated)
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));

        final EnumFacing otherSide = direction.getOpposite();
        final BlockPos sideOffset = pos.offset(direction);

        if (this.rotate.getValue()) {
            final float[] angle = MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), new Vec3d(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f));
            Seppuku.INSTANCE.getRotationManager().setPlayerRotations(angle[0], angle[1]);
        }

        if (!this.visible.getValue()) {
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(sideOffset, otherSide, EnumHand.MAIN_HAND, 0.5F, 0.5F, 0.5F));
            mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
        } else {
            mc.playerController.processRightClickBlock(mc.player, mc.world, sideOffset, otherSide, new Vec3d(0.5F, 0.5F, 0.5F), EnumHand.MAIN_HAND);

            if (this.swing.getValue()) {
                mc.player.swingArm(EnumHand.MAIN_HAND);
            }
        }

        if (activated)
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
    }
}
