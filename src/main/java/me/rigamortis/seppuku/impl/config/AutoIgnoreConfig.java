package me.rigamortis.seppuku.impl.config;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.config.Configurable;
import me.rigamortis.seppuku.impl.management.ConfigManager;
import me.rigamortis.seppuku.impl.module.misc.AutoIgnoreModule;

import java.io.*;

/**
 * Author Seth
 * 7/2/2019 @ 5:16 AM.
 */
public final class AutoIgnoreConfig extends Configurable {

    public AutoIgnoreConfig() {
        super(ConfigManager.CONFIG_PATH + "AutoIgnore.cfg");
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
                final AutoIgnoreModule autoIgnoreModule = (AutoIgnoreModule) Seppuku.INSTANCE.getModuleManager().find(AutoIgnoreModule.class);
                if(autoIgnoreModule != null) {
                    autoIgnoreModule.getBlacklist().add(line);
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

            final AutoIgnoreModule autoIgnoreModule = (AutoIgnoreModule) Seppuku.INSTANCE.getModuleManager().find(AutoIgnoreModule.class);

            if(autoIgnoreModule != null) {
                for (String s : autoIgnoreModule.getBlacklist()) {
                    writer.write(s);
                    writer.newLine();
                }
            }

            writer.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}