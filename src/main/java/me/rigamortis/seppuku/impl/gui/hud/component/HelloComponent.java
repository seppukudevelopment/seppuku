package me.rigamortis.seppuku.impl.gui.hud.component;

import me.rigamortis.seppuku.api.gui.hud.component.DraggableHudComponent;

/**
 * created by noil on 10/6/2019 at 8:53 PM
 */
public final class HelloComponent extends DraggableHudComponent {

    public HelloComponent() {
        // this will be the recognized name of our component
        super("Hello");
        this.setH(mc.fontRenderer.FONT_HEIGHT);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        // this will be the string we display on screen
        final String helloString = String.format("Hello %s :)", mc.getSession().getUsername());
        final int stringWidth = mc.fontRenderer.getStringWidth(helloString);

        // draw the string
        mc.fontRenderer.drawStringWithShadow(helloString, this.getX(), this.getY(), 0xFFFFFFFF);

        this.setW(stringWidth); // set the width of the component to the width of the displayed string
    }
}
