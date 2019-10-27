package me.rigamortis.seppuku.api.event.minecraft;

import me.rigamortis.seppuku.api.event.EventCancellable;
import net.minecraft.client.gui.GuiScreen;

/**
 * Author Seth
 * 4/6/2019 @ 1:27 AM.
 */
public class EventDisplayGui extends EventCancellable {

    private GuiScreen screen;

    public EventDisplayGui(GuiScreen screen) {
        this.screen = screen;
    }

    public GuiScreen getScreen() {
        return screen;
    }

    public void setScreen(GuiScreen screen) {
        this.screen = screen;
    }
}
