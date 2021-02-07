package me.rigamortis.seppuku.api.event.gui.hud.modulelist;

import me.rigamortis.seppuku.api.event.gui.hud.EventUIValueChanged;
import me.rigamortis.seppuku.api.value.Value;

/**
 * @author noil
 */
public class EventUIListValueChanged extends EventUIValueChanged {

    public EventUIListValueChanged(Value value) {
        super(value);
    }
}
