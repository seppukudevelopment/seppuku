package me.rigamortis.seppuku.impl.config;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.config.Configurable;
import me.rigamortis.seppuku.api.ignore.Ignored;
import me.rigamortis.seppuku.impl.management.ConfigManager;

import java.io.*;

/**
 * Author Seth
 * 6/29/2019 @ 9:14 AM.
 */
public final class IgnoreConfig extends Configurable {

    public IgnoreConfig() {
        super(ConfigManager.CONFIG_PATH + "Ignored.cfg");
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
                Seppuku.INSTANCE.getIgnoredManager().add(line);
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

            for(Ignored ignored : Seppuku.INSTANCE.getIgnoredManager().getIgnoredList()) {
                writer.write(ignored.getName());
                writer.newLine();
            }

            writer.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
