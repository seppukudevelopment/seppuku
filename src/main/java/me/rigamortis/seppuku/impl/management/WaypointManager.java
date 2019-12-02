package me.rigamortis.seppuku.impl.management;

import me.rigamortis.seppuku.impl.module.world.WaypointsModule;

import java.util.ArrayList;
import java.util.List;

/**
 * Author Seth
 * 5/8/2019 @ 6:50 AM.
 */
public final class WaypointManager {

    private List<WaypointsModule.WaypointData> waypointDataList = new ArrayList<>();

    public WaypointsModule.WaypointData find(String host, String name) {
        for (WaypointsModule.WaypointData data : this.waypointDataList) {
            if (data.getHost().equalsIgnoreCase(host) && data.getName().equalsIgnoreCase(name)) {
                return data;
            }
        }
        return null;
    }

    public void unload() {
        this.waypointDataList.clear();
    }

    public List<WaypointsModule.WaypointData> getWaypointDataList() {
        return waypointDataList;
    }

    public void setWaypointDataList(List<WaypointsModule.WaypointData> waypointDataList) {
        this.waypointDataList = waypointDataList;
    }
}
