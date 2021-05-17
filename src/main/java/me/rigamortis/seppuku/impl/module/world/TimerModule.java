package me.rigamortis.seppuku.impl.module.world;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.player.EventPlayerUpdate;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.client.Minecraft;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 4/19/2019 @ 10:06 PM.
 */
public final class TimerModule extends Module {

    public final Value<Float> speed = new Value<Float>("Speed", new String[]{"Spd"}, "Tick-rate multiplier. [(20tps/second) * (this value)]", 4.0f, 0.0f, 10.0f, 0.1f);
    public final Value<Boolean> tpsSync = new Value<Boolean>("TPSSync", new String[]{"TPS", "Sync"}, "Syncs timer with the servers TPS", false);

    public TimerModule() {
        super("Timer", new String[]{"Time", "Tmr"}, "Speeds up the client tick rate.", "NONE", -1, ModuleType.WORLD);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        Minecraft.getMinecraft().timer.tickLength = 50;
    }

    @Override
    public String getMetaData() {
        return "" + String.format("%.2f", this.speed.getValue());
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (this.tpsSync.getValue()) {
                this.speed.setValue(Seppuku.INSTANCE.getTickRateManager().getTickRate() / 20.0f);
            }
            Minecraft.getMinecraft().timer.tickLength = 50.0f / this.speed.getValue();
        }
    }

}
