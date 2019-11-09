package me.rigamortis.seppuku.api.event.world;

import me.rigamortis.seppuku.api.event.EventCancellable;
import net.minecraft.client.multiplayer.WorldClient;

/**
 * created by noil on 11/6/19 at 5:27 PM
 */
public class EventLoadWorld extends EventCancellable {

    private final WorldClient world;

    public EventLoadWorld(WorldClient world) {
        this.world = world;
    }

    public WorldClient getWorld() {
        return world;
    }
}

