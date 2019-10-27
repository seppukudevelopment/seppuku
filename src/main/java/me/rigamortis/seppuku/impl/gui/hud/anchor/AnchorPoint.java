package me.rigamortis.seppuku.impl.gui.hud.anchor;

/**
 * Author Seth
 * 8/7/2019 @ 1:18 PM.
 */
public class AnchorPoint {

    private float x;
    private float y;

    private Point point;

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

    public static enum Point {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, TOP_CENTER
    }

}
