package me.rigamortis.seppuku.impl.gui.hud.component;

import me.rigamortis.seppuku.api.gui.hud.component.DraggableHudComponent;
import me.rigamortis.seppuku.api.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

/**
 * created by noil on 10/19/2019 at 3:58 PM
 */
public final class InventoryComponent extends DraggableHudComponent {

    public InventoryComponent() {
        super("Inventory");
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        final Minecraft mc = Minecraft.getMinecraft();// store the minecraft instance in a local variable

        if (mc.player == null)
            return;

        this.setW(16 * 9);
        this.setH(16 * 3);

        GlStateManager.pushMatrix();
        RenderHelper.enableGUIStandardItemLighting();
        RenderUtil.drawRect(this.getX(), this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), 0x75101010); // background
        for (int i = 0; i < 27; i++) {
            ItemStack itemStack = mc.player.inventory.mainInventory.get(i + 9);
            int offsetX = (int) this.getX() + (i % 9) * 16;
            int offsetY = (int) this.getY() + (i / 9) * 16;
            mc.getRenderItem().renderItemAndEffectIntoGUI(itemStack, offsetX, offsetY);
            mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRenderer, itemStack, offsetX, offsetY, null);
        }
        RenderHelper.disableStandardItemLighting();
        mc.getRenderItem().zLevel = 0.0F;
        GlStateManager.popMatrix();
    }
}
