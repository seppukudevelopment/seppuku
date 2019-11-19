package me.rigamortis.seppuku.impl.module.combat;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.module.EventModulePostLoaded;
import me.rigamortis.seppuku.api.event.network.EventReceivePacket;
import me.rigamortis.seppuku.api.event.player.EventUpdateWalkingPlayer;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.impl.module.player.FreeCamModule;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author Daniel E
 */
public final class ObsidianReplaceModule extends Module {
    private static final int[][] BLOCK_DIRECTION_OFFSET = {
            {0, 1, 0},
            {0, -1, 0},

            {0, 0, -1},
            {0, 0, 1},

            {1, 0, 0},
            {-1, 0, 0}
    };

    private final Queue<PlacementRequest> placementRequests = new ConcurrentLinkedQueue<>();
    private FreeCamModule freeCamModule = null;

    public ObsidianReplaceModule() {
        super("ObsidianReplace", new String[]{
                        "ObbyRep", "ObbyReplace", "ObbRep", "ObsidianRep"
                }, "Automatically replaces broken obsidian near you",
                "NONE", -1, ModuleType.COMBAT);

        if (!Seppuku.INSTANCE.getEventManager().addEventListener(this))
            throw new RuntimeException();
    }

    @Listener
    public void onWalkingUpdate(final EventUpdateWalkingPlayer event) {
        if (placementRequests.isEmpty())
            return;

        if (event.getStage() != EventStageable.EventStage.PRE)
            return;

        if (freeCamModule != null && freeCamModule.isEnabled())
            return;

        final Minecraft minecraft = Minecraft.getMinecraft();
        final EntityPlayerSP player = minecraft.player;
        final int obisidanSlot = findObsidianInHotbar(player);
        if (obisidanSlot == -1)
            return;

        final int currentSlot = player.inventory.currentItem;
        final HandSwapContext handSwapContext = new HandSwapContext(currentSlot, obisidanSlot);
        processHandSwap(handSwapContext, false, minecraft);
        if (!isItemStackObsidian(player.inventory.getCurrentItem()))
            return;

        final PlacementRequest placementRequest = placementRequests.poll();
        final BlockPos position = placementRequest.getBlockPosition();
        final double playerToBlockDistance = calculateVecDistance(
                minecraft.player.getPositionEyes(1.0f), position.getX(),
                position.getY(), position.getZ());
        if (playerToBlockDistance <= getReachDistance(minecraft))
            handlePlaceRequest(minecraft, placementRequest);

        processHandSwap(handSwapContext, true, minecraft);
    }

    @Listener
    public void onReceivePacket(final EventReceivePacket event) {
        if (event.getStage() != EventStageable.EventStage.POST)
            return;

        if (freeCamModule != null && freeCamModule.isEnabled())
            return;

        final Minecraft minecraft = Minecraft.getMinecraft();
        if (event.getPacket() instanceof SPacketBlockChange) {
            final SPacketBlockChange blockChange = (SPacketBlockChange) event.getPacket();
            if (blockChange.getBlockState().getBlock() instanceof BlockAir) {
                final BlockPos position = blockChange.getBlockPosition();
                final double playerToBlockDistance = calculateVecDistance(
                        minecraft.player.getPositionEyes(1.0f), position.getX(),
                        position.getY(), position.getZ());
                if (playerToBlockDistance <= getReachDistance(minecraft))
                    buildPlacementRequest(minecraft, position);
            }
        }
    }

    @Listener
    public void onModulePostLoaded(final EventModulePostLoaded event) {
        if (event.getModule() instanceof FreeCamModule) {
            freeCamModule = (FreeCamModule) event.getModule();
            if (!Seppuku.INSTANCE.getEventManager().removeEventListener(this))
                throw new RuntimeException();
        }
    }

    @Override
    public void onToggle() {
        super.onToggle();
        placementRequests.clear();
    }

    private void buildPlacementRequest(final Minecraft minecraft, final BlockPos position) {
        for (final int[] directionOffset : BLOCK_DIRECTION_OFFSET) {
            final BlockPos relativePosition = position.add(directionOffset[0], directionOffset[1],
                    directionOffset[2]);
            final Block structureBlock = minecraft.world.getBlockState(relativePosition).getBlock();
            if (structureBlock instanceof BlockAir || structureBlock instanceof BlockLiquid)
                continue;

            final EnumFacing placementDirection = calculatePlacementFace(relativePosition, position);
            if (placementDirection != null && placementRequests.offer(new PlacementRequest(
                    relativePosition, position, placementDirection)))
                return;
        }
    }

    private EnumFacing calculatePlacementFace(final BlockPos structurePosition,
                                              final BlockPos blockPosition) {
        final int diffX = structurePosition.getX() - blockPosition.getX();
        if (diffX < -1 || diffX > 1)
            return null;

        switch (diffX) {
            case 1:
                return EnumFacing.EAST;
            case -1:
                return EnumFacing.WEST;
            default:
                break;
        }

        final int diffY = structurePosition.getY() - blockPosition.getY();
        if (diffY < -1 || diffY > 1)
            return null;

        switch (diffY) {
            case 1:
                return EnumFacing.UP;
            case -1:
                return EnumFacing.DOWN;
            default:
                break;
        }

        final int diffZ = structurePosition.getZ() - blockPosition.getZ();
        if (diffZ < -1 || diffZ > 1)
            return null;

        switch (diffZ) {
            case 1:
                return EnumFacing.SOUTH;
            case -1:
                return EnumFacing.NORTH;
            default:
                break;
        }

        return null;
    }

    private void handlePlaceRequest(final Minecraft minecraft, final PlacementRequest placementRequest) {
        final EntityPlayerSP player = minecraft.player;
        final IBlockState blockState = player.world.getBlockState(placementRequest.getStructureBlock());
        final boolean blockActivated = blockState.getBlock().onBlockActivated(player.world,
                placementRequest.getBlockPosition(), blockState, player, EnumHand.MAIN_HAND,
                placementRequest.getPlaceDirection(), 0.0f, 0.0f, 0.0f);
        if (blockActivated)
            player.connection.sendPacket(new CPacketEntityAction(player,
                    CPacketEntityAction.Action.START_SNEAKING));

        if (minecraft.playerController.processRightClickBlock(player, minecraft.world,
                placementRequest.getBlockPosition(), placementRequest.getPlaceDirection()
                        .getOpposite(), Vec3d.ZERO, EnumHand.MAIN_HAND) != EnumActionResult.FAIL)
            player.swingArm(EnumHand.MAIN_HAND);

        if (blockActivated)
            player.connection.sendPacket(new CPacketEntityAction(player,
                    CPacketEntityAction.Action.STOP_SNEAKING));
    }

    private boolean isItemStackObsidian(final ItemStack itemStack) {
        if (itemStack.getItem() instanceof ItemBlock)
            return ((ItemBlock) itemStack.getItem()).getBlock() instanceof BlockObsidian;

        return false;
    }

    private int findObsidianInHotbar(final EntityPlayer player) {
        for (int index = 0; InventoryPlayer.isHotbar(index); index++)
            if (isItemStackObsidian(player.inventory.getStackInSlot(index)))
                return index;

        return -1;
    }

    private double getReachDistance(final Minecraft minecraft) {
        // todo; this is just not right my guy, we should really be verifying
        //  placements on certain block faces with traces and stuff...
        return minecraft.playerController.getBlockReachDistance();
    }

    private double calculateVecDistance(final Vec3d vector, final int blockX,
                                        final int blockY, final int blockZ) {
        final double diffX = blockX - vector.x;
        final double diffY = blockY - vector.y;
        final double diffZ = blockZ - vector.z;
        return MathHelper.sqrt(diffX * diffX + diffY * diffY + diffZ * diffZ);
    }

    private void processHandSwap(final HandSwapContext context, final boolean restore, final Minecraft minecraft) {
        minecraft.player.inventory.currentItem = restore ? context.getOldSlot() : context.getNewSlot();
        minecraft.playerController.updateController();
    }

    private static final class HandSwapContext {
        private final int oldSlot;
        private final int newSlot;

        HandSwapContext(int oldSlot, int newSlot) {
            this.oldSlot = oldSlot;
            this.newSlot = newSlot;
        }

        int getOldSlot() {
            return oldSlot;
        }

        int getNewSlot() {
            return newSlot;
        }
    }

    private static final class PlacementRequest {
        private final BlockPos blockPosition;
        private final BlockPos structureBlock;
        private final EnumFacing placeDirection;

        PlacementRequest(final BlockPos blockPosition,
                         final BlockPos structureBlock,
                         final EnumFacing placeDirection) {
            this.blockPosition = blockPosition;
            this.structureBlock = structureBlock;
            this.placeDirection = placeDirection;
        }

        BlockPos getBlockPosition() {
            return blockPosition;
        }

        BlockPos getStructureBlock() {
            return structureBlock;
        }

        EnumFacing getPlaceDirection() {
            return placeDirection;
        }
    }
}
