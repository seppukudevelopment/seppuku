package me.rigamortis.seppuku.impl.config;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.config.Configurable;
import me.rigamortis.seppuku.impl.management.ConfigManager;
import me.rigamortis.seppuku.impl.module.world.WaypointsModule;

import java.io.*;

/**
 * Author Seth
 * 5/8/2019 @ 7:08 AM.
 */
public final class WaypointsConfig extends Configurable {

    public WaypointsConfig() {
        super(ConfigManager.CONFIG_PATH + "Waypoints.cfg");
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
                final String name = split[1];
                final String x = split[2];
                final String y = split[3];
                final String z = split[4];
                final String dim = split[5];

                final WaypointsModule.WaypointData waypointData = new WaypointsModule.WaypointData(host, name, Integer.parseInt(dim), Double.parseDouble(x), Double.parseDouble(y), Double.parseDouble(z));

                if (split.length > 6) // set color afterwards, since we create a random one in the waypoint data constructor if it doesn't exist
                    waypointData.setColor(Integer.parseInt(split[6], 16));

                Seppuku.INSTANCE.getWaypointManager().getWaypointDataList().add(waypointData);
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

            for(WaypointsModule.WaypointData waypointData : Seppuku.INSTANCE.getWaypointManager().getWaypointDataList()) {
                writer.write(waypointData.getHost() + ":" + waypointData.getName() + ":" + waypointData.getX() + ":" + waypointData.getY() + ":" + waypointData.getZ() + ":" + waypointData.getDimension() + ":" + Integer.toHexString(waypointData.getColor()));
                writer.newLine();
            }

            writer.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
