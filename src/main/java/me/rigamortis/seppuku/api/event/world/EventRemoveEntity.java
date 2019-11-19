package me.rigamortis.seppuku.api.event.world;

import net.minecraft.entity.Entity;

/**
 * Author Seth
 * 11/10/2019 @ 3:31 PM.
 */
public class EventRemoveEntity {

    private Entity entity;

    public EventRemoveEntity(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }
}
