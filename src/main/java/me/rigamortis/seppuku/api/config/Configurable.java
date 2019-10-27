package me.rigamortis.seppuku.api.config;

/**
 * Author Seth
 * 4/18/2019 @ 7:02 AM.
 */
public abstract class Configurable {

    private String path;

    public Configurable(String path) {
        this.path = path;
    }

    public abstract void load();

    public abstract void save();

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

}
