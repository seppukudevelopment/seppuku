package me.rigamortis.seppuku.api.gui.hud.component;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.texture.Texture;
import me.rigamortis.seppuku.api.util.RenderUtil;
import me.rigamortis.seppuku.api.util.Timer;
import me.rigamortis.seppuku.impl.gui.hud.component.ColorsComponent;
import me.rigamortis.seppuku.impl.gui.hud.component.module.ModuleListComponent;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.util.logging.Level;

/**
 * @author noil
 */
public class TextComponent extends HudComponent {

    public String displayValue, selectedText;
    public boolean focused;
    public boolean digitOnly;
    public ComponentListener returnListener;
    public TextComponentListener textListener;

    protected Texture checkTexture;

    protected Timer backspaceTimer = new Timer(), backspaceWaitTimer = new Timer();
    protected boolean doBackspacing = false;

    private int shiftLength = 0;

    private static final int CHECK_WIDTH = 10;
    private static final int BLOCK_WIDTH = 2;

    public TextComponent(String name, String displayValue, boolean digitOnly) {
        super(name);

        this.displayValue = displayValue;
        this.selectedText = "";
        this.focused = false;
        this.digitOnly = digitOnly;

        this.checkTexture = new Texture("check.png");
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        if (isMouseInside(mouseX, mouseY))
            RenderUtil.drawGradientRect(this.getX(), this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), 0x30909090, 0x00101010);

        RenderUtil.drawRect(this.getX(), this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), 0x45303030);

        String renderName = this.getName();
        if (this.getDisplayName() != null) {
            renderName = this.getDisplayName();
        }

        final String displayValueText = renderName + ": " + this.displayValue;
        this.shiftLength = 0;
        if (this.focused) {
            if (Minecraft.getMinecraft().fontRenderer.getStringWidth(displayValueText) > (this.getW() - CHECK_WIDTH - BLOCK_WIDTH - 2))
                this.shiftLength += Math.abs(Minecraft.getMinecraft().fontRenderer.getStringWidth(displayValueText) - (this.getW() - CHECK_WIDTH - BLOCK_WIDTH - 2));
        }

        Minecraft.getMinecraft().fontRenderer.drawString(displayValueText, (int) this.getX() + 1 - this.shiftLength, (int) this.getY() + 1, this.focused ? 0xFFFFFFFF : 0xFFAAAAAA);

        if (this.focused) {
            if (!this.selectedText.equals("")) {
                RenderUtil.drawRect(this.getX() + Minecraft.getMinecraft().fontRenderer.getStringWidth(renderName + ": ") - this.shiftLength, this.getY(), this.getX() + Minecraft.getMinecraft().fontRenderer.getStringWidth(displayValueText), this.getY() + this.getH(), 0x45FFFFFF);
            }

            float blockX = this.getX() + 1 - this.shiftLength + Minecraft.getMinecraft().fontRenderer.getStringWidth(renderName + ": " + this.displayValue);
            float blockY = this.getY() + 1;
            final int blockHeight = Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT - 2;
            RenderUtil.drawRect(blockX, blockY, blockX + BLOCK_WIDTH, blockY + blockHeight, 0xFFFFFFFF);

            // check
            RenderUtil.drawRect(this.getX() + this.getW() - CHECK_WIDTH, this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), 0xFF101010);
            this.checkTexture.bind();
            this.checkTexture.render(this.getX() + this.getW() - 9, this.getY() + 0.5f, 8, 8);

            // handle holding backspace
            this.handleBackspacing();
        }
    }

    @Override
    public void mouseClick(int mouseX, int mouseY, int button) {
        super.mouseClick(mouseX, mouseY, button);
        if (this.isMouseInside(mouseX, mouseY)) {
            for (HudComponent hudComponent : Seppuku.INSTANCE.getHudManager().getComponentList()) {
                if (hudComponent instanceof ModuleListComponent) {
                    final ModuleListComponent moduleListComponent = (ModuleListComponent) hudComponent;
                    if (moduleListComponent.getCurrentSettings() != null) {
                        for (HudComponent moduleComponent : moduleListComponent.getCurrentSettings().components) {
                            if (moduleComponent instanceof TextComponent && !this.getName().equals(moduleComponent.getName())) {
                                ((TextComponent) moduleComponent).focused = false;
                            }
                        }
                    }
                } else if (hudComponent instanceof ColorsComponent) {
                    final ColorsComponent colorsComponent = (ColorsComponent) hudComponent;
                    if (colorsComponent.getCurrentColorComponent() != null) {
                        if (!this.getName().equals(colorsComponent.getName())) {
                            colorsComponent.getCurrentColorComponent().focused = false;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void mouseRelease(int mouseX, int mouseY, int button) {
        super.mouseRelease(mouseX, mouseY, button);

        if (this.isMouseInside(mouseX, mouseY) && button == 0) {
            this.focus();

            // check for clicking check
            if (!(this instanceof ColorComponent)) {
                this.onCheckButtonPress(mouseX, mouseY);
            }
        } else {
            this.focused = false;
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        super.keyTyped(typedChar, keyCode);

        if (this.focused) {
            // invoke text listener
            if (textListener != null) {
                textListener.onKeyTyped(keyCode);
            }

            if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                switch (keyCode) {
                    case Keyboard.KEY_A:
                        this.selectedText = this.displayValue;
                        return;
                    case Keyboard.KEY_V:
                        if (!this.digitOnly) {
                            this.displayValue += this.getClipBoard();
                        } else if (this.getClipBoard().matches("[0-9]+") /* is a number */) {
                            this.displayValue += this.getClipBoard();
                        }
                        return;
                    case Keyboard.KEY_X:
                    case Keyboard.KEY_C:
                        return;
                }
            }

            switch (keyCode) {
                case Keyboard.KEY_ESCAPE:
                    this.focused = false;
                    return;
                case Keyboard.KEY_RETURN:
                    this.enterPressed();
                    return;
                //case Keyboard.KEY_SPACE:
                //    if (!this.digitOnly) {
                //        this.displayValue += ' ';
                //    }
                //    break;
                case Keyboard.KEY_BACK:
                case Keyboard.KEY_DELETE:
                    this.backspaceWaitTimer.reset();
                    this.doBackspacing = true;
                    if (this.displayValue.length() > 0) {
                        if (!this.onRemoveSelectedText()) {
                            this.displayValue = this.displayValue.substring(0, this.displayValue.length() - 1);
                        }
                    }
                    return;
                case Keyboard.KEY_CLEAR:
                    if (this.displayValue.length() > 0) {
                        this.displayValue = "";
                    }
                    return;
                case Keyboard.KEY_LEFT:
                case Keyboard.KEY_RIGHT:
                case Keyboard.KEY_UP:
                case Keyboard.KEY_DOWN:
                case Keyboard.KEY_LSHIFT:
                case Keyboard.KEY_RSHIFT:
                case Keyboard.KEY_LCONTROL:
                case Keyboard.KEY_RCONTROL:
                case Keyboard.KEY_TAB:
                case Keyboard.KEY_CAPITAL:
                case Keyboard.KEY_FUNCTION:
                case Keyboard.KEY_LMENU:
                case Keyboard.KEY_RMENU:
                case Keyboard.KEY_LMETA:
                    return;
                case Keyboard.KEY_PERIOD:
                    if (this.digitOnly) {
                        this.displayValue += typedChar;
                    }
                    break;
                default:
                    break;
            }

            if (digitOnly && !Character.isDigit(typedChar))
                return;

            this.onRemoveSelectedText();

            //if (!digitOnly && !Character.isLetterOrDigit(typedChar))
            //    return;

            this.displayValue += typedChar;
        }
    }

    protected void enterPressed() {
        // invoke return listener
        if (returnListener != null)
            returnListener.onComponentEvent();

        this.shiftLength = 0;
        this.focused = false;
    }

    protected boolean onCheckButtonPress(int mouseX, int mouseY) {
        if (mouseX >= this.getX() + this.getW() - CHECK_WIDTH && mouseX <= this.getX() + this.getW() && mouseY >= this.getY() && mouseY <= this.getY() + this.getH()) {
            this.enterPressed();
            return true;
        }
        return false;
    }

    protected boolean onRemoveSelectedText() {
        if (!this.selectedText.equals("")) {
            this.displayValue = "";
            this.selectedText = "";
            return true;
        }
        return false;
    }

    protected void handleBackspacing() {
        if (Keyboard.isKeyDown(Keyboard.KEY_BACK) || Keyboard.isKeyDown(Keyboard.KEY_DELETE)) {
            if (this.doBackspacing && this.backspaceWaitTimer.passed(600)) {
                if (this.backspaceTimer.passed(75)) {
                    if (this.displayValue.length() > 0) {
                        this.displayValue = this.displayValue.substring(0, this.displayValue.length() - 1);
                    }
                    this.backspaceTimer.reset();
                }
            }
        } else {
            this.doBackspacing = false;
        }
    }

    public String getClipBoard() {
        try {
            return (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
        } catch (Exception e) {
            Seppuku.INSTANCE.getLogger().log(Level.WARNING, "Error getting clipboard while using " + this.getName());
        }
        return "";
    }

    public void focus() {
        this.focused = true;
    }

    public interface TextComponentListener {
        void onKeyTyped(int keyCode);
    }
}
