package me.rigamortis.seppuku.impl.gui.hud.component;

import me.rigamortis.seppuku.api.gui.hud.component.DraggableHudComponent;
import net.minecraft.client.Minecraft;

/**
 * Author Seth
 * 7/28/2019 @ 9:43 AM.
 */
public final class ServerBrandComponent extends DraggableHudComponent {

    public ServerBrandComponent() {
        super("ServerBrand");
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);
        final String brand = Minecraft.getMinecraft().getCurrentServerData() == null ? "Vanilla" : Minecraft.getMinecraft().getCurrentServerData().gameVersion;

        this.setW(Minecraft.getMinecraft().fontRenderer.getStringWidth(brand));
        this.setH(Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT);

        //RenderUtil.drawRect(this.getX(), this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), 0x90222222);
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(brand, this.getX(), this.getY(), -1);
    }

}