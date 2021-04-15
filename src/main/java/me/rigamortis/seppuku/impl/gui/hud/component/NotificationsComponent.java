package me.rigamortis.seppuku.impl.gui.hud.component;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.gui.hud.component.DraggableHudComponent;
import me.rigamortis.seppuku.api.notification.Notification;
import me.rigamortis.seppuku.api.texture.Texture;
import me.rigamortis.seppuku.api.util.RenderUtil;
import me.rigamortis.seppuku.impl.gui.hud.GuiHudEditor;
import me.rigamortis.seppuku.impl.gui.hud.anchor.AnchorPoint;

/**
 * created by noil on 8/17/2019 at 4:39 PM
 */
public final class NotificationsComponent extends DraggableHudComponent {

    private final Texture[] textures = new Texture[6];

    public NotificationsComponent(AnchorPoint anchorPoint) {
        super("Notifications");
        this.setAnchorPoint(anchorPoint); // by default anchors in the top center
        this.setVisible(true);
        this.init();
    }

    public NotificationsComponent() {
        super("Notifications");
        this.setVisible(true);
        this.init();
    }

    private void init() {
        this.textures[0] = new Texture("info.png");
        this.textures[1] = new Texture("success.png");
        this.textures[2] = new Texture("warning.png");
        this.textures[3] = new Texture("error.png");
        this.textures[4] = new Texture("question.png");
        this.textures[5] = new Texture("misc.png");
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        int offsetY = 0;
        float maxWidth = 0;

        for (Notification notification : Seppuku.INSTANCE.getNotificationManager().getNotifications()) {

            float offsetX = 0;

            if (this.getAnchorPoint() != null) {
                switch (this.getAnchorPoint().getPoint()) {
                    case TOP_CENTER:
                    case BOTTOM_CENTER:
                        offsetX = (this.getW() - 16 - mc.fontRenderer.getStringWidth(notification.getText())) / 2;
                        break;
                    case TOP_LEFT:
                    case BOTTOM_LEFT:
                        offsetX = 0;
                        break;
                    case TOP_RIGHT:
                    case BOTTOM_RIGHT:
                        offsetX = this.getW() - 16 - mc.fontRenderer.getStringWidth(notification.getText());
                        break;
                }
            }

            notification.setX(this.getX() + offsetX);
            notification.setY(this.getY() + offsetY);
            notification.setWidth(16 + mc.fontRenderer.getStringWidth(notification.getText()));
            notification.setHeight(mc.fontRenderer.FONT_HEIGHT + 5);

            //rect bg
            RenderUtil.drawRect(notification.getTransitionX() - 1, notification.getTransitionY(), notification.getTransitionX() + notification.getWidth() + 1, notification.getTransitionY() + notification.getHeight(), 0x75101010);
            //rect bar
            RenderUtil.drawRect(notification.getTransitionX() + 16 - 1, notification.getTransitionY(), notification.getTransitionX() + notification.getWidth() + 1, (notification.getTransitionY() + 1), notification.getType().getColor());
            //icon
            this.textures[notification.getType().getTextureID()].render(notification.getTransitionX() - 1, notification.getTransitionY() - 1, 16, 16);
            //text
            mc.fontRenderer.drawStringWithShadow(notification.getText(), notification.getTransitionX() + 16, notification.getTransitionY() + 4.0F, 0xFFFFFFFF);

            final float width = notification.getWidth();
            if (width >= maxWidth) {
                maxWidth = width;
            }

            offsetY += notification.getHeight();
        }

        if (Seppuku.INSTANCE.getNotificationManager().getNotifications().isEmpty()) {
            if (mc.currentScreen instanceof GuiHudEditor) {
                final String placeholder = "(notifications)";
                maxWidth = mc.fontRenderer.getStringWidth(placeholder);
                offsetY = mc.fontRenderer.FONT_HEIGHT;
                mc.fontRenderer.drawStringWithShadow(placeholder, this.getX(), this.getY(), 0xFFAAAAAA);
            } else {
                maxWidth = 0;
                offsetY = 0;
                this.setEmptyH(mc.fontRenderer.FONT_HEIGHT);
            }
        }

        this.setW(maxWidth);
        this.setH(offsetY);
    }

}
