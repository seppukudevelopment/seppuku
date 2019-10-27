package me.rigamortis.seppuku.impl.module.world;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.player.EventPlayerUpdate;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.OptionalValue;
import net.minecraft.client.Minecraft;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 8/14/2019 @ 1:45 AM.
 */
public final class NoWeatherModule extends Module {

    public final OptionalValue mode = new OptionalValue("Mode", new String[]{"Mode", "M"}, 0, new String[]{"Remove", "Rain"});

    public NoWeatherModule() {
        super("NoWeather", new String[]{"AntiWeather"}, "Allows you to control the weather client-side", "NONE", -1, ModuleType.WORLD);
    }

    @Override
    public String getMetaData() {
        return this.mode.getSelectedOption();
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            final Minecraft mc = Minecraft.getMinecraft();
            switch (this.mode.getInt()) {
                case 0:
                    mc.world.setRainStrength(0);
                    mc.world.setThunderStrength(0);
                    break;
                case 1:
                    mc.world.setRainStrength(1.0f);
                    mc.world.setThunderStrength(0);
                    break;
            }
        }
    }

}
