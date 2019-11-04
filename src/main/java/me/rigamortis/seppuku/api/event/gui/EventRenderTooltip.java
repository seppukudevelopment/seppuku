package me.rigamortis.seppuku.api.event.gui;

import me.rigamortis.seppuku.api.event.EventCancellable;
import net.minecraft.item.ItemStack;

/**
 * created by noil on 11/4/19 at 1:49 PM
 */
public class EventRenderTooltip extends EventCancellable {

    private ItemStack itemStack;

    private int x;

    private int y;

    public EventRenderTooltip(ItemStack itemStack, int x, int y) {
        this.itemStack = itemStack;
        this.x = x;
        this.y = y;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
