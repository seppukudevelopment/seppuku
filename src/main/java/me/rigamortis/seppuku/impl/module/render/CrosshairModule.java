package me.rigamortis.seppuku.impl.module.render;

import me.rigamortis.seppuku.api.event.client.EventSaveConfig;
import me.rigamortis.seppuku.api.event.render.EventRender2D;
import me.rigamortis.seppuku.api.event.render.EventRenderCrosshairs;
import me.rigamortis.seppuku.api.event.world.EventLoadWorld;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.ColorUtil;
import me.rigamortis.seppuku.api.util.RenderUtil;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.awt.*;

public final class CrosshairModule extends Module {

    public final Value<Float> size = new Value<Float>("Size", new String[]{"chsize", "CrosshairSize", "CrosshairScale", "size", "scale", "crosssize", "cs"}, "The size of the crosshair in pixels.", 5.0f, 0.1f, 15.0f, 0.1f);
    public final Value<Boolean> outline = new Value<Boolean>("Outline", new String[]{"choutline", "CrosshairOutline", "CrosshairBackground", "CrosshairBg", "outline", "out", "chout", "chbg", "crossbg", "bg"}, "Enable or disable the crosshair background/outline.", true);
    public final Value<Color> color = new Value<Color>("Color", new String[]{"color", "c"}, "Change the color of the cross-hair.", new Color(255, 255, 255));
    public final Value<Integer> alpha = new Value<Integer>("Alpha", new String[]{"chalpha", "CrosshairAlpha", "alpha", "ca", "cha"}, "The alpha RGBA value of the crosshair.", 255, 1, 255, 1);

    private int CROSSHAIR_COLOR = 0xFFFFFFFF;
    private int CROSSHAIR_OUTLINE_COLOR = 0xFF000000;

    private float x, y, w, h;

    public CrosshairModule() {
        super("Crosshair", new String[]{"Cross", "Xhair", "Chair"}, "NONE", -1, ModuleType.RENDER);
        this.setDesc("Replaces the game's cross-hair with your own.");
    }

    @Listener
    public void render2D(EventRender2D event) {
        final Minecraft mc = Minecraft.getMinecraft();
        final ScaledResolution sr = new ScaledResolution(mc);

        this.setW(this.size.getValue());
        this.setH(this.size.getValue());

        this.setX((sr.getScaledWidth() / 2.0f) - (this.getW() / 2.0f));
        this.setY((sr.getScaledHeight() / 2.0f) - (this.getH() / 2.0f));

        // render bg
        if (this.outline.getValue()) {
            //RenderUtil.drawBorderedRect(this.getX() + (this.getW() / 2.0f) - 0.5f, this.getY() - 0.5f, this.getX() + (this.getW() / 2.0f), this.getY() + this.getH() + 0.5f, 1f, 0x00000000, CROSSHAIR_OUTLINE_COLOR);
            //RenderUtil.drawBorderedRect(this.getX() - 1f, this.getY() + (this.getH() / 2.0f), this.getX() + this.getW() + 1f, this.getY() + (this.getH() / 2.0f) + 0.5f, 1f, 0x00000000, CROSSHAIR_OUTLINE_COLOR);
            RenderUtil.drawRect(this.getX() + (this.getW() / 2.0f) - 1f, this.getY() - 0.5f, this.getX() + (this.getW() / 2.0f) + 0.5f, this.getY() + this.getH() + 1f, CROSSHAIR_OUTLINE_COLOR);
            RenderUtil.drawRect(this.getX() - 1f, this.getY() + (this.getH() / 2.0f) - 0.5f, this.getX() + this.getW() + 0.5f, this.getY() + (this.getH() / 2.0f) + 1f, CROSSHAIR_OUTLINE_COLOR);
        }

        // render plus sign
        RenderUtil.drawThinLine(this.getX() + (this.getW() / 2.0f), this.getY(), this.getX() + (this.getW() / 2.0f), this.getY() + this.getH() + 0.5f, CROSSHAIR_COLOR);
        RenderUtil.drawThinLine(this.getX() - 0.5f, this.getY() + (this.getH() / 2.0f), this.getX() + this.getW(), this.getY() + (this.getH() / 2.0f), CROSSHAIR_COLOR);
    }

    @Listener
    public void onRenderCrosshairs(EventRenderCrosshairs event) {
        event.setCanceled(true);
    }

    @Listener
    public void onLoadWorld(EventLoadWorld eventLoadWorld) {
        this.updateColors();
    }

    @Listener
    public void onConfigSave(EventSaveConfig eventSaveConfig) {
        this.updateColors();
    }

    private void updateColors() {
        this.CROSSHAIR_COLOR = ColorUtil.changeAlpha(new Color(this.color.getValue().getRed(), this.color.getValue().getGreen(), this.color.getValue().getBlue()).getRGB(), this.alpha.getValue());
        this.CROSSHAIR_OUTLINE_COLOR = ColorUtil.changeAlpha(0xFF000000, this.alpha.getValue());
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getW() {
        return w;
    }

    public void setW(float w) {
        this.w = w;
    }

    public float getH() {
        return h;
    }

    public void setH(float h) {
        this.h = h;
    }
}
