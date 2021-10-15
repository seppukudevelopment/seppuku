package me.rigamortis.seppuku.impl.module.world;

import java.awt.*;
import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.player.EventClickBlock;
import me.rigamortis.seppuku.api.event.player.EventPlayerDamageBlock;
import me.rigamortis.seppuku.api.event.player.EventPlayerUpdate;
import me.rigamortis.seppuku.api.event.player.EventResetBlockRemoving;
import me.rigamortis.seppuku.api.event.render.EventRender3D;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.ColorUtil;
import me.rigamortis.seppuku.api.util.RenderUtil;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 4/24/2019 @ 12:16 PM.
 */
public final class SpeedMineModule extends Module {

    public final Value<Mode> mode = new Value<Mode>("Mode", new String[]{"Mode", "M"}, "The speed-mine mode to use", Mode.DAMAGE);

    private enum Mode {
        PACKET, DAMAGE, INSTANT, SEQUENTIAL
    }

    public static BlockPos autoPos;

    public BlockPos seqPos;
    public EnumFacing seqDir;
    public final Minecraft mc = Minecraft.getMinecraft();

    public final Value<Boolean> reset = new Value<Boolean>("Reset", new String[]{"Res"}, "Stops current block destroy damage from resetting if enabled", true);
    public final Value<Boolean> doubleBreak = new Value<Boolean>("DoubleBreak", new String[]{"DoubleBreak", "Double", "DB"}, "Mining a block will also mine the block above it, if enabled.", false);
    public final Value<Boolean> auto = new Value<Boolean>("Auto", new String[]{}, "When enabled, allows for multi-mining blocks.", false);

    public SpeedMineModule() {
        super("SpeedMine", new String[]{"FastMine"}, "Allows you to break blocks faster", "NONE", -1, ModuleType.WORLD);
    }

    @Override
    public String getMetaData() {
        return this.mode.getValue().name();
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {

        if (event.getStage() == EventStageable.EventStage.PRE) {
            mc.playerController.blockHitDelay = 0;

            if (seqPos != null && mc.world.getBlockState(seqPos).getBlock() != Blocks.AIR && mode.getValue() == Mode.SEQUENTIAL) {
                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, seqPos, seqDir));
            }
            if (this.reset.getValue() && mc.gameSettings.keyBindUseItem.isKeyDown()) {
                mc.playerController.isHittingBlock = false;
            }
        }
    }

    @Listener
    public void resetBlockDamage(EventResetBlockRemoving event) {
        if (this.reset.getValue()) {
            event.setCanceled(true);
        }
    }

    @Listener
    public void clickBlock(EventClickBlock event) {
        if (this.reset.getValue()) {
            if (mc.playerController.curBlockDamageMP > 0.1f) {
                mc.playerController.isHittingBlock = true;
            }
        }
    }

    @Listener
    public void onRender(EventRender3D event) {
        if (!auto.getValue()) return;
        if (autoPos != null && mc.world.getBlockState(autoPos).getBlock() != Blocks.AIR) {
            RenderUtil.begin3D();
            final AxisAlignedBB bb = new AxisAlignedBB(
                autoPos.getX() - mc.getRenderManager().viewerPosX,
                autoPos.getY() - mc.getRenderManager().viewerPosY,
                autoPos.getZ() - mc.getRenderManager().viewerPosZ,
                autoPos.getX() + 1 - mc.getRenderManager().viewerPosX,
                autoPos.getY() + 1 - mc.getRenderManager().viewerPosY,
                autoPos.getZ() + 1 - mc.getRenderManager().viewerPosZ
            );
            RenderUtil.drawBoundingBox(bb, 2f, new Color(255,255,255).getRGB());
            RenderUtil.end3D();
        }
    }

    
    @Listener
    public void damageBlock(EventPlayerDamageBlock event) {
        if (canBreak(event.getPos())) {

            if (this.reset.getValue()) {
                mc.playerController.isHittingBlock = false;
            }

            switch (this.mode.getValue()) {
                case PACKET:
                    mc.player.swingArm(EnumHand.MAIN_HAND);
                    mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, event.getPos(), event.getFace()));
                    mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, event.getPos(), event.getFace()));
                    event.setCanceled(true);
                    break;
                case DAMAGE:
                    if (mc.playerController.curBlockDamageMP >= 0.7f) {
                        mc.playerController.curBlockDamageMP = 1.0f;
                        if (auto.getValue()) {
                            mode.setValue(Mode.INSTANT);
                        }
                    }
                    break;
                case INSTANT:
                    mc.player.swingArm(EnumHand.MAIN_HAND);
                    mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, event.getPos(), event.getFace()));
                    mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, event.getPos(), event.getFace()));
                    mc.playerController.onPlayerDestroyBlock(event.getPos());
                    mc.world.setBlockToAir(event.getPos());
                    if (auto.getValue()) {
                        if (autoPos == null) {
                            autoPos = event.getPos();
                        }
                        else if (mc.world.getBlockState(autoPos).getBlock() == Blocks.AIR) {
                            autoPos = event.getPos();
                        }

                        mode.setValue(Mode.DAMAGE);
                    }
                    break;
                case SEQUENTIAL:
                    mc.player.swingArm(EnumHand.MAIN_HAND);
                    mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, event.getPos(), event.getFace()));
                    mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, event.getPos(), event.getFace()));
                    seqPos = event.getPos();
                    seqDir = event.getFace();
                    event.setCanceled(true);
                    break;
            }
        }

        if (this.doubleBreak.getValue()) {
            final BlockPos above = event.getPos().add(0, 1, 0);
            if (canBreak(above) && mc.player.getDistance(above.getX(), above.getY(), above.getZ()) <= 5f) {
                mc.player.swingArm(EnumHand.MAIN_HAND);
                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, above, event.getFace()));
                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, above, event.getFace()));
                mc.playerController.onPlayerDestroyBlock(above);
                mc.world.setBlockToAir(above);
            }
        }
    }


    

    private boolean canBreak(BlockPos pos) {
        final IBlockState blockState = mc.world.getBlockState(pos);
        final Block block = blockState.getBlock();

        return block.getBlockHardness(blockState, mc.world, pos) != -1;
    }

}
