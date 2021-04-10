package me.rigamortis.seppuku.api.event.gui;

/**
 * Written by TBM
 */
public class EventGetGuiTabName {

    private String name;

    public EventGetGuiTabName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
