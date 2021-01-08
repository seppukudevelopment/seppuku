package me.rigamortis.seppuku.impl.module.world;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.player.EventUpdateWalkingPlayer;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.task.rotation.RotationTask;
import me.rigamortis.seppuku.api.util.EntityUtil;
import me.rigamortis.seppuku.api.util.MathUtil;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemSeedFood;
import net.minecraft.item.ItemSeeds;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * @author noil
 */
public final class AutoFarmModule extends Module {

    public final Value<Mode> mode = new Value<Mode>("Mode", new String[]{"Mode", "m"}, "The current farming mode.", Mode.HARVEST);
    public final Value<Float> range = new Value<Float>("Range", new String[]{"Range", "Reach", "r"}, "The range in blocks your player should reach to farm.", 4.0f, 1.0f, 9.0f, 0.1f);

    private BlockPos currentBlockPos;

    private final RotationTask rotationTask = new RotationTask("AutoFarmTask", 3);

    public AutoFarmModule() {
        super("AutoFarm", new String[]{"AutoFarm", "Farm", "AutoHoe", "AutoBoneMeal", "AutoPlant"}, "Good ol' farming, just change the \"Mode\" value.", "NONE", -1, ModuleType.WORLD);
    }

    @Listener
    public void onMotionUpdate(EventUpdateWalkingPlayer event) {
        final Minecraft mc = Minecraft.getMinecraft();

        switch (event.getStage()) {
            case PRE:
                double r = this.range.getValue();
                for (double y = mc.player.posY + r; y > mc.player.posY - r; y -= 1.0D) {
                    for (double x = mc.player.posX - r; x < mc.player.posX + r; x += 1.0D) {
                        for (double z = mc.player.posZ - r; z < mc.player.posZ + r; z += 1.0D) {
                            BlockPos blockPos = new BlockPos(x, y, z);
                            if (this.isBlockValid(blockPos, mc)) {
                                if (this.currentBlockPos == null) {
                                    this.currentBlockPos = blockPos;

                                    Seppuku.INSTANCE.getRotationManager().startTask(this.rotationTask);
                                    if (this.rotationTask.isOnline()) {
                                        final float[] angle = MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), new Vec3d(blockPos.getX() + 0.5f, blockPos.getY() + 0.5f, blockPos.getZ() + 0.5f));
                                        Seppuku.INSTANCE.getRotationManager().setPlayerRotations(angle[0], angle[1]);
                                    }
                                }
                            }
                        }
                    }
                }
                break;
            case POST:
                if (this.currentBlockPos != null) {
                    if (this.rotationTask.isOnline()) {
                        switch (mode.getValue()) {
                            case HARVEST:
                                if (mc.playerController.onPlayerDamageBlock(currentBlockPos, EntityUtil.getFacingDirectionToPosition(currentBlockPos))) {
                                    mc.player.swingArm(EnumHand.MAIN_HAND);
                                }
                                break;
                            case PLANT:
                            case HOE:
                            case BONEMEAL:
                                mc.playerController.processRightClickBlock(mc.player, mc.world, currentBlockPos, EntityUtil.getFacingDirectionToPosition(currentBlockPos), new Vec3d(currentBlockPos.getX() / 2F, currentBlockPos.getY() / 2F, currentBlockPos.getZ() / 2F), EnumHand.MAIN_HAND);
                                break;
                        }
                    }
                    this.currentBlockPos = null;
                } else {
                    if (this.rotationTask.isOnline()) {
                        Seppuku.INSTANCE.getRotationManager().finishTask(this.rotationTask);
                    }
                }
                break;
        }

        //if ((this.currentBlock == null) && ((mc.world.getBlockState(new BlockPos(pos)).getBlock() == Blocks.TALLGRASS) || (mc.world.getBlockState(new BlockPos(pos)).getBlock() == Blocks.DOUBLE_PLANT) || (mc.world.getBlockState(new BlockPos(pos)).getBlock() == Blocks.RED_FLOWER) || (mc.world.getBlockState(new BlockPos(pos)).getBlock() == Blocks.YELLOW_FLOWER) || (mc.world.getBlockState(new BlockPos(pos)).getBlock() instanceof BlockCrops)) && (mc.player.getDistance(pos.x, pos.y, pos.z) <= this.range)) {
    }

    private boolean isBlockValid(BlockPos position, final Minecraft mc) {
        boolean temp = false;
        Block block = mc.world.getBlockState(position).getBlock();
        switch (mode.getValue()) {
            case PLANT:
                if (mc.player.getHeldItemMainhand().getItem() == Items.NETHER_WART) {
                    if (block instanceof BlockSoulSand) {
                        if (mc.world.getBlockState(position.up()).getBlock() == Blocks.AIR) {
                            temp = true;
                        }
                    }
                }
                if (mc.player.getHeldItemMainhand().getItem() == Items.REEDS) {
                    if (block instanceof BlockGrass || block instanceof BlockDirt || block instanceof BlockSand) {
                        if (mc.world.getBlockState(position.up()).getBlock() == Blocks.AIR) {
                            for (EnumFacing side : EnumFacing.Plane.HORIZONTAL) {
                                IBlockState blockState = mc.world.getBlockState(position.offset(side));
                                if (blockState.getMaterial() == Material.WATER || blockState.getBlock() == Blocks.FROSTED_ICE) {
                                    temp = true;
                                }
                            }
                        }
                    }
                }
                if (mc.player.getHeldItemMainhand().getItem() instanceof ItemSeeds || mc.player.getHeldItemMainhand().getItem() instanceof ItemSeedFood) {
                    if (block instanceof BlockFarmland) {
                        if (mc.world.getBlockState(position.up()).getBlock() == Blocks.AIR) {
                            temp = true;
                        }
                    }
                }
                break;
            case HARVEST:
                if ((block instanceof BlockTallGrass) || (block instanceof BlockFlower) || (block instanceof BlockDoublePlant)) {
                    temp = true;
                } else if (block instanceof BlockCrops) {
                    BlockCrops crops = (BlockCrops) block;
                    if (crops.getMetaFromState(mc.world.getBlockState(position)) == 7) { // Crops are grown
                        temp = true;
                    }
                } else if (block instanceof BlockNetherWart) {
                    BlockNetherWart netherWart = (BlockNetherWart) block;
                    if (netherWart.getMetaFromState(mc.world.getBlockState(position)) == 3) { // Nether Wart is grown
                        temp = true;
                    }
                } else if (block instanceof BlockReed) {
                    if (mc.world.getBlockState(position.down()).getBlock() instanceof BlockReed) { // Check if a reed is under it
                        temp = true;
                    }
                } else if (block instanceof BlockCactus) {
                    if (mc.world.getBlockState(position.down()).getBlock() instanceof BlockCactus) { // Check if a cactus is under it
                        temp = true;
                    }
                } else if (block instanceof BlockPumpkin) {
                    temp = true;
                } else if (block instanceof BlockMelon) {
                    temp = true;
                } else if (block instanceof BlockChorusFlower) {
                    BlockChorusFlower chorusFlower = (BlockChorusFlower) block;
                    if (chorusFlower.getMetaFromState(mc.world.getBlockState(position)) == 5) { // Chorus is grown
                        temp = true;
                    }
                }
                break;
            case HOE:
                if (block == Blocks.DIRT || block == Blocks.GRASS) {
                    if (mc.world.getBlockState(position.up()).getBlock() == Blocks.AIR) {
                        temp = true;
                    }
                }
                break;
            case BONEMEAL:
                if (mc.player.getHeldItemMainhand().getItem() instanceof ItemDye) {
                    EnumDyeColor enumdyecolor = EnumDyeColor.byDyeDamage(mc.player.getHeldItemMainhand().getMetadata());
                    if (enumdyecolor == EnumDyeColor.WHITE) {
                        if (block instanceof BlockCrops) {
                            BlockCrops crops = (BlockCrops) block;
                            if (crops.getMetaFromState(mc.world.getBlockState(position)) < 7) { // Crops are not grown
                                temp = true;
                            }
                        }
                    }
                }
                break;

        }

        return temp && mc.player.getDistance(position.getX(), position.getY(), position.getZ()) <= this.range.getValue();
    }

    private enum Mode {
        PLANT,
        HARVEST,
        HOE,
        BONEMEAL
    }
}
