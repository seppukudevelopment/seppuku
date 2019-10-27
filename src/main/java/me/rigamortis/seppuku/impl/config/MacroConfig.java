package me.rigamortis.seppuku.impl.config;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.config.Configurable;
import me.rigamortis.seppuku.api.macro.Macro;
import me.rigamortis.seppuku.impl.management.ConfigManager;

import java.io.*;

/**
 * Author Seth
 * 5/7/2019 @ 9:57 PM.
 */
public final class MacroConfig extends Configurable {

    public MacroConfig() {
        super(ConfigManager.CONFIG_PATH + "Macros.cfg");
    }


    @Override
    public void load() {
        try{
            final File file = new File(this.getPath());

            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            final BufferedReader reader = new BufferedReader(new FileReader(this.getPath()));

            String line;
            while ((line = reader.readLine()) != null) {
                final String[] split = line.split(":");
                Seppuku.INSTANCE.getMacroManager().getMacroList().add(new Macro(split[0], split[1], split[2]));
            }

            reader.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save() {
        try{
            final File file = new File(this.getPath());

            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            final BufferedWriter writer = new BufferedWriter(new FileWriter(this.getPath()));

            for(Macro macro : Seppuku.INSTANCE.getMacroManager().getMacroList()) {
                writer.write(macro.getName() + ":" + macro.getKey() + ":" + macro.getMacro());
                writer.newLine();
            }

            writer.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
