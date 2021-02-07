package me.rigamortis.seppuku.impl.fml;

import me.rigamortis.seppuku.Seppuku;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

/**
 * @author Seth
 * @author noil
 * @author the entire github & discordcommunity
 */
@Mod(modid = "seppukumod", name = "Seppuku", version = SeppukuMod.VERSION, certificateFingerprint = "7979b1d0446af2675fcb5e888851a7f32637fdb9")
public final class SeppukuMod {

    public static final String VERSION = "3.1.6";

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
