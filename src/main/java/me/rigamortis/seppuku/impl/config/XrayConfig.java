package me.rigamortis.seppuku.impl.config;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.config.Configurable;
import me.rigamortis.seppuku.impl.management.ConfigManager;
import me.rigamortis.seppuku.impl.module.render.XrayModule;

import java.io.*;

/**
 * Author Seth
 * 4/18/2019 @ 11:05 PM.
 */
public final class XrayConfig extends Configurable {

    public XrayConfig() {
        super(ConfigManager.CONFIG_PATH + "Xray.cfg");
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
                final XrayModule xray = (XrayModule) Seppuku.INSTANCE.getModuleManager().find("Xray");
                if(xray != null) {
                    int id = Integer.parseInt(split[0]);
                    if(id > 0) {
                        xray.add(id);
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

            final XrayModule xray = (XrayModule) Seppuku.INSTANCE.getModuleManager().find("Xray");
            if(xray != null) {
                for(Integer id : xray.getIds()) {
                    writer.write("" + id.intValue());
                    writer.newLine();
                }
            }

            writer.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
