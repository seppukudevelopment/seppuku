package me.rigamortis.seppuku.api.event.render;

import me.rigamortis.seppuku.api.event.EventCancellable;
import net.minecraft.entity.Entity;

/**
 * Author Seth
 * 4/9/2019 @ 10:43 AM.
 */
public class EventRenderEntity extends EventCancellable {

    private Entity entity;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float partialTicks;

    public EventRenderEntity(EventStage stage, Entity entity, double x, double y, double z, float yaw, float partialTicks) {
        super(stage);
        this.entity = entity;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.partialTicks = partialTicks;
    }

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPartialTicks() {
        return partialTicks;
    }

    public void setPartialTicks(float partialTicks) {
        this.partialTicks = partialTicks;
    }
}
