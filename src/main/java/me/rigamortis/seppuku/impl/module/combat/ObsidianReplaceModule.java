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
import java.util.function.BiFunction;

/**
 * @author Daniel E
 */
public final class ObsidianReplaceModule extends Module {
    private static final int[][] BLOCK_DIRECTION_OFFSET = {
            {1, 0, 0},
            {-1, 0, 0},

            {0, 1, 0},
            {0, -1, 0},

            {0, 0, 1},
            {0, 0, -1},
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
        final HandSwapContext handSwapContext = new HandSwapContext(
                player.inventory.currentItem, findObsidianInHotbar(player));
        if (handSwapContext.getNewSlot() == -1)
            return;

        handleHandSwap(handSwapContext, false, minecraft);

        final PlacementRequest placementRequest = placementRequests.poll();
        final double playerToBlockDistance = calculateVecDistance(
                player.getPositionEyes(1.0f), placementRequest.getStructurePosition());
        if (playerToBlockDistance <= getReachDistance(minecraft))
            handlePlaceRequest(minecraft, placementRequest);

        handleHandSwap(handSwapContext, true, minecraft);
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
                final BlockPos blockPosition = blockChange.getBlockPosition();
                final double playerToBlockDistance = calculateVecDistance(
                        minecraft.player.getPositionEyes(1.0f), blockPosition);
                if (playerToBlockDistance <= getReachDistance(minecraft))
                    buildPlacementRequest(minecraft, blockPosition);
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
    public void onDisable() {
        super.onDisable();
        placementRequests.clear();
    }

    private void buildPlacementRequest(final Minecraft minecraft, final BlockPos position) {
        for (final int[] directionOffset : BLOCK_DIRECTION_OFFSET) {
            final BlockPos relativePosition = position.add(directionOffset[0], directionOffset[1],
                    directionOffset[2]);
            final Block structureBlock = minecraft.world.getBlockState(relativePosition).getBlock();
            if (structureBlock instanceof BlockAir || structureBlock instanceof BlockLiquid)
                continue;

            final EnumFacing blockPlacementFace = calculateFaceForPlacement(relativePosition, position);
            if (blockPlacementFace != null && placementRequests.offer(new PlacementRequest(
                    relativePosition, blockPlacementFace)))
                return;
        }
    }

    private EnumFacing calculateFaceForPlacement(final BlockPos structurePosition,
                                                 final BlockPos blockPosition) {
        final BiFunction<Integer, String, Integer> throwingClamp = (number, axis) -> {
            if (number < -1 || number > 1)
                throw new IllegalArgumentException(
                        String.format("Difference in %s is illegal, " +
                                "positions are too far apart.", axis));

            return number;
        };

        final int diffX = throwingClamp.apply(
                structurePosition.getX() - blockPosition.getX(), "x-axis");
        switch (diffX) {
            case 1:
                return EnumFacing.WEST;
            case -1:
                return EnumFacing.EAST;
            default:
                break;
        }

        final int diffY = throwingClamp.apply(
                structurePosition.getY() - blockPosition.getY(), "y-axis");
        switch (diffY) {
            case 1:
                return EnumFacing.DOWN;
            case -1:
                return EnumFacing.UP;
            default:
                break;
        }

        final int diffZ = throwingClamp.apply(
                structurePosition.getZ() - blockPosition.getZ(), "z-axis");
        switch (diffZ) {
            case 1:
                return EnumFacing.NORTH;
            case -1:
                return EnumFacing.SOUTH;
            default:
                break;
        }

        return null;
    }

    private void handlePlaceRequest(final Minecraft minecraft, final PlacementRequest placementRequest) {
        final BlockPos structurePosition = placementRequest.getStructurePosition();
        final IBlockState structureBlockState = minecraft.world.getBlockState(structurePosition);
        final boolean blockActivated = structureBlockState.getBlock().onBlockActivated(minecraft.world,
                structurePosition, structureBlockState, minecraft.player, EnumHand.MAIN_HAND,
                EnumFacing.UP, 0.0f, 0.0f, 0.0f);
        if (blockActivated)
            minecraft.player.connection.sendPacket(new CPacketEntityAction(minecraft.player,
                    CPacketEntityAction.Action.START_SNEAKING));

        if (minecraft.playerController.processRightClickBlock(minecraft.player, minecraft.world,
                structurePosition, placementRequest.getPlaceDirection(),
                Vec3d.ZERO, EnumHand.MAIN_HAND) != EnumActionResult.FAIL)
            minecraft.player.swingArm(EnumHand.MAIN_HAND);

        if (blockActivated)
            minecraft.player.connection.sendPacket(new CPacketEntityAction(minecraft.player,
                    CPacketEntityAction.Action.STOP_SNEAKING));
    }

    private boolean isItemStackObsidian(final ItemStack itemStack) {
        if (itemStack.getItem() instanceof ItemBlock)
            return ((ItemBlock) itemStack.getItem()).getBlock() instanceof BlockObsidian;

        return false;
    }

    private int findObsidianInHotbar(final EntityPlayerSP player) {
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

    private double calculateVecDistance(final Vec3d vec, final BlockPos blockPosition) {
        final double diffX = blockPosition.getX() - vec.x;
        final double diffY = blockPosition.getY() - vec.y;
        final double diffZ = blockPosition.getZ() - vec.z;
        return MathHelper.sqrt(diffX * diffX + diffY * diffY + diffZ * diffZ);
    }

    private void handleHandSwap(final HandSwapContext context, final boolean restore,
                                final Minecraft minecraft) {
        minecraft.player.inventory.currentItem =
                restore ? context.getOldSlot() : context.getNewSlot();
        minecraft.playerController.updateController();
    }

    private static final class HandSwapContext {
        private final int oldSlot;
        private final int newSlot;

        HandSwapContext(final int oldSlot, final int newSlot) {
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
        private final BlockPos structurePosition;
        private final EnumFacing placeDirection;

        PlacementRequest(final BlockPos structurePosition,
                         final EnumFacing placeDirection) {
            this.structurePosition = structurePosition;
            this.placeDirection = placeDirection;
        }

        BlockPos getStructurePosition() {
            return structurePosition;
        }

        EnumFacing getPlaceDirection() {
            return placeDirection;
        }
    }
}
