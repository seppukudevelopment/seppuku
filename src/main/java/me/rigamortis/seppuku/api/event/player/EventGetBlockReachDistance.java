package me.rigamortis.seppuku.api.event.player;

/**
 * Author Seth
 * 4/6/2019 @ 6:35 PM.
 */
public class EventGetBlockReachDistance {

    private float distance;

    public EventGetBlockReachDistance() {
    }

    public EventGetBlockReachDistance(float distance) {
        this.distance = distance;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }
}
