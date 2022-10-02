package me.rigamortis.seppuku.api.gui.hud.component;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.texture.Texture;
import me.rigamortis.seppuku.api.util.ColorUtil;
import me.rigamortis.seppuku.api.util.RenderUtil;

import java.awt.*;

public class ColorComponent extends TextComponent {

    // space occupied from left to right: border, color box, spacing, text, spacing, check, spacing, gear, border
    private static final int COLOR_SIZE = 7;
    private static final int GEAR_WIDTH = 8;
    private final Texture gearTexture;
    private final Texture gearTextureEnabled;
    private Color currentColor;
    private String customDisplayValue;

    public ColorComponent(String name, int defaultColor) {
        super(name, String.valueOf(defaultColor), false);
        this.currentColor = new Color(defaultColor);
        this.setText("#" + Integer.toHexString(this.currentColor.getRGB()).toLowerCase().substring(2));
        this.gearTexture = new Texture("gear_wheel.png");
        this.gearTextureEnabled = new Texture("gear_wheel-enabled.png");

        this.setH(9);
    }

    public ColorComponent(String name, int defaultColor, String customDisplayValue) {
        this(name, defaultColor);
        this.customDisplayValue = customDisplayValue;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        // draw text component, reserving space for gear and color rectangle
        // only show color hex value if focused, else, show value's name
        String displayedName = null;
        if (this.focused) {
            displayedName = "";
        } else if (customDisplayValue != null) {
            displayedName = customDisplayValue;
        } else if (this.getDisplayName() != null) {
            displayedName = this.getDisplayName();
        } else {
            displayedName = this.getName();
        }

        this.renderReserved(mouseX, mouseY, partialTicks, displayedName, this.focused, SPACING + COLOR_SIZE, SPACING + GEAR_WIDTH + SPACING);

        // draw color rect
        RenderUtil.drawRect(this.getX() + BORDER, this.getY() + BORDER, this.getX() + BORDER + COLOR_SIZE, this.getY() + BORDER + COLOR_SIZE, ColorUtil.changeAlpha(this.currentColor.getRGB(), 0xFF));

        // draw gear
        final float gearOffset = this.getX() + this.getW() - BORDER - GEAR_WIDTH;
        if (this.focused) {
            RenderUtil.drawRect(gearOffset - SPACING, this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), 0xFF101010);
            this.gearTextureEnabled.bind();
            this.gearTextureEnabled.render(gearOffset, this.getY() + ICON_V_OFFSET, GEAR_WIDTH, GEAR_WIDTH);
        } else {
            this.gearTexture.bind();
            this.gearTexture.render(gearOffset, this.getY() + ICON_V_OFFSET, GEAR_WIDTH, GEAR_WIDTH);
        }
    }

    @Override
    public void mouseRelease(int mouseX, int mouseY, int button) {
        super.mouseRelease(mouseX, mouseY, button);

        if (!this.focused) // must be focused
            return;

        if (button == 0) {
            // check for clicking check check, spacing, gear, border
            final float right = this.getX() + this.getW() - BORDER - GEAR_WIDTH - SPACING;
            if (mouseX >= right - CHECK_WIDTH && mouseX <= right && mouseY >= this.getY() && mouseY <= this.getY() + this.getH()) {
                this.enterPressed();
            }
        }
    }

    @Override
    protected void enterPressed() {
        try {
            int newColor = (int) Long.parseLong(this.getText().replaceAll("#", ""), 16);
            this.currentColor = new Color(newColor);
        } catch (NumberFormatException e) {
            Seppuku.INSTANCE.logChat(this.getName() + ": Invalid color format. Correct format example: \"ff0000\" for red.");
        } catch (Exception e) {
            Seppuku.INSTANCE.logChat(this.getName() + ": Something went terribly wrong while setting the color. Please try again.");
        }

        super.enterPressed();
    }

    public Color getCurrentColor() {
        return currentColor;
    }

    public void setCurrentColor(Color currentColor) {
        this.currentColor = currentColor;
    }

    public String getCustomDisplayValue() {
        return customDisplayValue;
    }

    public void setCustomDisplayValue(String customDisplayValue) {
        this.customDisplayValue = customDisplayValue;
    }
}
