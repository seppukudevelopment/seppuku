package me.rigamortis.seppuku.impl.gui.hud.component;

import me.rigamortis.seppuku.api.gui.hud.component.DraggableHudComponent;

/**
 * Author Seth
 * 7/28/2019 @ 9:43 AM.
 */
public final class ServerBrandComponent extends DraggableHudComponent {

    public ServerBrandComponent() {
        super("ServerBrand");
        this.setH(mc.fontRenderer.FONT_HEIGHT);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        if (mc.getCurrentServerData() != null) {
            final String brand = mc.getCurrentServerData() == null ? "Vanilla" : mc.getCurrentServerData().gameVersion;

            this.setW(mc.fontRenderer.getStringWidth(brand));
            mc.fontRenderer.drawStringWithShadow(brand, this.getX(), this.getY(), -1);
        } else {
            this.setW(mc.fontRenderer.getStringWidth("(server brand)"));
            mc.fontRenderer.drawStringWithShadow("(server brand)", this.getX(), this.getY(), 0xFFAAAAAA);
        }
    }

}