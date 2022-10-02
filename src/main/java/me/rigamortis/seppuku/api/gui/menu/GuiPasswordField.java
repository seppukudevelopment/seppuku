package me.rigamortis.seppuku.api.gui.menu;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiPageButtonList.GuiResponder;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.LogicOp;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.math.MathHelper;

/**
 * @author Mojang
 * @author noil
 */
public class GuiPasswordField extends Gui {

    private final int id;
    private final FontRenderer fontRenderer;
    public int x;
    public int y;
    public int width;
    public int height;
    private String text = "";
    private int maxStringLength = 32;
    private int cursorCounter;
    private boolean enableBackgroundDrawing = true;
    private boolean canLoseFocus = true;
    private boolean isFocused;
    private boolean isEnabled = true;
    private int lineScrollOffset;
    private int cursorPosition;
    private int selectionEnd;
    private int enabledColor = 14737632;
    private int disabledColor = 7368816;
    private boolean visible = true;
    private GuiResponder guiResponder;
    private Predicate<String> validator = Predicates.alwaysTrue();

    public GuiPasswordField(int componentId, FontRenderer fontrendererObj, int x, int y, int par5Width, int par6Height) {
        this.id = componentId;
        this.fontRenderer = fontrendererObj;
        this.x = x;
        this.y = y;
        this.width = par5Width;
        this.height = par6Height;
    }

    public void setGuiResponder(GuiResponder guiResponderIn) {
        this.guiResponder = guiResponderIn;
    }

    public void updateCursorCounter() {
        ++this.cursorCounter;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String textIn) {
        if (this.validator.apply(textIn)) {
            if (textIn.length() > this.maxStringLength) {
                this.text = textIn.substring(0, this.maxStringLength);
            } else {
                this.text = textIn;
            }

            this.setCursorPositionEnd();
        }

    }

    public String getSelectedText() {
        int i = Math.min(this.cursorPosition, this.selectionEnd);
        int j = Math.max(this.cursorPosition, this.selectionEnd);
        return this.text.substring(i, j);
    }

    public void setValidator(Predicate<String> theValidator) {
        this.validator = theValidator;
    }

    public void writeText(String textToWrite) {
        String s = "";
        String s1 = ChatAllowedCharacters.filterAllowedCharacters(textToWrite);
        int i = Math.min(this.cursorPosition, this.selectionEnd);
        int j = Math.max(this.cursorPosition, this.selectionEnd);
        int k = this.maxStringLength - this.text.length() - (i - j);
        if (!this.text.isEmpty()) {
            s = s + this.text.substring(0, i);
        }

        int l;
        if (k < s1.length()) {
            s = s + s1.substring(0, k);
            l = k;
        } else {
            s = s + s1;
            l = s1.length();
        }

        if (!this.text.isEmpty() && j < this.text.length()) {
            s = s + this.text.substring(j);
        }

        if (this.validator.apply(s)) {
            this.text = s;
            this.moveCursorBy(i - this.selectionEnd + l);
            this.setResponderEntryValue(this.id, this.text);
        }

    }

    public void setResponderEntryValue(int idIn, String textIn) {
        if (this.guiResponder != null) {
            this.guiResponder.setEntryValue(idIn, textIn);
        }

    }

    public void deleteWords(int num) {
        if (!this.text.isEmpty()) {
            if (this.selectionEnd != this.cursorPosition) {
                this.writeText("");
            } else {
                this.deleteFromCursor(this.getNthWordFromCursor(num) - this.cursorPosition);
            }
        }

    }

    public void deleteFromCursor(int num) {
        if (!this.text.isEmpty()) {
            if (this.selectionEnd != this.cursorPosition) {
                this.writeText("");
            } else {
                boolean flag = num < 0;
                int i = flag ? this.cursorPosition + num : this.cursorPosition;
                int j = flag ? this.cursorPosition : this.cursorPosition + num;
                String s = "";
                if (i >= 0) {
                    s = this.text.substring(0, i);
                }

                if (j < this.text.length()) {
                    s = s + this.text.substring(j);
                }

                if (this.validator.apply(s)) {
                    this.text = s;
                    if (flag) {
                        this.moveCursorBy(num);
                    }

                    this.setResponderEntryValue(this.id, this.text);
                }
            }
        }

    }

    public int getId() {
        return this.id;
    }

    public int getNthWordFromCursor(int numWords) {
        return this.getNthWordFromPos(numWords, this.getCursorPosition());
    }

    public int getNthWordFromPos(int n, int pos) {
        return this.getNthWordFromPosWS(n, pos, true);
    }

    public int getNthWordFromPosWS(int n, int pos, boolean skipWs) {
        int i = pos;
        boolean flag = n < 0;
        int j = Math.abs(n);

        for (int k = 0; k < j; ++k) {
            if (!flag) {
                int l = this.text.length();
                i = this.text.indexOf(32, i);
                if (i == -1) {
                    i = l;
                } else {
                    while (skipWs && i < l && this.text.charAt(i) == ' ') {
                        ++i;
                    }
                }
            } else {
                while (skipWs && i > 0 && this.text.charAt(i - 1) == ' ') {
                    --i;
                }

                while (i > 0 && this.text.charAt(i - 1) != ' ') {
                    --i;
                }
            }
        }

        return i;
    }

    public void moveCursorBy(int num) {
        this.setCursorPosition(this.selectionEnd + num);
    }

    public void setCursorPositionZero() {
        this.setCursorPosition(0);
    }

    public void setCursorPositionEnd() {
        this.setCursorPosition(this.text.length());
    }

    public boolean textboxKeyTyped(char typedChar, int keyCode) {
        if (!this.isFocused) {
            return false;
        } else if (GuiScreen.isKeyComboCtrlA(keyCode)) {
            this.setCursorPositionEnd();
            this.setSelectionPos(0);
            return true;
        } else if (GuiScreen.isKeyComboCtrlC(keyCode)) {
            GuiScreen.setClipboardString(this.getSelectedText());
            return true;
        } else if (GuiScreen.isKeyComboCtrlV(keyCode)) {
            if (this.isEnabled) {
                this.writeText(GuiScreen.getClipboardString());
            }

            return true;
        } else if (GuiScreen.isKeyComboCtrlX(keyCode)) {
            GuiScreen.setClipboardString(this.getSelectedText());
            if (this.isEnabled) {
                this.writeText("");
            }

            return true;
        } else {
            switch (keyCode) {
                case 14:
                    if (GuiScreen.isCtrlKeyDown()) {
                        if (this.isEnabled) {
                            this.deleteWords(-1);
                        }
                    } else if (this.isEnabled) {
                        this.deleteFromCursor(-1);
                    }

                    return true;
                case 199:
                    if (GuiScreen.isShiftKeyDown()) {
                        this.setSelectionPos(0);
                    } else {
                        this.setCursorPositionZero();
                    }

                    return true;
                case 203:
                    if (GuiScreen.isShiftKeyDown()) {
                        if (GuiScreen.isCtrlKeyDown()) {
                            this.setSelectionPos(this.getNthWordFromPos(-1, this.getSelectionEnd()));
                        } else {
                            this.setSelectionPos(this.getSelectionEnd() - 1);
                        }
                    } else if (GuiScreen.isCtrlKeyDown()) {
                        this.setCursorPosition(this.getNthWordFromCursor(-1));
                    } else {
                        this.moveCursorBy(-1);
                    }

                    return true;
                case 205:
                    if (GuiScreen.isShiftKeyDown()) {
                        if (GuiScreen.isCtrlKeyDown()) {
                            this.setSelectionPos(this.getNthWordFromPos(1, this.getSelectionEnd()));
                        } else {
                            this.setSelectionPos(this.getSelectionEnd() + 1);
                        }
                    } else if (GuiScreen.isCtrlKeyDown()) {
                        this.setCursorPosition(this.getNthWordFromCursor(1));
                    } else {
                        this.moveCursorBy(1);
                    }

                    return true;
                case 207:
                    if (GuiScreen.isShiftKeyDown()) {
                        this.setSelectionPos(this.text.length());
                    } else {
                        this.setCursorPositionEnd();
                    }

                    return true;
                case 211:
                    if (GuiScreen.isCtrlKeyDown()) {
                        if (this.isEnabled) {
                            this.deleteWords(1);
                        }
                    } else if (this.isEnabled) {
                        this.deleteFromCursor(1);
                    }

                    return true;
                default:
                    if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
                        if (this.isEnabled) {
                            this.writeText(Character.toString(typedChar));
                        }

                        return true;
                    } else {
                        return false;
                    }
            }
        }
    }

    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        boolean flag = mouseX >= this.x && mouseX < this.x + this.width && mouseY >= this.y && mouseY < this.y + this.height;
        if (this.canLoseFocus) {
            this.setFocused(flag);
        }

        if (this.isFocused && flag && mouseButton == 0) {
            int i = mouseX - this.x;
            if (this.enableBackgroundDrawing) {
                i -= 4;
            }

            String s = this.fontRenderer.trimStringToWidth(this.text.substring(this.lineScrollOffset), this.getWidth());
            this.setCursorPosition(this.fontRenderer.trimStringToWidth(s, i).length() + this.lineScrollOffset);
            return true;
        } else {
            return false;
        }
    }

    public void drawTextBox() {
        if (this.getVisible()) {
            if (this.getEnableBackgroundDrawing()) {
                drawRect(this.x - 1, this.y - 1, this.x + this.width + 1, this.y + this.height + 1, -6250336);
                drawRect(this.x, this.y, this.x + this.width, this.y + this.height, -16777216);
            }

            int i = this.isEnabled ? this.enabledColor : this.disabledColor;
            int j = this.cursorPosition - this.lineScrollOffset;
            int k = this.selectionEnd - this.lineScrollOffset;
            String s = this.fontRenderer.trimStringToWidth(this.text.substring(this.lineScrollOffset), this.getWidth());
            boolean flag = j >= 0 && j <= s.length();
            boolean flag1 = this.isFocused && this.cursorCounter / 6 % 2 == 0 && flag;
            int l = this.enableBackgroundDrawing ? this.x + 4 : this.x;
            int i1 = this.enableBackgroundDrawing ? this.y + (this.height - 8) / 2 : this.y;
            int j1 = l;
            if (k > s.length()) {
                k = s.length();
            }

            if (!s.isEmpty()) {
                String s1 = flag ? s.substring(0, j) : s;
                j1 = this.fontRenderer.drawStringWithShadow(s1.replaceAll("(?s).", "*"), (float) l, (float) i1, i);
            }

            boolean flag2 = this.cursorPosition < this.text.length() || this.text.length() >= this.getMaxStringLength();
            int k1 = j1;
            if (!flag) {
                k1 = j > 0 ? l + this.width : l;
            } else if (flag2) {
                k1 = j1 - 1;
                --j1;
            }

            if (!s.isEmpty() && flag && j < s.length()) {
                j1 = this.fontRenderer.drawStringWithShadow(s.substring(j), (float) j1, (float) i1, i);
            }

            if (flag1) {
                if (flag2) {
                    Gui.drawRect(k1, i1 - 1, k1 + 1, i1 + 1 + this.fontRenderer.FONT_HEIGHT, -3092272);
                } else {
                    this.fontRenderer.drawStringWithShadow("_", (float) k1, (float) i1, i);
                }
            }

            if (k != j) {
                int l1 = l + this.fontRenderer.getStringWidth(s.substring(0, k));
                this.drawSelectionBox(k1, i1 - 1, l1 - 1, i1 + 1 + this.fontRenderer.FONT_HEIGHT);
            }
        }

    }

    private void drawSelectionBox(int startX, int startY, int endX, int endY) {
        int j;
        if (startX < endX) {
            j = startX;
            startX = endX;
            endX = j;
        }

        if (startY < endY) {
            j = startY;
            startY = endY;
            endY = j;
        }

        if (endX > this.x + this.width) {
            endX = this.x + this.width;
        }

        if (startX > this.x + this.width) {
            startX = this.x + this.width;
        }

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.color(0.0F, 0.0F, 255.0F, 255.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.enableColorLogic();
        GlStateManager.colorLogicOp(LogicOp.OR_REVERSE);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.pos(startX, endY, 0.0D).endVertex();
        bufferbuilder.pos(endX, endY, 0.0D).endVertex();
        bufferbuilder.pos(endX, startY, 0.0D).endVertex();
        bufferbuilder.pos(startX, startY, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.disableColorLogic();
        GlStateManager.enableTexture2D();
    }

    public int getMaxStringLength() {
        return this.maxStringLength;
    }

    public void setMaxStringLength(int length) {
        this.maxStringLength = length;
        if (this.text.length() > length) {
            this.text = this.text.substring(0, length);
        }

    }

    public int getCursorPosition() {
        return this.cursorPosition;
    }

    public void setCursorPosition(int pos) {
        this.cursorPosition = pos;
        int i = this.text.length();
        this.cursorPosition = MathHelper.clamp(this.cursorPosition, 0, i);
        this.setSelectionPos(this.cursorPosition);
    }

    public boolean getEnableBackgroundDrawing() {
        return this.enableBackgroundDrawing;
    }

    public void setEnableBackgroundDrawing(boolean enableBackgroundDrawingIn) {
        this.enableBackgroundDrawing = enableBackgroundDrawingIn;
    }

    public void setTextColor(int color) {
        this.enabledColor = color;
    }

    public void setDisabledTextColour(int color) {
        this.disabledColor = color;
    }

    public boolean isFocused() {
        return this.isFocused;
    }

    public void setFocused(boolean isFocusedIn) {
        if (isFocusedIn && !this.isFocused) {
            this.cursorCounter = 0;
        }

        this.isFocused = isFocusedIn;
        if (Minecraft.getMinecraft().currentScreen != null) {
            Minecraft.getMinecraft().currentScreen.setFocused(isFocusedIn);
        }

    }

    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }

    public int getSelectionEnd() {
        return this.selectionEnd;
    }

    public int getWidth() {
        return this.getEnableBackgroundDrawing() ? this.width - 8 : this.width;
    }

    public void setSelectionPos(int position) {
        int i = this.text.length();
        if (position > i) {
            position = i;
        }

        if (position < 0) {
            position = 0;
        }

        this.selectionEnd = position;
        if (this.fontRenderer != null) {
            if (this.lineScrollOffset > i) {
                this.lineScrollOffset = i;
            }

            int j = this.getWidth();
            String s = this.fontRenderer.trimStringToWidth(this.text.substring(this.lineScrollOffset), j);
            int k = s.length() + this.lineScrollOffset;
            if (position == this.lineScrollOffset) {
                this.lineScrollOffset -= this.fontRenderer.trimStringToWidth(this.text, j, true).length();
            }

            if (position > k) {
                this.lineScrollOffset += position - k;
            } else if (position <= this.lineScrollOffset) {
                this.lineScrollOffset -= this.lineScrollOffset - position;
            }

            this.lineScrollOffset = MathHelper.clamp(this.lineScrollOffset, 0, i);
        }

    }

    public void setCanLoseFocus(boolean canLoseFocusIn) {
        this.canLoseFocus = canLoseFocusIn;
    }

    public boolean getVisible() {
        return this.visible;
    }

    public void setVisible(boolean isVisible) {
        this.visible = isVisible;
    }
}

