package me.rigamortis.seppuku.api.gui.hud.component;

import me.rigamortis.seppuku.api.util.RenderUtil;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.client.Minecraft;

/**
 * @author noil
 */
public final class CarouselComponent extends HudComponent {

    private Value value;

    public String displayValue;
    public boolean focused;

    public CarouselComponent(String name, Value value) {
        super(name);

        this.value = value;
        this.displayValue = value.getCapitalizedValue();
        this.focused = false;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        if (isMouseInside(mouseX, mouseY))
            RenderUtil.drawGradientRect(this.getX(), this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), 0x30909090, 0x00101010);

        RenderUtil.drawRect(this.getX(), this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), 0x45303030);

        final String displayValueText = this.getName() + ": " + this.displayValue;
        Minecraft.getMinecraft().fontRenderer.drawString(displayValueText, (int) this.getX() + 1, (int) this.getY() + 1, this.focused ? 0xFFFFFFFF : 0xFFAAAAAA);

        RenderUtil.drawRect(this.getX() + this.getW() - 18, this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), 0xFF101010);
        RenderUtil.drawTriangle(this.getX() + this.getW() - 14, this.getY() + 4, 3, -90, this.focused ? 0x75FFFFFF : 0x75909090);
        RenderUtil.drawTriangle(this.getX() + this.getW() - 4, this.getY() + 4, 3, 90, this.focused ? 0x75FFFFFF : 0x75909090);
    }

    @Override
    public void mouseRelease(int mouseX, int mouseY, int button) {
        super.mouseRelease(mouseX, mouseY, button);

        if (this.isMouseInside(mouseX, mouseY) && button == 0) {
            this.focus();

            this.onLeftButtonPress(mouseX);
            this.onRightButtonPress(mouseX);

            this.displayValue = this.value.getCapitalizedValue();
        } else {
            this.focused = false;
        }
    }

    protected boolean onLeftButtonPress(int mouseX) {
        if (mouseX >= this.getX() + this.getW() - 18 && mouseX <= this.getX() + this.getW() - 10) {
            this.declineOption();
            return true;
        }
        return false;
    }

    protected boolean onRightButtonPress(int mouseX) {
        if (mouseX >= this.getX() + this.getW() - 8 && mouseX <= this.getX() + this.getW()) {
            this.raiseOption();
            return true;
        }
        return false;
    }

    public void focus() {
        this.focused = true;
    }

    public void raiseOption() {
        final Enum[] options = ((Enum) this.value.getValue()).getClass().getEnumConstants();
        for (int index = 0; index < options.length; index++) {
            if (options[index].name().equalsIgnoreCase(value.getValue().toString())) {
                index++;
                if (index > options.length - 1)
                    index = 0;
                value.setEnumValue(options[index].toString());
            }
        }
    }

    public void declineOption() {
        final Enum[] options = ((Enum) this.value.getValue()).getClass().getEnumConstants();
        for (int index = 0; index < options.length; index++) {
            if (options[index].name().equalsIgnoreCase(value.getValue().toString())) {
                index--;
                if (index < 0)
                    index = options.length - 1;
                value.setEnumValue(options[index].toString());
            }
        }
    }
}
