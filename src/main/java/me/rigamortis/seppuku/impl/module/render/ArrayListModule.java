package me.rigamortis.seppuku.impl.module.render;

import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.Value;

/**
 * Author Ice
 * 05/06/2020 @ 22:49 PM.
 */
public class ArrayListModule extends Module {

    public final Value<ArrayListModule.Mode> mode = new Value<ArrayListModule.Mode>("Sorting", new String[]{"Sorting", "sort"}, "Changes arraylist sorting.", ArrayListModule.Mode.LENGTH);

    public ArrayListModule() {
        super("Arraylist", new String[]{"Arraylist"}, "Sorting", "NONE", -1, ModuleType.RENDER);
        this.setHidden(true);
    }

    public enum Mode {
        LENGTH, ALPHABET, UNSORTED;
    }
}
