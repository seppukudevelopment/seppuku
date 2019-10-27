package me.rigamortis.seppuku.impl.module.world;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.player.EventPlayerUpdate;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.NumberValue;
import net.minecraft.client.Minecraft;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 4/19/2019 @ 10:06 PM.
 */
public final class TimerModule extends Module {

    public final NumberValue speed = new NumberValue("Speed", new String[]{"Spd"}, 4.0f, Float.class, 0.0f, 10.0f, 0.1f);

    public TimerModule() {
        super("Timer", new String[] {"Time", "Tmr"}, "Speeds up the client tick rate", "NONE", -1, ModuleType.WORLD);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        Minecraft.getMinecraft().timer.tickLength = 50;
    }

    @Override
    public String getMetaData() {
        return "" + this.speed.getFloat();
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        if(event.getStage() == EventStageable.EventStage.PRE) {
            Minecraft.getMinecraft().timer.tickLength = 50.0f / speed.getFloat();
        }
    }

}
