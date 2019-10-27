package me.rigamortis.seppuku.api.notification;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.util.MathUtil;
import me.rigamortis.seppuku.api.util.Timer;
import me.rigamortis.seppuku.impl.gui.hud.component.NotificationsComponent;

/**
 * created by noil on 8/17/2019 at 3:15 PM
 */
public final class Notification {

    private final String title;

    private String text;

    private float x = 0, y = 0, width = 0, height = 0;

    private final Type type;

    private int duration; // milliseconds

    private final int maxDuration;

    private float transitionX = 0, transitionY = 0;

    private final Timer timer = new Timer();

    public Notification(String title, String text, Type type, int duration) {
        this.title = title;
        this.text = text;
        this.type = type;
        this.duration = duration;
        this.maxDuration = duration;

        final NotificationsComponent notificationsComponent = (NotificationsComponent) Seppuku.INSTANCE.getHudManager().findComponent(NotificationsComponent.class);
        if (notificationsComponent != null) {
            this.setX(notificationsComponent.getX());
            this.setY(notificationsComponent.getY());
            this.transitionX = notificationsComponent.getX();
            this.transitionY = notificationsComponent.getY();
        }

        this.timer.reset();
    }

    public Notification(String title, String text) {
        this(title, text, Type.INFO, 3000);
    }

    public void update() {
        int incline = 10;
        this.transitionX = (float) MathUtil.parabolic(this.transitionX, this.x, incline);
        this.transitionY = (float) MathUtil.parabolic(this.transitionY, this.y, incline);
        if (this.timer.passed((this.duration))) {
            Seppuku.INSTANCE.getNotificationManager().removeNotification(this);
        }
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Type getType() {
        return type;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getMaxDuration() {
        return maxDuration;
    }

    public float getTransitionX() {
        return transitionX;
    }

    public float getTransitionY() {
        return transitionY;
    }

    public enum Type {
        INFO(0xFF909090), SUCCESS(0xFF10FF10), WARNING(0xFFFFFF10), ERROR(0xFFFF1010), QUESTION(0xFF10FFFF), MISC(0xFFFFFFFF);

        private int color;

        Type(int color) {
            this.color = color;
        }

        public int getColor() {
            return color;
        }
    }
}
