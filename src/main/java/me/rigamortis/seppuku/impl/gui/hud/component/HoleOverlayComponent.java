package me.rigamortis.seppuku.impl.gui.hud.component;

import me.rigamortis.seppuku.api.gui.hud.component.DraggableHudComponent;
import me.rigamortis.seppuku.api.util.MathUtil;
import me.rigamortis.seppuku.api.util.RenderUtil;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * Author Seth
 * 12/2/2019 @ 2:17 PM.
 */
public final class HoleOverlayComponent extends DraggableHudComponent {

    public HoleOverlayComponent() {
        super("HoleOverlay");
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        this.setW(48);
        this.setH(48);

        final Minecraft mc = Minecraft.getMinecraft();

        float yaw = 0;
        final int dir = (MathHelper.floor((double) (mc.player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3);

        switch (dir) {
            case 1:
                yaw = 90;
                break;
            case 2:
                yaw = -180;
                break;
            case 3:
                yaw = -90;
                break;
        }

        final BlockPos northPos = this.traceToBlock(partialTicks, yaw);

        if (northPos != null) {
            final Block north = this.getBlock(northPos);

            if (north != null && north != Blocks.AIR) {
                final int damage = this.getBlockDamage(northPos);

                if (damage != 0) {
                    RenderUtil.drawRect(this.getX() + 16, this.getY(), this.getX() + 32, this.getY() + 16, 0x60ff0000);
                }

                this.drawBlock(north, this.getX() + 16, this.getY());
            }
        }

        final BlockPos southPos = this.traceToBlock(partialTicks, yaw - 180.0f);

        if (southPos != null) {
            final Block south = this.getBlock(southPos);

            if (south != null && south != Blocks.AIR) {
                final int damage = this.getBlockDamage(southPos);

                if (damage != 0) {
                    RenderUtil.drawRect(this.getX() + 16, this.getY() + 32, this.getX() + 32, this.getY() + 48, 0x60ff0000);
                }

                this.drawBlock(south, this.getX() + 16, this.getY() + 32);
            }
        }

        final BlockPos eastPos = this.traceToBlock(partialTicks, yaw + 90.0f);

        if (eastPos != null) {
            final Block east = this.getBlock(eastPos);

            if (east != null && east != Blocks.AIR) {
                final int damage = this.getBlockDamage(eastPos);

                if (damage != 0) {
                    RenderUtil.drawRect(this.getX() + 32, this.getY() + 16, this.getX() + 48, this.getY() + 32, 0x60ff0000);
                }
                this.drawBlock(east, this.getX() + 32, this.getY() + 16);
            }
        }

        final BlockPos westPos = this.traceToBlock(partialTicks, yaw - 90.0f);

        if (westPos != null) {
            final Block west = this.getBlock(westPos);

            if (west != null && west != Blocks.AIR) {
                final int damage = this.getBlockDamage(westPos);

                if (damage != 0) {
                    RenderUtil.drawRect(this.getX(), this.getY() + 16, this.getX() + 16, this.getY() + 32, 0x60ff0000);
                }
                this.drawBlock(west, this.getX(), this.getY() + 16);
            }
        }
    }

    private int getBlockDamage(BlockPos pos) {
        for (DestroyBlockProgress destBlockProgress : Minecraft.getMinecraft().renderGlobal.damagedBlocks.values()) {
            if (destBlockProgress.getPosition().getX() == pos.getX() && destBlockProgress.getPosition().getY() == pos.getY() && destBlockProgress.getPosition().getZ() == pos.getZ()) {
                return destBlockProgress.getPartialBlockDamage();
            }
        }
        return 0;
    }

    private BlockPos traceToBlock(float partialTicks, float yaw) {
        final Vec3d pos = MathUtil.interpolateEntity(Minecraft.getMinecraft().player, partialTicks);
        final Vec3d dir = MathUtil.direction(yaw);

        return new BlockPos(pos.x + dir.x, pos.y, pos.z + dir.z);
    }

    private Block getBlock(BlockPos pos) {
        final Block block = Minecraft.getMinecraft().world.getBlockState(pos).getBlock();

        if ((block == Blocks.BEDROCK) || (block == Blocks.OBSIDIAN)) {
            return block;
        }

        return Blocks.AIR;
    }

    private void drawBlock(Block block, float x, float y) {
        final ItemStack stack = new ItemStack(block);

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.translate(x, y, 0);
        Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(stack, 0, 0);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableBlend();
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.popMatrix();
    }

}
