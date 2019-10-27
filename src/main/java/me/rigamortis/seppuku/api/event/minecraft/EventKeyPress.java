package me.rigamortis.seppuku.api.event.minecraft;

/**
 * Author Seth
 * 4/6/2019 @ 1:18 AM.
 */
public class EventKeyPress {

    private int key;

    public EventKeyPress(int key) {
        this.key = key;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }
}
