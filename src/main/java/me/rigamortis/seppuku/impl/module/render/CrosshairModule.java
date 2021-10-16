package me.rigamortis.seppuku.impl.module.render;

import me.rigamortis.seppuku.api.event.render.EventRender2D;
import me.rigamortis.seppuku.api.event.render.EventRenderCrosshairs;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

public final class CrosshairModule extends Module {

    public final Value<Float> size = new Value<Float>("Size", new String[]{"chsize", "CrosshairSize", "CrosshairScale", "size", "scale", "crosssize", "cs"}, "The size of the crosshair in pixels", 5.0f, 0.5f, 15.0f, 0.1f);
    public final Value<Float> thickness = new Value<Float>("Thickness", new String[]{"thickness", "thick", "t"}, "The thickness of the crosshair in pixels", 1.0f, 0.5f, 15.0f, 0.1f);
    public final Value<Integer> alpha = new Value<Integer>("Alpha", new String[]{"chalpha", "CrosshairAlpha", "alpha", "ca", "cha"}, "The alpha RGBA value of the crosshair", 255, 1, 255, 1);

    public final Value<Boolean> outline = new Value<Boolean>("Outline", new String[]{"choutline", "CrosshairOutline", "CrosshairBorder", "CrosshairB", "outline", "out", "chout", "chb", "crossb", "b"}, "Enable or disable the crosshair border/outline", true);
    public final Value<Color> outlineColor = new Value<Color>("OutlineColor", new String[]{"outlinecolor", "oc"}, "Change the color of the cross-hair's outline", new Color(0, 0, 0));
    public final Value<Float> outlineThickness = new Value<Float>("OutlineThickness", new String[]{"outlinethickness", "outlinethick", "outlinet", "ot", "othickness", "othick"}, "The thickness of the crosshair's border/outline in pixels. Some GPUs don't support this", 1.0f, 0.5f, 15.0f, 0.1f);

    public final Value<Boolean> fill = new Value<Boolean>("Fill", new String[]{"cfill", "CrosshairFill", "CrosshairBackground", "CrosshairBg", "outline", "out", "chout", "chbg", "crossbg", "bg"}, "Enable or disable the crosshair background/outline", true);
    public final Value<Color> fillColor = new Value<Color>("FillColor", new String[]{"fillcolor", "fc"}, "Change the color of the cross-hair", new Color(255, 255, 255));
    public final Value<Boolean> fillInvert = new Value<Boolean>("FillInvert", new String[]{"FillInvert", "FInvert", "FillInv", "FInv", "FillNegative", "FNegative", "FillNeg", "FNeg", "invert", "inv", "negative", "neg", "fi", "fn"}, "Invert crosshair color like in vanilla", true);

    public CrosshairModule() {
        super("Crosshair", new String[]{"Cross", "Xhair", "Chair"}, "NONE", -1, ModuleType.RENDER);
        this.setDesc("Replaces the game's cross-hair with your own");
    }

    @Listener
    public void render2D(EventRender2D event) {
        // abort if crosshair is hidden
        if (!this.fill.getValue() && !this.outline.getValue()) {
            return;
        }

        final Minecraft mc = Minecraft.getMinecraft();
        final ScaledResolution sr = new ScaledResolution(mc);

        final float alpha = (float) this.alpha.getValue() / 255;

        float size = this.size.getValue();
        float thickness = this.thickness.getValue();

        // if thickness > size rendering fails. swap them if this is true
        if (thickness > size) {
            final float temp = size;
            size = thickness;
            thickness = temp;
        }

        final float xMid = (float) sr.getScaledWidth() / 2;
        final float yMid = (float) sr.getScaledHeight() / 2;
        final float halfSize = size / 2;
        final float left = xMid - halfSize;
        final float right = xMid + halfSize;
        final float top = yMid - halfSize;
        final float bottom = yMid + halfSize;
        final float halfThick = thickness / 2;
        final float xEdgeMin = xMid - halfThick;
        final float xEdgeMax = xMid + halfThick;
        final float yEdgeMin = yMid - halfThick;
        final float yEdgeMax = yMid + halfThick;

        // ussing tesselator directly since renderutil doesnt work with custom
        // blend functions (for color inversion) and this is more efficient
        // anyway
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();

        // crosshair fill
        if (this.fill.getValue()) {
            final Color fillColor = this.fillColor.getValue();
            final float red = (float) fillColor.getRed() / 255;
            final float green = (float) fillColor.getGreen() / 255;
            final float blue = (float) fillColor.getBlue() / 255;

            if (this.fillInvert.getValue()) {
                GlStateManager.tryBlendFuncSeparate(GL_ONE_MINUS_DST_COLOR, GL_ONE_MINUS_SRC_COLOR, 1, 0);
            } else {
                GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, 1, 0);
            }

            bufferbuilder.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

            // horizontal line
            bufferbuilder.pos(left, yEdgeMax, 0.0D).color(red, green, blue, alpha).endVertex();
            bufferbuilder.pos(right, yEdgeMax, 0.0D).color(red, green, blue, alpha).endVertex();
            bufferbuilder.pos(right, yEdgeMin, 0.0D).color(red, green, blue, alpha).endVertex();
            bufferbuilder.pos(left, yEdgeMin, 0.0D).color(red, green, blue, alpha).endVertex();
            // vertical line top
            // needs 2 quads for the vertical line so that there isn't overdraw,
            // which messes with alpha and invert mode
            bufferbuilder.pos(xEdgeMin, yEdgeMin, 0.0D).color(red, green, blue, alpha).endVertex();
            bufferbuilder.pos(xEdgeMax, yEdgeMin, 0.0D).color(red, green, blue, alpha).endVertex();
            bufferbuilder.pos(xEdgeMax, top, 0.0D).color(red, green, blue, alpha).endVertex();
            bufferbuilder.pos(xEdgeMin, top, 0.0D).color(red, green, blue, alpha).endVertex();
            // vertical line bottom
            bufferbuilder.pos(xEdgeMin, bottom, 0.0D).color(red, green, blue, alpha).endVertex();
            bufferbuilder.pos(xEdgeMax, bottom, 0.0D).color(red, green, blue, alpha).endVertex();
            bufferbuilder.pos(xEdgeMax, yEdgeMax, 0.0D).color(red, green, blue, alpha).endVertex();
            bufferbuilder.pos(xEdgeMin, yEdgeMax, 0.0D).color(red, green, blue, alpha).endVertex();
            /*
            bufferbuilder.pos(left, bottom, 0.0D).color(red, green, blue, alpha).endVertex();
            bufferbuilder.pos(right, bottom, 0.0D).color(red, green, blue, alpha).endVertex();
            bufferbuilder.pos(right, top, 0.0D).color(red, green, blue, alpha).endVertex();
            bufferbuilder.pos(left, top, 0.0D).color(red, green, blue, alpha).endVertex();
            */

            tessellator.draw();
        }

        // crosshair outline
        if (this.outline.getValue()) {
            final Color outlineColor = this.outlineColor.getValue();
            final float ored = (float) outlineColor.getRed() / 255;
            final float ogreen = (float) outlineColor.getGreen() / 255;
            final float oblue = (float) outlineColor.getBlue() / 255;

            GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, 1, 0);
            GlStateManager.shadeModel(GL_SMOOTH);
            glLineWidth(this.outlineThickness.getValue());
            glEnable(GL_LINE_SMOOTH);
            glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);

            bufferbuilder.begin(GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);

            // top left quadrant
            bufferbuilder.pos(left, yEdgeMin, 0).color(ored, ogreen, oblue, alpha).endVertex();
            bufferbuilder.pos(xEdgeMin, yEdgeMin, 0).color(ored, ogreen, oblue, alpha).endVertex();
            bufferbuilder.pos(xEdgeMin, top, 0).color(ored, ogreen, oblue, alpha).endVertex();
            // top
            bufferbuilder.pos(xEdgeMax, top, 0).color(ored, ogreen, oblue, alpha).endVertex();
            // top right quadrant
            bufferbuilder.pos(xEdgeMax, yEdgeMin, 0).color(ored, ogreen, oblue, alpha).endVertex();
            bufferbuilder.pos(right, yEdgeMin, 0).color(ored, ogreen, oblue, alpha).endVertex();
            // right
            bufferbuilder.pos(right, yEdgeMax, 0).color(ored, ogreen, oblue, alpha).endVertex();
            // bottom right quadrant
            bufferbuilder.pos(xEdgeMax, yEdgeMax, 0).color(ored, ogreen, oblue, alpha).endVertex();
            bufferbuilder.pos(xEdgeMax, bottom, 0).color(ored, ogreen, oblue, alpha).endVertex();
            // bottom
            bufferbuilder.pos(xEdgeMin, bottom, 0).color(ored, ogreen, oblue, alpha).endVertex();
            // bottom left quadrant (connects to left in the end)
            bufferbuilder.pos(xEdgeMin, yEdgeMax, 0).color(ored, ogreen, oblue, alpha).endVertex();
            bufferbuilder.pos(left, yEdgeMax, 0).color(ored, ogreen, oblue, alpha).endVertex();

            tessellator.draw();

            GlStateManager.shadeModel(GL_FLAT);
            glDisable(GL_LINE_SMOOTH);
        }

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    @Listener
    public void onRenderCrosshairs(EventRenderCrosshairs event) {
        event.setCanceled(true);
    }
}
