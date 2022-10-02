package me.rigamortis.seppuku.api.gui.hud.component;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.texture.Texture;
import me.rigamortis.seppuku.api.util.RenderUtil;
import me.rigamortis.seppuku.api.util.StringUtil;
import me.rigamortis.seppuku.impl.gui.hud.component.ColorsComponent;
import me.rigamortis.seppuku.impl.gui.hud.component.module.ModuleListComponent;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.util.logging.Level;

/**
 * @author noil
 */
public class TextComponent extends HudComponent {

    // space occupied from left to right: border, text, spacing, check, border
    protected static final int BORDER = 1;
    protected static final int SPACING = 1;
    protected static final int SHIFT_GAP = 8;
    protected static final int CHECK_WIDTH = 8;
    protected static final int BLOCK_WIDTH = 2;
    protected static final float ICON_V_OFFSET = 0.5f;
    public boolean focused;
    public boolean digitOnly;
    public ComponentListener returnListener;
    public TextComponentListener textListener;
    protected Texture checkTexture;
    private String displayValue;
    private int textCursor = 0;
    private int textCursorOffset = 0;
    private int selectCursor = 0;
    private int selectCursorOffset = 0;
    private int shiftLength = 0;
    private boolean dirty = false;

    public TextComponent(String name, String displayValue, boolean digitOnly) {
        super(name);

        this.displayValue = displayValue;
        this.focused = false;
        this.digitOnly = digitOnly;

        this.checkTexture = new Texture("check.png");
    }

    protected void renderReserved(int mouseX, int mouseY, float partialTicks, String renderName, boolean renderValue, float reservedLeft, float reservedRight) {
        // calculate dimensions that can be used given reserved left/right
        final float left = this.getX() + reservedLeft;
        final float right = this.getX() + this.getW() - reservedRight;

        // draw gradient if component has mouse hovering
        if (this.isMouseInside(mouseX, mouseY))
            RenderUtil.drawGradientRect(this.getX(), this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), 0x30909090, 0x00101010);

        // draw background
        RenderUtil.drawRect(this.getX(), this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), 0x45303030);

        // update text shift and cursor offsets if needed
        String displayValueText = renderName;
        if (renderValue) {
            displayValueText += this.displayValue;
        }

        if (this.focused && renderValue) {
            if (this.dirty) {
                this.dirty = false;
                final String beforeTextCursor = displayValueText.substring(0, renderName.length() + this.textCursor);
                this.textCursorOffset = Minecraft.getMinecraft().fontRenderer.getStringWidth(beforeTextCursor);

                if (this.selectCursor == this.textCursor) {
                    this.selectCursorOffset = this.textCursorOffset;
                } else {
                    final String beforeSelectCursor = displayValueText.substring(0, renderName.length() + this.selectCursor);
                    this.selectCursorOffset = Minecraft.getMinecraft().fontRenderer.getStringWidth(beforeSelectCursor);
                }

                // shift gap is the minimum amount of space to leave after the
                // text cursor (block) so the user can see that there is more
                // text to the right
                final int shiftStart = Math.round(right - left) - CHECK_WIDTH - SPACING - BLOCK_WIDTH - BORDER * 2 - SHIFT_GAP;
                if (this.textCursorOffset > shiftStart) {
                    this.shiftLength = this.textCursorOffset - shiftStart;
                } else {
                    this.shiftLength = 0;
                }
            }
        } else {
            this.shiftLength = 0;
        }

        // draw text
        Minecraft.getMinecraft().fontRenderer.drawString(displayValueText, (int) left + BORDER - this.shiftLength, (int) this.getY() + BORDER, this.focused ? 0xFFFFFFFF : 0xFFAAAAAA);

        if (this.focused && renderValue) {
            // draw text selection background
            if (this.textCursor != this.selectCursor) {
                final int start = Math.min(this.textCursorOffset, this.selectCursorOffset);
                final int end = Math.max(this.textCursorOffset, this.selectCursorOffset);
                RenderUtil.drawRect(left + start - this.shiftLength, this.getY(), left + end - this.shiftLength, this.getY() + this.getH(), 0x45FFFFFF);
            }

            // draw text cursor (block)
            float blockX = left + BORDER + this.textCursorOffset - this.shiftLength;
            float blockY = this.getY() + BORDER;
            final int blockHeight = Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT - BORDER * 2;
            RenderUtil.drawRect(blockX, blockY, blockX + BLOCK_WIDTH, blockY + blockHeight, 0xFFFFFFFF);

            // draw checkbox
            RenderUtil.drawRect(right - CHECK_WIDTH - BORDER - SPACING, this.getY(), right, this.getY() + this.getH(), 0xFF101010);
            this.checkTexture.bind();
            this.checkTexture.render(right - CHECK_WIDTH - BORDER, this.getY() + ICON_V_OFFSET, CHECK_WIDTH, CHECK_WIDTH);
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        String renderName = this.getName();
        if (this.getDisplayName() != null) {
            renderName = this.getDisplayName();
        }

        this.renderReserved(mouseX, mouseY, partialTicks, renderName + ": ", true, 0, 0);
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

            final boolean ctrlDown = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
            if (ctrlDown) {
                switch (keyCode) {
                    case Keyboard.KEY_A:
                        this.selectAll();
                        return;
                    case Keyboard.KEY_V:
                        this.insertText(this.getClipBoard());
                        return;
                    case Keyboard.KEY_X:
                        if (this.setClipBoard(this.getSelection())) {
                            this.onRemoveSelectedText();
                        }
                        return;
                    case Keyboard.KEY_C:
                        this.setClipBoard(this.getSelection());
                        return;
                }

                return; // dont do anything else or you will get special characters typed
            }

            final boolean shiftDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
            switch (keyCode) {
                case Keyboard.KEY_ESCAPE:
                    this.focused = false;
                    return;
                case Keyboard.KEY_RETURN:
                    this.enterPressed();
                    return;
                case Keyboard.KEY_BACK:
                case Keyboard.KEY_DELETE:
                    final int delta = (keyCode == Keyboard.KEY_DELETE) ? 1 : -1;
                    this.deleteText(delta);
                    return;
                case Keyboard.KEY_CLEAR:
                    this.setText("");
                    return;
                case Keyboard.KEY_LEFT:
                    this.setTextCursor(this.textCursor - 1, shiftDown);
                    return;
                case Keyboard.KEY_RIGHT:
                    this.setTextCursor(this.textCursor + 1, shiftDown);
                    return;
                case Keyboard.KEY_UP:
                case Keyboard.KEY_HOME:
                    this.setTextCursor(0, shiftDown);
                    return;
                case Keyboard.KEY_DOWN:
                case Keyboard.KEY_END:
                    this.setTextCursor(this.displayValue.length(), shiftDown);
                    return;
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
                default:
                    break;
            }

            this.insertText(typedChar);
        }
    }

    protected void enterPressed() {
        // invoke return listener
        if (returnListener != null)
            returnListener.onComponentEvent();

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
        if (this.textCursor == this.selectCursor) {
            return false;
        }

        this.deleteText(this.textCursor, this.selectCursor);
        return true;
    }

    @Override
    public void setW(float w) {
        if (this.getW() != w) {
            this.dirty = true;
        }
        super.setW(w);
    }

    public String getClipBoard() {
        try {
            return (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
        } catch (Exception e) {
            Seppuku.INSTANCE.getLogger().log(Level.WARNING, "Error getting clipboard while using " + this.getName());
        }
        return "";
    }

    public boolean setClipBoard(String s) {
        try {
            final StringSelection sel = new StringSelection(s);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
        } catch (Exception e) {
            Seppuku.INSTANCE.getLogger().log(Level.WARNING, "Error setting clipboard while using " + this.getName());
            return false;
        }

        return true;
    }

    public String getSelection() {
        if (!this.focused || this.textCursor == this.selectCursor) {
            // textCursor or selectCursor may be invalid if not focused
            return "";
        }

        if (this.textCursor > this.selectCursor) {
            return this.displayValue.substring(this.selectCursor, this.textCursor);
        } else {
            return this.displayValue.substring(this.textCursor, this.selectCursor);
        }
    }

    public void focus() {
        this.textCursor = this.selectCursor = this.displayValue.length();
        this.focused = true;
        this.dirty = true;
    }

    public void setTextCursor(int pos, boolean shiftDown) {
        if (pos <= 0) {
            pos = 0;
        } else if (pos > this.displayValue.length()) {
            pos = this.displayValue.length();
        }

        final int selectPos = shiftDown ? this.selectCursor : pos;
        if (this.textCursor != pos || this.selectCursor != selectPos) {
            this.textCursor = pos;
            this.selectCursor = selectPos;
            this.dirty = true;
        }
    }

    public void insertText(char character) {
        this.insertText(String.valueOf(character));
    }

    public void insertText(String str) {
        if (this.digitOnly && !str.matches("[0-9.]+") /* is a number */) {
            return;
        }

        this.onRemoveSelectedText();

        this.displayValue = StringUtil.insertAt(this.displayValue, str, this.textCursor);
        this.setTextCursor(this.textCursor + str.length(), false);
    }

    public void deleteText(int start, int end) {
        // sanitise range
        start = Math.min(Math.max(start, 0), this.displayValue.length());
        end = Math.min(Math.max(end, 0), this.displayValue.length());
        if (start == end) {
            return;
        } else if (start > end) {
            final int temp = start;
            start = end;
            end = temp;
        }

        this.displayValue = StringUtil.removeRange(this.displayValue, start, end);
        this.setTextCursor(start, false);
    }

    public void deleteText(int delta) {
        if (delta == 0) {
            return;
        }

        if (!this.onRemoveSelectedText()) {
            this.deleteText(this.textCursor, this.textCursor + delta);
        }
    }

    public void selectAll() {
        this.textCursor = this.displayValue.length();
        this.selectCursor = 0;
        this.dirty = true;
    }

    public String getText() {
        return this.displayValue;
    }

    public void setText(String text) {
        this.displayValue = text;
        this.selectCursor = this.textCursor = text.length();
        if (this.focused) {
            this.dirty = true;
        }
    }

    public interface TextComponentListener {
        void onKeyTyped(int keyCode);
    }
}
