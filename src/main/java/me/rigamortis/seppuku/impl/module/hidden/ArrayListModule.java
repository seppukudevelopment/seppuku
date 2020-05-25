package me.rigamortis.seppuku.impl.module.hidden;

import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.Value;

/**
 * Author Ice
 * 05/06/2020 @ 22:49 PM.
 */
public class ArrayListModule extends Module {

    public final Value<ArrayListModule.Mode> mode = new Value<ArrayListModule.Mode>("Sorting", new String[]{"Sorting", "sort"}, "Changes arraylist sorting method.", ArrayListModule.Mode.LENGTH);

    public ArrayListModule() {
        super("ArrayList", new String[]{"ArrayList", "arraylist", "modulelist", "modlist", "array-list", "alist"}, "Optional values for the ArrayList hud component.", "NONE", -1, ModuleType.HIDDEN);
        this.setHidden(true);
    }

    public enum Mode {
        LENGTH, ALPHABET, UNSORTED;
    }
}
