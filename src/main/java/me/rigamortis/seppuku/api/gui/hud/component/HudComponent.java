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

    private float emptyH;

    private String name;
    private String tooltipText = "No description to be found.";

    private boolean visible;

    public HudComponent() {

    }

    public HudComponent(String name) {
        this.name = name;
    }

    public HudComponent(String name, String tooltipText) {
        this.name = name;
        this.tooltipText = tooltipText;
    }

    public HudComponent(float x, float y, float w, float h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.emptyH = h;
    }

    public HudComponent(float x, float y, float w, float h, String name) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.name = name;
    }

    public HudComponent(float x, float y, float w, float h, String name, String tooltipText) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.name = name;
        this.tooltipText = tooltipText;
    }

    public void render(int mouseX, int mouseY, float partialTicks) {

    }

    public void mouseClickMove(int mouseX, int mouseY, int button) {

    }

    public void mouseClick(int mouseX, int mouseY, int button) {

    }

    public void mouseRelease(int mouseX, int mouseY, int button) {

    }

    public void keyTyped(char typedChar, int keyCode) {

    }

    public boolean isMouseInside(int mouseX, int mouseY) {
        return mouseX >= this.getX() && mouseX <= this.getX() + this.getW() && mouseY >= this.getY() && mouseY <= this.getY() + this.getH();
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

    public float getEmptyH() {
        return emptyH;
    }

    public void setEmptyH(float emptyH) {
        this.emptyH = emptyH;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTooltipText() {
        return tooltipText;
    }

    public void setTooltipText(String tooltipText) {
        this.tooltipText = tooltipText;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
