package me.rigamortis.seppuku.api.event;

/**
 * Author Seth
 * 4/5/2019 @ 6:37 PM.
 */
public class EventStageable {

    private EventStage stage;

    public EventStageable() {

    }

    public EventStageable(EventStage stage) {
        this.stage = stage;
    }

    public EventStage getStage() {
        return stage;
    }

    public void setStage(EventStage stage) {
        this.stage = stage;
    }

    public enum EventStage {
        PRE, POST
    }

}
