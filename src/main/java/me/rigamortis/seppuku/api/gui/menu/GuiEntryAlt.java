package me.rigamortis.seppuku.api.gui.menu;

import me.rigamortis.seppuku.api.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ImageBufferDownload;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;

/**
 * @author noil
 */
public class GuiEntryAlt implements GuiListExtended.IGuiListEntry {

    private GuiListAlt parent;
    private ResourceLocation resourceLocation;
    private final AltData alt;

    public GuiEntryAlt(GuiListAlt parent, AltData alt) {
        this.parent = parent;
        this.alt = alt;
    }

    @Override
    public void updatePosition(int i, int i1, int i2, float v) {

    }

    @Override
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean mouseOver, float v) {
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(this.alt.getUsername(), x + 22, y + 2, 0xFFFFFFFF);

        if (this.alt.isPremium()) {
            String password = this.alt.getPassword().replaceAll("(?s).", "*");
            Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(password, x + 22, y + 2 + Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT, 0xFFAAAAAA);
        }

        // draw rect behind head
        RenderUtil.drawRect(x, y, x + 20, y + 20, 0xFFAAAAAA);

        // load resource location & download skin from minotar's public api
        if (this.resourceLocation == null) {
            this.resourceLocation = AbstractClientPlayer.getLocationSkin(this.alt.getUsername());
            this.getDownloadImageSkin(this.resourceLocation, this.alt.getUsername());
        } else { // render player head
            Minecraft.getMinecraft().getTextureManager().bindTexture(this.resourceLocation);
            GlStateManager.enableTexture2D();
            RenderUtil.drawTexture(x, y, 20, 20, 0, 0, 1, 1);
        }
    }

    @Override
    public boolean mousePressed(int slotIndex, int p_148278_2_, int p_148278_3_, int p_148278_4_, int p_148278_5_, int p_148278_6_) {
        this.parent.setSelected(slotIndex);
        return false;
    }

    @Override
    public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {
    }

    private ThreadDownloadImageData getDownloadImageSkin(ResourceLocation resourceLocationIn, String username) {
        TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
        textureManager.getTexture(resourceLocationIn);
        ThreadDownloadImageData textureObject = new ThreadDownloadImageData(null, String.format("https://minotar.net/avatar/%s/64.png", StringUtils.stripControlCodes(username)), DefaultPlayerSkin.getDefaultSkin(AbstractClientPlayer.getOfflineUUID(username)), new ImageBufferDownload());
        textureManager.loadTexture(resourceLocationIn, textureObject);
        return textureObject;
    }

    public AltData getAlt() {
        return this.alt;
    }
}
