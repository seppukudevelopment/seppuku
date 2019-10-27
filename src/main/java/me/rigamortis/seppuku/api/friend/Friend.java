package me.rigamortis.seppuku.api.friend;

/**
 * Author Seth
 * 4/16/2019 @ 11:28 PM.
 */
public final class Friend {

    private String name;
    private String uuid;
    private String alias;

    public Friend() {

    }

    public Friend(String name, String alias) {
        this.name = name;
        this.alias = alias;
    }

    public Friend(String name, String uuid, String alias) {
        this.name = name;
        this.uuid = uuid;
        this.alias = alias;
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

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
