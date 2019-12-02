package me.rigamortis.seppuku.impl.management;

import java.util.ArrayList;
import java.util.List;

/**
 * Author Seth
 * 6/11/2019 @ 7:07 AM.
 */
public final class WorldManager {

    private List<WorldData> worldDataList = new ArrayList<>();

    public WorldManager() {

    }

    public WorldData find(String host) {
        for (WorldData worldData : this.worldDataList) {
            if (worldData.getHost().equalsIgnoreCase(host)) {
                return worldData;
            }
        }
        return null;
    }

    public List<WorldData> getWorldDataList() {
        return worldDataList;
    }

    public void setWorldDataList(List<WorldData> worldDataList) {
        this.worldDataList = worldDataList;
    }

    public static class WorldData {
        private String host;
        private long seed;

        public WorldData() {

        }

        public WorldData(String host, long seed) {
            this.host = host;
            this.seed = seed;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public long getSeed() {
            return seed;
        }

        public void setSeed(long seed) {
            this.seed = seed;
        }
    }

}
