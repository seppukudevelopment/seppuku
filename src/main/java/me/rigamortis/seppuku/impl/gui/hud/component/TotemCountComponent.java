package me.rigamortis.seppuku.impl.gui.hud.component;

import me.rigamortis.seppuku.api.gui.hud.component.DraggableHudComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

/**
 * Author: fsck
 * 2019-10-21
 */
public final class TotemCountComponent extends DraggableHudComponent {

    public TotemCountComponent() {
        super("TotemCount");
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);
        // RenderUtil.drawRect(this.getX(), this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), 0x90222222);
        final Minecraft mc = Minecraft.getMinecraft();
        final String totemCount = "Totems: " + Integer.toString(getTotemCount());

        this.setW(mc.fontRenderer.getStringWidth(totemCount));
        this.setH(mc.fontRenderer.FONT_HEIGHT);
        mc.fontRenderer.drawStringWithShadow(totemCount, this.getX(), this.getY(), -1);
    }

        private int getTotemCount() {
            int totems = 0;
            for(int i = 0; i < 45; i++) {
                final ItemStack stack = Minecraft.getMinecraft().player.inventory.getStackInSlot(i);
                if(stack.getItem() == Items.TOTEM_OF_UNDYING){
                    totems += stack.getCount();
                }
            }
            return totems;
        }
    }