package me.rigamortis.seppuku.impl.module.render;

import me.rigamortis.seppuku.api.event.render.EventRender3D;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.ColorUtil;
import me.rigamortis.seppuku.api.util.MathUtil;
import me.rigamortis.seppuku.api.util.RenderUtil;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.awt.*;

/**
 * @author Seth
 * @author noil
 */
public final class BlockHighlightModule extends Module {

    public final Value<Mode> mode = new Value<Mode>("Mode", new String[]{"M", "type", "t"}, "Select which mode to draw the highlight visual.", Mode.BOX);
    public final Value<Color> color = new Value<Color>("Color", new String[]{"Col", "c"}, "Edit the block highlight color.", new Color(255, 255, 255));
    public final Value<Integer> alpha = new Value<Integer>("Alpha", new String[]{"Alp", "Opacity", "a", "o"}, "Alpha value for the highlight visual.", 127, 1, 255, 1);
    public final Value<Float> width = new Value<Float>("Width", new String[]{"W", "size", "s"}, "Width value of the highlight visual.", 1.5f, 0.1f, 5.0f, 0.1f);
    public final Value<Boolean> breaking = new Value<Boolean>("Breaking", new String[]{"Break", "block", "brk"}, "Sizes the highlight visual based on the block breaking damage.", false);

    public enum Mode {
        BOX, OUTLINE, CROSS
    }

    public BlockHighlightModule() {
        super("BlockHighlight", new String[]{"BHighlight", "BlockHigh"}, "Highlights the block at your cross-hair", "NONE", -1, ModuleType.RENDER);
    }

    @Listener
    public void render3D(EventRender3D event) {
        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null)
            return;

        if (mc.objectMouseOver != null) {
            final RayTraceResult ray = mc.objectMouseOver;
            if (ray.typeOfHit == RayTraceResult.Type.BLOCK) {
                this.drawHighlight(ray, mc);
            }
        }
    }

    public void drawHighlight(final RayTraceResult ray, final Minecraft mc) {
        final BlockPos blockpos = ray.getBlockPos();
        final IBlockState iblockstate = mc.world.getBlockState(blockpos);
        if (iblockstate.getMaterial() != Material.AIR && mc.world.getWorldBorder().contains(blockpos)) {
            float currentDamage = 0.0f;

            if (mc.player.isSwingInProgress) {
                if (this.breaking.getValue()) {
                    currentDamage = Math.abs(mc.playerController.curBlockDamageMP);
                } else {
                    currentDamage = 0.0f;
                }
            }

            GlStateManager.color(1.0f, 1.0f, 1.0f);
            RenderUtil.begin3D();
            final Vec3d interp = MathUtil.interpolateEntity(mc.player, mc.getRenderPartialTicks());
            final AxisAlignedBB bb = iblockstate.getSelectedBoundingBox(mc.world, blockpos).shrink(currentDamage / 2.0f).offset(-interp.x, -interp.y, -interp.z);
            final int color = ColorUtil.changeAlpha(this.color.getValue().getRGB(), this.alpha.getValue());
            switch (this.mode.getValue()) {
                case BOX:
                    RenderUtil.drawFilledBox(bb, ColorUtil.changeAlpha(color, this.alpha.getValue() / 2));
                    RenderUtil.drawBoundingBox(bb, this.width.getValue(), color);
                    break;
                case OUTLINE:
                    RenderUtil.drawBoundingBox(bb, this.width.getValue(), color);
                    break;
                case CROSS:
                    RenderUtil.drawFilledBox(bb, ColorUtil.changeAlpha(color, this.alpha.getValue() / 2));
                    RenderUtil.drawCrosses(bb, this.width.getValue(), color);
                    RenderUtil.drawBoundingBox(bb, this.width.getValue(), color);
                    break;
            }
            RenderUtil.end3D();
        }
    }
}
