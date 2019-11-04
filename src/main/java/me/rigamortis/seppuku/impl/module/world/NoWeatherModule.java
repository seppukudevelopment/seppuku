package me.rigamortis.seppuku.impl.module.world;

import me.rigamortis.seppuku.api.event.world.EventRainStrength;
import me.rigamortis.seppuku.api.module.Module;
import net.minecraft.client.Minecraft;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 8/14/2019 @ 1:45 AM.
 */
public final class NoWeatherModule extends Module {

    public NoWeatherModule() {
        super("NoWeather", new String[]{"AntiWeather"}, "Allows you to control the weather client-side", "NONE", -1, ModuleType.WORLD);
    }

    @Listener
    public void onRainStrength(EventRainStrength event) {
        if (Minecraft.getMinecraft().world == null)
            return;

        event.setCanceled(true);
    }

}
