package me.rigamortis.seppuku.impl.module.world;

import me.rigamortis.seppuku.api.event.world.EventRainStrength;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.OptionalValue;
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
    public void onRainStrength(EventRainStrength event) {
        event.setCanceled(true);
    }

}
