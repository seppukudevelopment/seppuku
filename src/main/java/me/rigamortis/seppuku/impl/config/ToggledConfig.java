package me.rigamortis.seppuku.impl.config;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.config.Configurable;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.impl.management.ConfigManager;

import java.io.*;

/**
 * Author Seth
 * 4/18/2019 @ 7:03 AM.
 */
public final class ToggledConfig extends Configurable {

    public ToggledConfig() {
        super(ConfigManager.CONFIG_PATH + "Toggled.cfg");
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
                final Module mod = Seppuku.INSTANCE.getModuleManager().find(split[0]);
                if(mod != null && !mod.isEnabled() && mod.getType() != Module.ModuleType.HIDDEN) {
                    mod.toggle();
                }
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

            for(Module mod : Seppuku.INSTANCE.getModuleManager().getModuleList()) {
                if(mod.isEnabled() && mod.getType() != Module.ModuleType.HIDDEN) {
                    writer.write(mod.getDisplayName());
                    writer.newLine();
                }
            }

            writer.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
