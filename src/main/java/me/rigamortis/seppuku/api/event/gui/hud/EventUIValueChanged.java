package me.rigamortis.seppuku.api.event.gui.hud;

import me.rigamortis.seppuku.api.value.Value;

/**
 * @author noil
 */
public class EventUIValueChanged {

    private Value value;

    public EventUIValueChanged(Value value) {
        this.value = value;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }
}
