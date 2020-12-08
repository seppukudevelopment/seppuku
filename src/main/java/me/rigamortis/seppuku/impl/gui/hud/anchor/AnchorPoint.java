package me.rigamortis.seppuku.impl.gui.hud.anchor;

import net.minecraft.client.gui.ScaledResolution;

/**
 * Author Seth
 * 8/7/2019 @ 1:18 PM.
 */
public class AnchorPoint {

    private float x;
    private float y;

    private Point point;

    public AnchorPoint(Point point) {
        this.point = point;
    }

    public AnchorPoint(float x, float y, Point point) {
        this.x = x;
        this.y = y;
        this.point = point;
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

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    public enum Point {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, TOP_CENTER, BOTTOM_CENTER
    }

    public void updatePosition(final ScaledResolution sr) {
        switch (this.getPoint()) {
            case TOP_LEFT:
                this.x = 2;
                this.y = 2;
                break;
            case TOP_RIGHT:
                this.x = sr.getScaledWidth() - 2;
                this.y = 2;
                break;
            case BOTTOM_LEFT:
                this.x = 2;
                this.y = sr.getScaledHeight() - 2;
                break;
            case BOTTOM_RIGHT:
                this.x = sr.getScaledWidth() - 2;
                this.y = sr.getScaledHeight() - 2;
                break;
            case TOP_CENTER:
                this.x = sr.getScaledWidth() / 2.0f;
                this.y = 2;
                break;
            case BOTTOM_CENTER:
                this.x = sr.getScaledWidth() / 2.0f;
                this.y = sr.getScaledHeight() - 2;
                break;
        }
    }
}
