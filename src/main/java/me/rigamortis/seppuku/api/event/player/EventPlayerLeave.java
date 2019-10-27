package me.rigamortis.seppuku.api.event.player;

/**
 * Author Seth
 * 7/23/2019 @ 7:41 AM.
 */
public class EventPlayerLeave {

    private String name;
    private String uuid;

    public EventPlayerLeave(String name, String uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

}
