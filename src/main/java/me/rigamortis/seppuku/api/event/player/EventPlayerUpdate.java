package me.rigamortis.seppuku.api.event.player;

import me.rigamortis.seppuku.api.event.EventCancellable;

/**
 * Author Seth
 * 4/8/2019 @ 3:03 AM.
 */
public class EventPlayerUpdate extends EventCancellable {

    public EventPlayerUpdate(EventStage stage) {
        super(stage);
    }

}
