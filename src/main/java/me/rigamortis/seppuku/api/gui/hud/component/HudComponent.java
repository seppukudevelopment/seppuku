package me.rigamortis.seppuku.api.gui.hud.component;

/**
 * Author Seth
 * 7/25/2019 @ 4:14 AM.
 */
public class HudComponent {

    private float x;
    private float y;
    private float w;
    private float h;

    private String name;
    private boolean visible;

    public HudComponent() {

    }

    public HudComponent(float x, float y, float w, float h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public void render(int mouseX, int mouseY, float partialTicks) {

    }

    public void mouseClickMove(int mouseX, int mouseY, int button) {

    }

    public void mouseClick(int mouseX, int mouseY, int button) {

    }

    public void mouseRelease(int mouseX, int mouseY, int button) {

    }

    public boolean collidesWith(HudComponent other) {
        // Collision x-axis?
        boolean collisionX = this.x + this.w > other.x &&
                other.x + other.w > this.x;
        // Collision y-axis?
        boolean collisionY = this.y + this.h > other.y &&
                other.y + other.h > this.y;
        // Collision only if on both axes
        return collisionX && collisionY;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
