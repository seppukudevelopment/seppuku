package me.rigamortis.seppuku.impl.management;

import me.rigamortis.seppuku.api.config.Configurable;
import me.rigamortis.seppuku.impl.config.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Author Seth
 * 4/18/2019 @ 7:04 AM.
 */
public final class ConfigManager {

    private List<Configurable> configurableList = new ArrayList<>();

    public static final String CONFIG_PATH = "Seppuku 1.12.2/Config/";

    private boolean firstLaunch;

    public ConfigManager() {
        final File dir = new File(CONFIG_PATH);

        if (!dir.exists()) {
            this.firstLaunch = true;
            dir.mkdirs();
        }

        this.configurableList.add(new ToggledConfig());
        this.configurableList.add(new BindConfig());
        this.configurableList.add(new ColorConfig());
        this.configurableList.add(new HiddenConfig());
        this.configurableList.add(new FriendConfig());
        this.configurableList.add(new XrayConfig());
        this.configurableList.add(new ValueConfig());
        this.configurableList.add(new MacroConfig());
        this.configurableList.add(new WaypointsConfig());
        this.configurableList.add(new WorldConfig());
        this.configurableList.add(new IgnoreConfig());
        this.configurableList.add(new AutoIgnoreConfig());
        this.configurableList.add(new HudConfig());

        if (this.firstLaunch) {
            this.saveAll();
        } else {
            this.loadAll();
        }

        //Runtime.getRuntime().addShutdownHook(new Thread(() -> Seppuku.INSTANCE.getConfigManager().saveAll()));
    }

    public void saveAll() {
        new Thread(() -> {
            for (Configurable cfg : configurableList) {
                cfg.save();
            }
        }).start();
    }

    public void loadAll() {
        new Thread(() -> {
            for (Configurable cfg : configurableList) {
                cfg.load();
            }
        }).start();
    }

    public boolean isFirstLaunch() {
        return firstLaunch;
    }

    public void setFirstLaunch(boolean firstLaunch) {
        this.firstLaunch = firstLaunch;
    }

    public List<Configurable> getConfigurableList() {
        return configurableList;
    }

    public void setConfigurableList(List<Configurable> configurableList) {
        this.configurableList = configurableList;
    }
}
