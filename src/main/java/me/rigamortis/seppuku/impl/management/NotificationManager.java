package me.rigamortis.seppuku.impl.management;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.animation.Animation;
import me.rigamortis.seppuku.api.notification.Notification;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * created by noil on 8/17/2019 at 3:14 PM
 */
public final class NotificationManager implements Animation {

    private final List<Notification> notifications = new CopyOnWriteArrayList<>();

    public NotificationManager() {
        Seppuku.INSTANCE.getAnimationManager().addAnimation(this);
    }

    public void update() {
        for (Notification notification : getNotifications()) {
            notification.update();
        }
    }

    public void unload() {
        this.notifications.clear();
    }

    public void addNotification(Notification notification) {
        this.notifications.add(notification);
    }

    public void addNotification(String title, String text, Notification.Type type, int duration) {
        this.notifications.add(new Notification(title, text, type, duration));
    }

    public void addNotification(String title, String text) {
        this.notifications.add(new Notification(title, text));
    }

    public void removeNotification(Notification notification) {
        if (this.notifications.contains(notification)) {
            this.notifications.remove(notification);
        }
    }

    public List<Notification> getNotifications() {
        return notifications;
    }
}
