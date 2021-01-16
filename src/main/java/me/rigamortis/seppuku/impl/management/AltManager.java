package me.rigamortis.seppuku.impl.management;

import me.rigamortis.seppuku.api.gui.menu.AltData;

import java.util.ArrayList;
import java.util.List;

public class AltManager {

    private final List<AltData> alts = new ArrayList<>();

    public AltManager() {
    }

    public void unload() {
    }

    public void addAlt(AltData alt) {
        if (!this.alts.contains(alt))
            this.alts.add(alt);
    }

    public void removeAlt(AltData alt) {
        this.alts.remove(alt);
    }

    public void removeAlt(String username) {
        this.alts.removeIf(alt -> alt.getUsername().equalsIgnoreCase(username));
    }

    public List<AltData> getAlts() {
        return this.alts;
    }
}
