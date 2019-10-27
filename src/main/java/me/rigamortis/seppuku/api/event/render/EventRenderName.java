package me.rigamortis.seppuku.api.event.render;

import me.rigamortis.seppuku.api.event.EventCancellable;
import net.minecraft.entity.EntityLivingBase;

/**
 * Author Seth
 * 4/9/2019 @ 11:01 AM.
 */
public class EventRenderName extends EventCancellable {

    private EntityLivingBase entity;

    public EventRenderName(EntityLivingBase entity) {
        this.entity = entity;
    }

    public EntityLivingBase getEntity() {
        return entity;
    }

    public void setEntity(EntityLivingBase entity) {
        this.entity = entity;
    }
}
