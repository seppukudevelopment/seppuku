package me.rigamortis.seppuku.impl.management;

import me.rigamortis.seppuku.api.macro.Macro;

import java.util.ArrayList;
import java.util.List;

/**
 * Author Seth
 * 5/7/2019 @ 4:33 AM.
 */
public final class MacroManager {

    private List<Macro> macroList = new ArrayList<>();

    public MacroManager() {

    }

    public Macro find(String name) {
        for (Macro macro : this.macroList) {
            if (macro.getName().equalsIgnoreCase(name)) {
                return macro;
            }
        }
        return null;
    }

    public void unload() {
        this.macroList.clear();
    }

    public List<Macro> getMacroList() {
        return macroList;
    }

    public void setMacroList(List<Macro> macroList) {
        this.macroList = macroList;
    }
}
