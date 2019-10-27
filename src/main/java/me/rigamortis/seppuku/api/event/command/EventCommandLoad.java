package me.rigamortis.seppuku.api.event.command;

import me.rigamortis.seppuku.api.command.Command;

/**
 * Author Seth
 * 6/10/2019 @ 2:37 PM.
 */
public class EventCommandLoad {

    private Command command;

    public EventCommandLoad(Command command) {
        this.command = command;
    }

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }
}
