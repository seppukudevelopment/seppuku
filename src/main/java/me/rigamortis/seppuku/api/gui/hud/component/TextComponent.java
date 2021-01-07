package me.rigamortis.seppuku.api.gui.hud.component;

import me.rigamortis.seppuku.api.texture.Texture;
import me.rigamortis.seppuku.api.util.RenderUtil;
import me.rigamortis.seppuku.api.util.Timer;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

/**
 * @author noil
 */
public class TextComponent extends HudComponent {

    public String displayValue;
    public boolean focused;
    public boolean digitOnly;
    public ComponentListener returnListener;
    public TextComponentListener textListener;

    protected Texture checkTexture;

    protected Timer backspaceTimer = new Timer(), backspaceWaitTimer = new Timer();
    protected boolean doBackspacing = false;

    public TextComponent(String name, String displayValue, boolean digitOnly) {
        super(name);

        this.displayValue = displayValue;
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
        Minecraft.getMinecraft().fontRenderer.drawString(this.getName() + ": " + this.displayValue, (int) this.getX() + 1, (int) this.getY() + 1, this.focused ? 0xFFFFFFFF : 0xFFAAAAAA);

        if (this.focused) {
            float blockX = this.getX() + Minecraft.getMinecraft().fontRenderer.getStringWidth(this.getName() + ": " + this.displayValue) + 1;
            float blockY = this.getY() + 1;
            int blockWidth = 2;
            int blockHeight = Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT - 2;
            RenderUtil.drawRect(blockX, blockY, blockX + blockWidth, blockY + blockHeight, 0xFFFFFFFF);

            // check
            RenderUtil.drawRect(this.getX() + this.getW() - 10, this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), 0xFF101010);
            this.checkTexture.bind();
            this.checkTexture.render(this.getX() + this.getW() - 9, this.getY() + 0.5f, 8, 8);

            if (Keyboard.isKeyDown(Keyboard.KEY_BACK) || Keyboard.isKeyDown(Keyboard.KEY_DELETE)) {
                if (this.doBackspacing && this.backspaceWaitTimer.passed(750)) {
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
                        this.displayValue = this.displayValue.substring(0, this.displayValue.length() - 1);
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

            //if (!digitOnly && !Character.isLetterOrDigit(typedChar))
            //    return;

            this.displayValue += typedChar;
        }
    }

    protected void enterPressed() {
        // invoke return listener
        if (returnListener != null)
            returnListener.onComponentEvent();

        this.focused = false;
    }

    protected boolean onCheckButtonPress(int mouseX, int mouseY) {
        if (mouseX >= this.getX() + this.getW() - 10 && mouseX <= this.getX() + this.getW() && mouseY >= this.getY() && mouseY <= this.getY() + this.getH()) {
            this.enterPressed();
            return true;
        }
        return false;
    }

    public interface TextComponentListener {
        void onKeyTyped(int keyCode);
    }

    public void focus() {
        this.focused = true;
    }
}
