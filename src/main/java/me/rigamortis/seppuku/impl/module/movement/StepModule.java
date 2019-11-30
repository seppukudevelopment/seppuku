package me.rigamortis.seppuku.impl.module.movement;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.player.EventUpdateWalkingPlayer;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author: noil
 * Date: 7/14/2019
 * Time: 2:30 PM
 */
public final class StepModule extends Module {

    public final Value<Mode> mode = new Value<Mode>("Mode", new String[]{"Mode", "M"}, "The step block-height mode to use.", Mode.ONE);

    private enum Mode {
        ONE, TWO
    }

    private final double[] oneblockPositions = {0.42D, 0.75D};

    private final double[] twoblockPositions = {0.4D, 0.75D, 0.5D, 0.41D, 0.83D, 1.16D, 1.41D, 1.57D, 1.58D, 1.42D};

    private double[] selectedPositions = new double[0];

    private int packets;

    public StepModule() {
        super("Step", new String[]{"stp"}, "Allows the player to step/teleport up blocks when horizontally colliding with one", "NONE", -1, ModuleType.MOVEMENT);
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Listener
    public void onWalkingUpdate(EventUpdateWalkingPlayer event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            final Minecraft mc = Minecraft.getMinecraft();

            switch (this.mode.getValue()) {
                case ONE:
                    this.selectedPositions = this.oneblockPositions;
                    break;
                case TWO:
                    this.selectedPositions = this.twoblockPositions;
                    break;
            }

            if (mc.player.collidedHorizontally && mc.player.onGround) {
                this.packets++;
            }

            //check if there is a block above our head
            final AxisAlignedBB bb = mc.player.getEntityBoundingBox();

            for (int x = MathHelper.floor(bb.minX); x < MathHelper.floor(bb.maxX + 1.0D); x++) {
                for (int z = MathHelper.floor(bb.minZ); z < MathHelper.floor(bb.maxZ + 1.0D); z++) {
                    final Block block = mc.world.getBlockState(new BlockPos(x, bb.maxY + 1, z)).getBlock();
                    if (!(block instanceof BlockAir)) {
                        return;
                    }
                }
            }

            if (mc.player.onGround && !mc.player.isInsideOfMaterial(Material.WATER) && !mc.player.isInsideOfMaterial(Material.LAVA) && !mc.player.isInWeb && mc.player.collidedVertically && mc.player.fallDistance == 0 && !mc.gameSettings.keyBindJump.pressed && mc.player.collidedHorizontally && !mc.player.isOnLadder() && this.packets > this.selectedPositions.length - 2) {
                for (double position : this.selectedPositions) {
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + position, mc.player.posZ, true));
                }
                mc.player.setPosition(mc.player.posX, mc.player.posY + this.selectedPositions[this.selectedPositions.length - 1], mc.player.posZ);
                this.packets = 0;
            }
        }
    }

}

