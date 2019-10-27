package me.rigamortis.seppuku.impl.module.misc;

import me.rigamortis.seppuku.api.event.gui.EventBookPage;
import me.rigamortis.seppuku.api.event.gui.EventBookTitle;
import me.rigamortis.seppuku.api.module.Module;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 4/16/2019 @ 8:07 AM.
 */
public final class ColoredBooksModule extends Module {

    public ColoredBooksModule() {
        super("ColoredBooks", new String[] {"BookColor", "BookColors", "cbooks", "cbook"}, "Allows you to use the & character to color book text and titles", "NONE", -1, ModuleType.MISC);
    }

    @Listener
    public void addPage(EventBookPage event) {
        event.setPage(event.getPage().replace("&", "\247"));
    }

    @Listener
    public void editTitle(EventBookTitle event) {
        event.setTitle(event.getTitle().replace("&", "\247"));
    }

}
