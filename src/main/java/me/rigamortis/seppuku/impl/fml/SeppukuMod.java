package me.rigamortis.seppuku.impl.fml;

import me.rigamortis.seppuku.Seppuku;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

/**
 * @author Seth/riga
 * @author noil
 * @author GitHub's contributors & our Discord plugin developers
 */
@Mod(modid = "seppukumod", name = "Seppuku", version = SeppukuMod.VERSION)
public final class SeppukuMod {

    public static final String VERSION = "3.2.1";

    /**
     * Our mods entry point
     *
     * @param event
     */
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        //initialize the client
        Seppuku.INSTANCE.init();
    }

}
