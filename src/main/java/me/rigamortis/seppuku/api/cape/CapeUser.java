package me.rigamortis.seppuku.api.cape;

/**
 * Author Seth
 * 7/9/2019 @ 5:46 PM.
 */
public class CapeUser {

    private String uuid;
    private String cape;

    public CapeUser(String uuid, String cape) {
        this.uuid = uuid;
        this.cape = cape;
    }

    public String getCape() {
        return cape;
    }

    public void setCape(String cape) {
        this.cape = cape;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
