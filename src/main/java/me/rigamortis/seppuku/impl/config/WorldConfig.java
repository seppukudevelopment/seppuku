package me.rigamortis.seppuku.impl.config;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.config.Configurable;
import me.rigamortis.seppuku.impl.management.ConfigManager;
import me.rigamortis.seppuku.impl.management.WorldManager;

import java.io.*;

/**
 * Author Seth
 * 6/11/2019 @ 7:29 AM.
 */
public final class WorldConfig extends Configurable {

    public WorldConfig() {
        super(ConfigManager.CONFIG_PATH + "Worlds.cfg");
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
                final String host = split[0];
                final String seed = split[1];

                Seppuku.INSTANCE.getWorldManager().getWorldDataList().add(new WorldManager.WorldData(host, Long.parseLong(seed)));
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

            for(WorldManager.WorldData worldData : Seppuku.INSTANCE.getWorldManager().getWorldDataList()) {
                writer.write(worldData.getHost() + ":" + worldData.getSeed());
                writer.newLine();
            }

            writer.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
