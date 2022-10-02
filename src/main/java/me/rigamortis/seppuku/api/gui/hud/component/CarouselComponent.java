package me.rigamortis.seppuku.api.gui.hud.component;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.util.RenderUtil;
import me.rigamortis.seppuku.api.value.Shader;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author noil
 */
public final class CarouselComponent extends HudComponent {

    public String displayValue;
    public boolean focused;
    private final Value value;

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

        if (this.subComponents > 0) {
            final int dotColor = this.rightClickEnabled ? 0xFF6D55FF : (this.focused ? 0x75FFFFFF : 0x75909090);
            RenderUtil.drawRect(this.getX() + this.getW() - 27, this.getY(), this.getX() + this.getW() - 18, this.getY() + this.getH(), 0xFF101010);
            RenderUtil.drawRect(this.getX() + this.getW() - 21, this.getY() + this.getH() - 2, this.getX() + this.getW() - 20, this.getY() + this.getH() - 1, dotColor);
            RenderUtil.drawRect(this.getX() + this.getW() - 23, this.getY() + this.getH() - 2, this.getX() + this.getW() - 22, this.getY() + this.getH() - 1, dotColor);
            RenderUtil.drawRect(this.getX() + this.getW() - 25, this.getY() + this.getH() - 2, this.getX() + this.getW() - 24, this.getY() + this.getH() - 1, dotColor);
        }

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
        } else if (mouseX >= this.getX() + this.getW() - 25 && mouseX <= this.getX() + this.getW() - 20) {
            this.rightClickEnabled = !this.rightClickEnabled;

            if (!this.rightClickEnabled) {
                this.focused = false;
            }
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

    protected String getValueAsOption() {
        final Object wrappedValue = this.value.getValue();
        if (wrappedValue instanceof Shader) {
            return ((Shader) wrappedValue).getShaderID();
        } else {
            return wrappedValue.toString().toLowerCase();
        }
    }

    protected ArrayList<String> getOptions() {
        final Object wrappedValue = this.value.getValue();
        final ArrayList<String> options = new ArrayList<String>();
        if (wrappedValue instanceof Shader) {
            options.add(""); // always add an option for having no shader
            for (Iterator<String> it = Seppuku.INSTANCE.getShaderManager().getShaderList(); it.hasNext(); ) {
                options.add(it.next());
            }
        } else {
            for (Enum<?> e : ((Enum<?>) this.value.getValue()).getClass().getEnumConstants()) {
                options.add(e.name().toLowerCase());
            }
        }

        return options;
    }

    protected void pickOption(String option) {
        final Object wrappedValue = this.value.getValue();
        if (wrappedValue instanceof Shader) {
            ((Shader) wrappedValue).setShaderID(option);
        } else {
            this.value.setEnumValue(option);
        }
    }

    private void moveOption(int delta) {
        String curValue = this.getValueAsOption();
        ArrayList<String> options = this.getOptions();

        int index = 0;
        for (String option : options) {
            if (option.equals(curValue)) {
                index += delta;
                break;
            }

            index++;
        }

        index = index % options.size();
        if (index < 0) {
            // java is a disgrace of a language and allows modulo output to be
            // negative
            index += options.size();
        }

        this.pickOption(options.get(index));
    }

    public void raiseOption() {
        this.moveOption(1);
    }

    public void declineOption() {
        this.moveOption(-1);
    }
}
