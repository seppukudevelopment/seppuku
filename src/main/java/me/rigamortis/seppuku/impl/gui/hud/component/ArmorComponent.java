package me.rigamortis.seppuku.impl.gui.hud.component;

import me.rigamortis.seppuku.api.gui.hud.component.DraggableHudComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

/**
 * Author Seth
 * 7/28/2019 @ 9:23 AM.
 */
public final class ArmorComponent extends DraggableHudComponent {

    public ArmorComponent() {
        super("Armor");
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);
       // RenderUtil.drawRect(this.getX(), this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), 0x90222222);
        final Minecraft mc = Minecraft.getMinecraft();

        int space = 0;

        for (int i = 0; i <= 3; i++) {
            final ItemStack stack = mc.player.inventoryContainer.getSlot(8 - i).getStack();
            if (stack != ItemStack.EMPTY) {
                GlStateManager.pushMatrix();
                RenderHelper.enableGUIStandardItemLighting();
                mc.getRenderItem().renderItemAndEffectIntoGUI(stack, (int)this.getX() + space, (int)this.getY());
                mc.getRenderItem().renderItemOverlays(mc.fontRenderer, stack, (int)this.getX() + space, (int)this.getY());
                RenderHelper.disableStandardItemLighting();
                GlStateManager.popMatrix();
                space += 18;
            }
        }

        this.setW(space);
        this.setH(16);
    }

}
