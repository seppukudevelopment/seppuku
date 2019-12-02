package me.rigamortis.seppuku.impl.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.config.Configurable;
import me.rigamortis.seppuku.api.util.FileUtil;
import me.rigamortis.seppuku.impl.module.world.WaypointsModule;

import java.io.File;

/**
 * @author noil
 */
public final class WaypointsConfig extends Configurable {

    public WaypointsConfig(File dir) {
        super(FileUtil.createJsonFile(dir, "Waypoints"));
    }

    @Override
    public void onLoad() {
        super.onLoad();

        final JsonArray waypointsJsonArray = this.getJsonObject().get("Waypoints").getAsJsonArray();

        for (JsonElement jsonElement : waypointsJsonArray) {
            if (jsonElement instanceof JsonArray) {
                final JsonArray waypointDataJson = (JsonArray) jsonElement;

                final String host = waypointDataJson.get(0).getAsString();
                final String name = waypointDataJson.get(1).getAsString();
                final int x = waypointDataJson.get(2).getAsInt();
                final int y = waypointDataJson.get(3).getAsInt();
                final int z = waypointDataJson.get(4).getAsInt();
                final int dimension = waypointDataJson.get(5).getAsInt();
                final String color = waypointDataJson.get(6).getAsString();

                final WaypointsModule.WaypointData waypointData = new WaypointsModule.WaypointData(host, name, dimension, x, y, z);
                waypointData.setColor((int) Long.parseLong(color, 16));

                Seppuku.INSTANCE.getWaypointManager().getWaypointDataList().add(waypointData);
            }
        }
    }

    @Override
    public void onSave() {
        JsonObject save = new JsonObject();
        JsonArray waypointsJsonArray = new JsonArray();

        Seppuku.INSTANCE.getWaypointManager().getWaypointDataList().forEach(waypoint -> {
            JsonArray waypointDataJson = new JsonArray();
            waypointDataJson.add(waypoint.getHost());
            waypointDataJson.add(waypoint.getName());
            waypointDataJson.add((int) waypoint.getX());
            waypointDataJson.add((int) waypoint.getY());
            waypointDataJson.add((int) waypoint.getZ());
            waypointDataJson.add(waypoint.getDimension());
            waypointDataJson.add(Integer.toHexString(waypoint.getColor()).toUpperCase());
            waypointsJsonArray.add(waypointDataJson);
        });

        save.add("Waypoints", waypointsJsonArray);
        this.saveJsonObjectToFile(save);
    }
}
