package me.rigamortis.seppuku.api.gui.hud.component;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.util.RenderUtil;
import me.rigamortis.seppuku.impl.gui.hud.GuiHudEditor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

/**
 * Author Seth
 * 12/10/2019 @ 1:26 AM.
 */
public class ResizableHudComponent extends DraggableHudComponent {

    private boolean resizeDragging;
    private float resizeDeltaX;
    private float resizeDeltaY;

    private float initialWidth;
    private float initialHeight;

    private final float CLICK_ZONE = 2;

    public ResizableHudComponent(String name, float initialWidth, float initialHeight) {
        super(name);
        this.initialWidth = initialWidth;
        this.initialHeight = initialHeight;
    }

    @Override
    public void mouseClick(int mouseX, int mouseY, int button) {
        super.mouseClick(mouseX, mouseY, button);
        final boolean inside = mouseX >= this.getX() + this.getW() - CLICK_ZONE && mouseX <= this.getX() + this.getW() + CLICK_ZONE && mouseY >= this.getY() + this.getH() - CLICK_ZONE && mouseY <= this.getY() + this.getH() + CLICK_ZONE;

        if (inside) {
            if (button == 0) {
                this.setResizeDragging(true);
                this.setDragging(false);
                this.setResizeDeltaX(mouseX - this.getW());
                this.setResizeDeltaY(mouseY - this.getH());
                Seppuku.INSTANCE.getHudManager().moveToTop(this);
            }
        }
    }


    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        if (this.isResizeDragging()) {
            this.setW(mouseX - this.getResizeDeltaX());
            this.setH(mouseY - this.getResizeDeltaY());
            this.clampMaxs();
        }

        final boolean inside = mouseX >= this.getX() + this.getW() - CLICK_ZONE && mouseX <= this.getX() + this.getW() + CLICK_ZONE && mouseY >= this.getY() + this.getH() - CLICK_ZONE && mouseY <= this.getY() + this.getH() + CLICK_ZONE;

        if (inside) {
            RenderUtil.drawRect(this.getX() + this.getW() - CLICK_ZONE, this.getY() + this.getH() - CLICK_ZONE, this.getX() + this.getW() + CLICK_ZONE, this.getY() + this.getH() + CLICK_ZONE, 0x45FFFFFF);
        }

        if (Minecraft.getMinecraft().currentScreen instanceof GuiHudEditor) {
            RenderUtil.drawRect(this.getX() + this.getW() - CLICK_ZONE, this.getY() + this.getH() - CLICK_ZONE, this.getX() + this.getW() + CLICK_ZONE, this.getY() + this.getH() + CLICK_ZONE, 0x75101010);
        }

        this.clampMaxs();
    }

    @Override
    public void mouseRelease(int mouseX, int mouseY, int button) {
        super.mouseRelease(mouseX, mouseY, button);

        if (button == 0) {
            if (this.isResizeDragging()) {
                this.setResizeDragging(false);
            }
        }
    }

    public void clampMaxs() {
        if (this.getW() <= this.getInitialWidth()) {
            this.setW(this.getInitialWidth());
        }

        if (this.getH() <= this.getInitialHeight()) {
            this.setH(this.getInitialHeight());
        }
    }

    public boolean isResizeDragging() {
        return resizeDragging;
    }

    public void setResizeDragging(boolean resizeDragging) {
        this.resizeDragging = resizeDragging;
    }

    public float getResizeDeltaX() {
        return resizeDeltaX;
    }

    public void setResizeDeltaX(float resizeDeltaX) {
        this.resizeDeltaX = resizeDeltaX;
    }

    public float getResizeDeltaY() {
        return resizeDeltaY;
    }

    public void setResizeDeltaY(float resizeDeltaY) {
        this.resizeDeltaY = resizeDeltaY;
    }

    public float getInitialWidth() {
        return initialWidth;
    }

    public void setInitialWidth(float initialWidth) {
        this.initialWidth = initialWidth;
    }

    public float getInitialHeight() {
        return initialHeight;
    }

    public void setInitialHeight(float initialHeight) {
        this.initialHeight = initialHeight;
    }
}
