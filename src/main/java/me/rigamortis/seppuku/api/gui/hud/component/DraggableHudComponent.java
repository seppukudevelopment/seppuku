package me.rigamortis.seppuku.api.gui.hud.component;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.util.RenderUtil;
import me.rigamortis.seppuku.impl.gui.hud.GuiHudEditor;
import me.rigamortis.seppuku.impl.gui.hud.anchor.AnchorPoint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.math.MathHelper;

/**
 * Author Seth
 * 7/25/2019 @ 7:17 AM.
 */
public class DraggableHudComponent extends HudComponent {

    private boolean dragging;
    private float deltaX;
    private float deltaY;

    private AnchorPoint anchorPoint;

    private DraggableHudComponent glued;
    private GlueSide glueSide;

    public DraggableHudComponent(String name) {
        this.setName(name);
        this.setVisible(false);
        this.setX(Minecraft.getMinecraft().displayWidth / 2.0f);
        this.setY(Minecraft.getMinecraft().displayHeight / 2.0f);
    }

    @Override
    public void mouseClick(int mouseX, int mouseY, int button) {
        final boolean inside = mouseX >= this.getX() && mouseX <= this.getX() + this.getW() && mouseY >= this.getY() && mouseY <= this.getY() + this.getH();

        if (inside) {
            if (button == 0) {
                this.setDragging(true);
                this.setDeltaX(mouseX - this.getX());
                this.setDeltaY(mouseY - this.getY());
                Seppuku.INSTANCE.getHudManager().moveToTop(this);
                this.anchorPoint = null;
                this.glued = null;
                this.glueSide = null;
            }
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        if (this.isDragging()) {
            this.setX(mouseX - this.getDeltaX());
            this.setY(mouseY - this.getDeltaY());
            this.clamp();
        }

        final boolean inside = mouseX >= this.getX() && mouseX <= this.getX() + this.getW() && mouseY >= this.getY() && mouseY <= this.getY() + this.getH();
        if (inside) {
            RenderUtil.drawRect(this.getX(), this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), 0x45FFFFFF);
        }

        if (Minecraft.getMinecraft().currentScreen instanceof GuiHudEditor) {
            RenderUtil.drawRect(this.getX(), this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), 0x75101010);
        }

        if (this.glued != null) {
            if (this.glued.getAnchorPoint() == null) {
                if (this.anchorPoint != null) {
                    this.anchorPoint = null;
                }
            } else {
                this.anchorPoint = this.glued.getAnchorPoint();
            }
        }

        if (this.anchorPoint == null && this.glued != null) {
            this.setX(this.glued.getX());
            if (this.glueSide != null) {
                switch (this.glueSide) {
                    case TOP:
                        this.setY(this.glued.getY() - this.getH());
                        break;
                    case BOTTOM:
                        this.setY(this.glued.getY() + this.glued.getH());
                        break;
                }
            }
        }

        if (!this.isDragging()) {
            if (this.anchorPoint != null && this.glued != null) {
                switch (this.anchorPoint.getPoint()) {
                    case TOP_LEFT:
                        this.setX(this.anchorPoint.getX());
                        break;
                    case BOTTOM_LEFT:
                        this.setX(this.anchorPoint.getX());
                        break;
                    case TOP_RIGHT:
                        this.setX(this.anchorPoint.getX() - this.getW());
                        break;
                    case BOTTOM_RIGHT:
                        this.setX(this.anchorPoint.getX() - this.getW());
                        break;
                    case TOP_CENTER:
                        this.setX(this.anchorPoint.getX() - (this.getW() / 2));
                        break;
                }
                if (this.glueSide != null) {
                    switch (this.glueSide) {
                        case TOP:
                            this.setY(this.glued.getY() - this.getH());
                            break;
                        case BOTTOM:
                            this.setY(this.glued.getY() + this.glued.getH());
                            break;
                    }
                }
            } else if (this.anchorPoint != null) {
                switch (this.anchorPoint.getPoint()) {
                    case TOP_LEFT:
                        this.setX(this.anchorPoint.getX());
                        this.setY(this.anchorPoint.getY());
                        break;
                    case BOTTOM_LEFT:
                        this.setX(this.anchorPoint.getX());
                        this.setY(this.anchorPoint.getY() - this.getH());
                        break;
                    case TOP_RIGHT:
                        this.setX(this.anchorPoint.getX() - this.getW());
                        this.setY(this.anchorPoint.getY());
                        break;
                    case BOTTOM_RIGHT:
                        this.setX(this.anchorPoint.getX() - this.getW());
                        this.setY(this.anchorPoint.getY() - this.getH());
                        break;
                    case TOP_CENTER:
                        this.setX(this.anchorPoint.getX() - (this.getW() / 2));
                        this.setY(this.anchorPoint.getY());
                        break;
                }
            }
        }

        this.clamp();
    }

    @Override
    public void mouseRelease(int mouseX, int mouseY, int button) {
        super.mouseRelease(mouseX, mouseY, button);

        if (button == 0) {
            if (this.isDragging()) {
                this.anchorPoint = this.findClosest(mouseX, mouseY);

                for (HudComponent component : Seppuku.INSTANCE.getHudManager().getComponentList()) {
                    if (component instanceof DraggableHudComponent) {
                        DraggableHudComponent draggable = (DraggableHudComponent) component;
                        if (draggable != this && this.collidesWith(draggable) && draggable.isVisible()) {
                            if ((this.getY() + (this.getH() / 2)) < (draggable.getY() + (draggable.getH() / 2))) { // top
                                this.setY(draggable.getY() - this.getH());
                                this.glueSide = GlueSide.TOP;
                                this.glued = draggable;
                                if (draggable.getAnchorPoint() != null) {
                                    this.anchorPoint = draggable.getAnchorPoint();
                                }
                            } else if ((this.getY() + (this.getH() / 2)) > (draggable.getY() + (draggable.getH() / 2))) { // bottom
                                this.setY(draggable.getY() + draggable.getH());
                                this.glueSide = GlueSide.BOTTOM;
                                this.glued = draggable;
                                if (draggable.getAnchorPoint() != null) {
                                    this.anchorPoint = draggable.getAnchorPoint();
                                }
                            }
                        }
                    }
                }
            }

            this.setDragging(false);
        }
    }

    private AnchorPoint findClosest(int x, int y) {
        AnchorPoint ret = null;
        double max = 100;
        for (AnchorPoint point : Seppuku.INSTANCE.getHudManager().getAnchorPoints()) {
            final double deltaX = x - point.getX();
            final double deltaY = y - point.getY();

            final double dist = MathHelper.sqrt(deltaX * deltaX + deltaY * deltaY);
            if (dist <= max) {
                max = dist;
                ret = point;
            }
        }
        return ret;
    }

    public void clamp() {
        if (this.getX() <= 0) {
            this.setX(2);
        }

        if (this.getY() <= 0) {
            this.setY(2);
        }

        final ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());

        if (this.getX() + this.getW() >= res.getScaledWidth() - 2) {
            this.setX(res.getScaledWidth() - 2 - this.getW());
        }

        if (this.getY() + this.getH() >= res.getScaledHeight() - 2) {
            this.setY(res.getScaledHeight() - 2 - this.getH());
        }
    }

    public boolean collides() {
        return false;
    }

    public boolean isDragging() {
        return dragging;
    }

    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }

    public float getDeltaX() {
        return deltaX;
    }

    public void setDeltaX(float deltaX) {
        this.deltaX = deltaX;
    }

    public float getDeltaY() {
        return deltaY;
    }

    public void setDeltaY(float deltaY) {
        this.deltaY = deltaY;
    }

    public AnchorPoint getAnchorPoint() {
        return anchorPoint;
    }

    public void setAnchorPoint(AnchorPoint anchorPoint) {
        this.anchorPoint = anchorPoint;
    }

    public HudComponent getGlued() {
        return glued;
    }

    public void setGlued(DraggableHudComponent glued) {
        this.glued = glued;
    }

    public GlueSide getGlueSide() {
        return glueSide;
    }

    public void setGlueSide(GlueSide glueSide) {
        this.glueSide = glueSide;
    }

    public enum GlueSide {
        TOP, BOTTOM
    }
}
