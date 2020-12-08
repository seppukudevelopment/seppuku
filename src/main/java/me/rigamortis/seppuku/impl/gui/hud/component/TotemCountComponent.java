package me.rigamortis.seppuku.impl.gui.hud.component;

import me.rigamortis.seppuku.api.gui.hud.component.DraggableHudComponent;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

/**
 * Author: fsck
 * 2019-10-21
 */
public final class TotemCountComponent extends DraggableHudComponent {

    public TotemCountComponent() {
        super("TotemCount");
        this.setH(mc.fontRenderer.FONT_HEIGHT);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        if (mc.player != null) {
            final String totemCount = "Totems: " + this.getTotemCount();
            this.setW(mc.fontRenderer.getStringWidth(totemCount));
            mc.fontRenderer.drawStringWithShadow(totemCount, this.getX(), this.getY(), -1);
        } else {
            this.setW(mc.fontRenderer.getStringWidth("(totem count)"));
            mc.fontRenderer.drawStringWithShadow("(totem count)", this.getX(), this.getY(), 0xFFAAAAAA);
        }
    }

    private int getTotemCount() {
        int totems = 0;
        for (int i = 0; i < 45; i++) {
            final ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                totems += stack.getCount();
            }
        }
        return totems;
    }
}