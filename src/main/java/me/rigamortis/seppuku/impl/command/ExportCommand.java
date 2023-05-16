package me.rigamortis.seppuku.impl.command;

import com.google.gson.JsonObject;
import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.command.Command;
import me.rigamortis.seppuku.api.config.Configurable;
import me.rigamortis.seppuku.api.gui.hud.component.HudComponent;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.FileUtil;
import me.rigamortis.seppuku.impl.config.*;

import java.io.File;

/**
 * @author noil
 */
public final class ExportCommand extends Command {
    public ExportCommand() {
        super("Export", new String[]{"Exprt"}, "Export all Module & HUD configs into a single json for upload on Seppuku's website.", "Export <config_name>");
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 2, 2)) {
            this.printUsage();
            return;
        }

        final String[] split = input.split(" ");
        final String configName = split[1];
        final File file = FileUtil.createJsonFile(Seppuku.INSTANCE.getConfigManager().getConfigDir(), configName);

        JsonObject endJson = new JsonObject();
        for (Configurable cfg : Seppuku.INSTANCE.getConfigManager().getConfigurableList()) {
            if (cfg.getClass().equals(ClientConfig.class)) {
                final JsonObject clientJson = cfg.getJsonObject();
                endJson.add("Client", clientJson);
            }
            if (cfg.getClass().equals(XrayConfig.class)) {
                final JsonObject xrayJson = cfg.getJsonObject();
                endJson.add("Xray", xrayJson);
            }
            if (cfg.getClass().equals(SearchConfig.class)) {
                final JsonObject searchJson = cfg.getJsonObject();
                endJson.add("Search", searchJson);
            }
            if (cfg.getClass().equals(NukerFilterConfig.class)) {
                final JsonObject nukerFilterJson = cfg.getJsonObject();
                endJson.add("NukerFilter", nukerFilterJson);
            }
            if (cfg.getClass().equals(ModuleConfig.class)) {
                final JsonObject moduleJson = cfg.getJsonObject();
                final ModuleConfig moduleConfig = (ModuleConfig) cfg;
                final Module module = moduleConfig.getModule();
                endJson.add("Module" + module.getDisplayName(), moduleJson);
            }
            if (cfg.getClass().equals(HudConfig.class)) {
                final JsonObject hudJson = cfg.getJsonObject();
                final HudConfig hudConfig = (HudConfig) cfg;
                final HudComponent hudComponent = hudConfig.getHudComponent();
                endJson.add("HudComponent" + hudComponent.getName(), hudJson);
            }
        }

        FileUtil.saveJsonFile(file, endJson);

        Seppuku.INSTANCE.logChat("\247c" + "Exported config " + configName + ".json into the Seppuku directory.");
    }

}
