package me.rigamortis.seppuku.impl.module.render;

import me.rigamortis.seppuku.api.module.Module;

/**
 * created by noil on 11/3/2019 at 1:55 PM
 */
public final class PortalFinderModule extends Module {

    public PortalFinderModule() {
        super("PortalFinder", new String[]{"PortalFinder", "PFinder"}, "Highlights nearby portals.", "NONE", -1, Module.ModuleType.RENDER);
    }
}
