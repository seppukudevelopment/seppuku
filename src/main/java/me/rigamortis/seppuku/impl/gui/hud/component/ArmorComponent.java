package me.rigamortis.seppuku.impl.gui.hud.component;

import me.rigamortis.seppuku.api.gui.hud.component.DraggableHudComponent;
import me.rigamortis.seppuku.impl.gui.hud.GuiHudEditor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

/**
 * Author Seth
 * 7/28/2019 @ 9:23 AM.
 */
public final class ArmorComponent extends DraggableHudComponent {

    private static final int ITEM_SIZE = 18;

    public ArmorComponent() {
        super("Armor");
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        final Minecraft mc = Minecraft.getMinecraft();
        boolean isInHudEditor = mc.currentScreen instanceof GuiHudEditor;
        int itemSpacingWidth = 0;
        boolean playerHasArmor = false;

        for (int i = 0; i <= 3; i++) {
            final ItemStack stack = mc.player.inventoryContainer.getSlot(8 - i).getStack();
            if (!stack.isEmpty()) {
                GlStateManager.pushMatrix();
                RenderHelper.enableGUIStandardItemLighting();
                mc.getRenderItem().renderItemAndEffectIntoGUI(stack, (int)this.getX() + itemSpacingWidth, (int)this.getY());
                mc.getRenderItem().renderItemOverlays(mc.fontRenderer, stack, (int)this.getX() + itemSpacingWidth, (int)this.getY());
                RenderHelper.disableStandardItemLighting();
                GlStateManager.popMatrix();
                itemSpacingWidth += ITEM_SIZE;
                playerHasArmor = true;
            }
        }

        if (isInHudEditor) {
            if (!playerHasArmor) {
                mc.fontRenderer.drawString("(armor)", (int) this.getX(), (int) this.getY(), 0xFFAAAAAA);
            }

            itemSpacingWidth = ITEM_SIZE * 4; // simulate 4 slots of armor ( for a placeholder in hud editor )
        }

        this.setW(itemSpacingWidth);
        this.setH(16);
    }

}
