package me.rigamortis.seppuku.impl.module.misc;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.player.EventPlayerJoin;
import me.rigamortis.seppuku.api.event.player.EventPlayerLeave;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.old.OptionalValue;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 4/25/2019 @ 2:12 PM.
 */
public final class GreeterModule extends Module {

    public final OptionalValue mode = new OptionalValue("Mode", new String[]{"Mode", "M"}, 0, new String[]{"Client", "Server"});

    public GreeterModule() {
        super("Greeter", new String[]{"Greet"}, "Automatically announces when a player joins and leaves", "NONE", -1, ModuleType.MISC);
    }

    @Override
    public String getMetaData() {
        return this.mode.getSelectedOption();
    }

    @Listener
    public void onPlayerJoin(EventPlayerJoin event) {
        switch (mode.getInt()) {
            case 0:
                Seppuku.INSTANCE.logChat(event.getName() + " has joined the game");
                break;
            case 1:
                Seppuku.INSTANCE.getChatManager().add(event.getName() + " has joined the game");
                break;
        }
    }

    @Listener
    public void onPlayerLeave(EventPlayerLeave event) {
        switch (mode.getInt()) {
            case 0:
                Seppuku.INSTANCE.logChat(event.getName() + " has left the game");
                break;
            case 1:
                Seppuku.INSTANCE.getChatManager().add(event.getName() + " has left the game");
                break;
        }
    }
}
