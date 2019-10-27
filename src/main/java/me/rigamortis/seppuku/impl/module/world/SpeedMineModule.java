package me.rigamortis.seppuku.impl.module.world;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.player.*;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.BooleanValue;
import me.rigamortis.seppuku.api.value.OptionalValue;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 4/24/2019 @ 12:16 PM.
 */
public final class SpeedMineModule extends Module {

    public final OptionalValue mode = new OptionalValue("Mode", new String[]{"Mode", "M"}, 0, new String[]{"Packet", "Damage", "Instant"});

    public final BooleanValue reset = new BooleanValue("Reset", new String[]{"Res"}, true);
    public final BooleanValue doubleBreak = new BooleanValue("DoubleBreak", new String[]{"DoubleBreak", "Double", "DB"}, false);

    public SpeedMineModule() {
        super("SpeedMine", new String[]{"FastMine"}, "Allows you to break blocks faster", "NONE", -1, ModuleType.WORLD);
    }

    @Override
    public String getMetaData() {
        return this.mode.getSelectedOption();
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            Minecraft.getMinecraft().playerController.blockHitDelay = 0;

            if (this.reset.getBoolean() && Minecraft.getMinecraft().gameSettings.keyBindUseItem.isKeyDown()) {
                Minecraft.getMinecraft().playerController.isHittingBlock = false;
            }
        }
    }

    @Listener
    public void resetBlockDamage(EventResetBlockRemoving event) {
        if (this.reset.getBoolean()) {
            event.setCanceled(true);
        }
    }

    @Listener
    public void clickBlock(EventClickBlock event) {
        if (this.reset.getBoolean()) {
            if (Minecraft.getMinecraft().playerController.curBlockDamageMP > 0.1f) {
                Minecraft.getMinecraft().playerController.isHittingBlock = true;
            }
        }
    }

    @Listener
    public void damageBlock(EventPlayerDamageBlock event) {
        if (canBreak(event.getPos())) {

            final Minecraft mc = Minecraft.getMinecraft();

            if (this.reset.getBoolean()) {
                mc.playerController.isHittingBlock = false;
            }

            switch (this.mode.getInt()) {
                case 0:
                    mc.player.swingArm(EnumHand.MAIN_HAND);
                    mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, event.getPos(), event.getFace()));
                    mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, event.getPos(), event.getFace()));
                    event.setCanceled(true);
                    break;
                case 1:
                    if (mc.playerController.curBlockDamageMP >= 0.7f) {
                        mc.playerController.curBlockDamageMP = 1.0f;
                    }
                    break;
                case 2:
                    mc.player.swingArm(EnumHand.MAIN_HAND);
                    mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, event.getPos(), event.getFace()));
                    mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, event.getPos(), event.getFace()));
                    mc.playerController.onPlayerDestroyBlock(event.getPos());
                    mc.world.setBlockToAir(event.getPos());
                    break;
            }
        }

        if (this.doubleBreak.getBoolean()) {
            final BlockPos above = event.getPos().add(0, 1, 0);

            final Minecraft mc = Minecraft.getMinecraft();

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
        final IBlockState blockState = Minecraft.getMinecraft().world.getBlockState(pos);
        final Block block = blockState.getBlock();

        return block.getBlockHardness(blockState, Minecraft.getMinecraft().world, pos) != -1;
    }

}
