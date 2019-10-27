package me.rigamortis.seppuku.impl.gui.menu;

import me.rigamortis.seppuku.api.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;

/**
 * Author Seth
 * 9/7/2019 @ 2:14 PM.
 */
public abstract class MainMenuButton {

    private float x;
    private float y;
    private float w;
    private float h;

    private String text;

    private boolean clicked;

    public MainMenuButton(float x, float y, String text) {
        this.x = x;
        this.y = y;
        this.text = text;
        this.w = 140;
        this.h = 18;
    }

    public void render(int x, int y, float partialTicks) {
        if(this.clicked) {
            RenderUtil.drawRect(this.x, this.y, this.x + this.w, this.y + this.h, 0x66111111);
            RenderUtil.drawGradientRect(this.x + 1, this.y + 1, this.x + this.w - 1, this.y + this.h - 1, 0xAA232323, 0xAA303030);
            Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(this.text, this.x + (this.w / 2) - (Minecraft.getMinecraft().fontRenderer.getStringWidth(this.text) / 2), this.y + (this.h / 2) - (Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT / 2), 0xFF9900EE);
        }else{
            if(this.inside(x, y)) {
                RenderUtil.drawRect(this.x, this.y, this.x + this.w, this.y + this.h, 0x66111111);
                RenderUtil.drawGradientRect(this.x + 1, this.y + 1, this.x + this.w - 1, this.y + this.h - 1, 0xAA303030, 0xAA232323);
                Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(this.text, this.x + (this.w / 2) - (Minecraft.getMinecraft().fontRenderer.getStringWidth(this.text) / 2), this.y + (this.h / 2) - (Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT / 2), -1);
            }else{
                RenderUtil.drawRect(this.x, this.y, this.x + this.w, this.y + this.h, 0x66111111);
                RenderUtil.drawGradientRect(this.x + 1, this.y + 1, this.x + this.w - 1, this.y + this.h - 1, 0xAA303030, 0xAA232323);
                Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(this.text, this.x + (this.w / 2) - (Minecraft.getMinecraft().fontRenderer.getStringWidth(this.text) / 2), this.y + (this.h / 2) - (Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT / 2), 0xFFAAAAAA);
            }
        }
    }

    public void mouseRelease(int x, int y, int button) {
        if(inside(x, y) && this.clicked && button == 0) {
            this.action();
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
        }

        if(button == 0) {
            this.clicked = false;
        }
    }

    public void mouseClicked(int x, int y, int button) {
        if(inside(x, y) && button == 0) {
            this.clicked = true;
        }
    }

    public abstract void action();

    private boolean inside(int x, int y) {
        return x >= this.getX() && x <= this.getX() + this.getW() && y >= this.getY() && y <= this.getY() + this.getH();
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
