package me.rigamortis.seppuku.api.event.player;

import me.rigamortis.seppuku.api.event.EventCancellable;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.ResourceLocation;

/**
 * Author Seth
 * 12/9/2019 @ 2:48 AM.
 */
public class EventCapeLocation extends EventCancellable {

    private AbstractClientPlayer player;
    private ResourceLocation location;

    public EventCapeLocation(AbstractClientPlayer player) {
        this.player = player;
    }

    public AbstractClientPlayer getPlayer() {
        return player;
    }

    public void setPlayer(AbstractClientPlayer player) {
        this.player = player;
    }

    public ResourceLocation getLocation() {
        return location;
    }

    public void setLocation(ResourceLocation location) {
        this.location = location;
    }
}
