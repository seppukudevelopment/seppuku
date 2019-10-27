package me.rigamortis.seppuku.api.event.player;

import me.rigamortis.seppuku.api.event.EventCancellable;
import net.minecraft.entity.MoverType;

/**
 * Author Seth
 * 4/25/2019 @ 7:01 PM.
 */
public class EventMove extends EventCancellable {

    private MoverType moverType;

    private double x;
    private double y;
    private double z;

    public EventMove(MoverType moverType, double x, double y, double z) {
        this.moverType = moverType;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public MoverType getMoverType() {
        return moverType;
    }

    public void setMoverType(MoverType moverType) {
        this.moverType = moverType;
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
}
