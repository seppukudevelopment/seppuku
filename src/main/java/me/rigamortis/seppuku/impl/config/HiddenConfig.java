package me.rigamortis.seppuku.impl.config;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.config.Configurable;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.impl.management.ConfigManager;

import java.io.*;

/**
 * Author Seth
 * 4/18/2019 @ 11:02 PM.
 */
public final class HiddenConfig extends Configurable {

    public HiddenConfig() {
        super(ConfigManager.CONFIG_PATH + "Hidden.cfg");
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
                if(mod != null && mod.getType() != Module.ModuleType.HIDDEN) {
                    if(split[1] != "") {
                        mod.setHidden(Boolean.parseBoolean(split[1]));
                    }
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
                if(mod.getType() != Module.ModuleType.HIDDEN) {
                    writer.write(mod.getDisplayName() + ":" + mod.isHidden());
                    writer.newLine();
                }
            }

            writer.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
