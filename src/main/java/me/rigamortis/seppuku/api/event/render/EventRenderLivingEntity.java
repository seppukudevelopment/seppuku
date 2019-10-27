package me.rigamortis.seppuku.api.event.render;

import me.rigamortis.seppuku.api.event.EventCancellable;
import net.minecraft.entity.EntityLivingBase;

/**
 * Author Seth
 * 4/23/2019 @ 12:41 PM.
 */
public class EventRenderLivingEntity extends EventCancellable {

    private EntityLivingBase entity;

    public EventRenderLivingEntity(EventStage stage, EntityLivingBase entity) {
        super(stage);
        this.entity = entity;
    }

    public EntityLivingBase getEntity() {
        return entity;
    }

    public void setEntity(EntityLivingBase entity) {
        this.entity = entity;
    }
}
