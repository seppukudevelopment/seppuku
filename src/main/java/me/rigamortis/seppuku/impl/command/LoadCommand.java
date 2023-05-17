package me.rigamortis.seppuku.impl.command;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.command.Command;
import me.rigamortis.seppuku.impl.config.*;
import net.minecraft.client.Minecraft;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author noil
 */
public final class LoadCommand extends Command {

    public LoadCommand() {
        super("Load", new String[]{"Lode"}, "Load a config from your profile on Seppuku's website", "Load <pin>");
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 2, 2)) {
            this.printUsage();
            return;
        }

        final String[] split = input.split(" ");

        JsonObject configJson = null;
        try {
            final String stringUrl = "https://seppuku.pw/config/" + Minecraft.getMinecraft().player.getUniqueID().toString().replace("-", "") + "/" + split[1];
            URL url = new URL(stringUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.addRequestProperty("User-Agent", "Mozilla/4.76");
            final BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            String line;
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("<pre>") && !line.startsWith("</pre>")) {
                    stringBuilder.append(line);
                }
            }
            reader.close();
            if (stringBuilder.toString().length() > 0) {
                configJson = new JsonParser().parse(stringBuilder.toString()).getAsJsonObject();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Seppuku.INSTANCE.logChat("\247c" + "Error loading config from server");
        }

        if (configJson != null) {
            configJson.entrySet().forEach(entry -> {
                if (entry.getKey().equalsIgnoreCase("Client")) {
                    this.loadConfigForClass(ClientConfig.class, entry.getValue().getAsJsonObject());
                }
                if (entry.getKey().equalsIgnoreCase("Xray")) {
                    this.loadConfigForClass(XrayConfig.class, entry.getValue().getAsJsonObject());
                }
                if (entry.getKey().equalsIgnoreCase("Search")) {
                    this.loadConfigForClass(SearchConfig.class, entry.getValue().getAsJsonObject());
                }
                Seppuku.INSTANCE.getModuleManager().getModuleList().forEach(module -> {
                    if (entry.getKey().equalsIgnoreCase("Module" + module.getDisplayName())) {
                        this.loadModuleConfigForClass(ModuleConfig.class, entry.getValue().getAsJsonObject(), module.getDisplayName());
                    }
                });
                Seppuku.INSTANCE.getHudManager().getComponentList().forEach(hudComponent -> {
                    if (entry.getKey().equalsIgnoreCase("HudComponent" + hudComponent.getName())) {
                        this.loadHudConfigForClass(HudConfig.class, entry.getValue().getAsJsonObject(), hudComponent.getName());
                    }
                });
            });

//            Seppuku.INSTANCE.getConfigManager().saveAll();
//            Seppuku.INSTANCE.reload();

            Seppuku.INSTANCE.logChat("\247c" + "Loaded config from server");
        }
    }

    private void loadConfigForClass(Class configClass, JsonObject jsonObject) {
        Seppuku.INSTANCE.getConfigManager().getConfigurableList().stream().filter(configurable -> configurable.getClass().equals(configClass)).forEach(configurable -> {
            configurable.onLoad(jsonObject);
        });
    }

    private void loadModuleConfigForClass(Class configClass, JsonObject jsonObject, String displayName) {
        Seppuku.INSTANCE.getConfigManager().getConfigurableList().stream().filter(configurable -> configurable.getClass().equals(ModuleConfig.class)).forEach(configurable -> {
            final ModuleConfig moduleConfig = (ModuleConfig) configurable;
            if (moduleConfig.getModule().getDisplayName().equalsIgnoreCase(displayName)) {
                moduleConfig.onLoad(jsonObject);
            }
        });
    }

    private void loadHudConfigForClass(Class configClass, JsonObject jsonObject, String name) {
        Seppuku.INSTANCE.getConfigManager().getConfigurableList().stream().filter(configurable -> configurable.getClass().equals(HudConfig.class)).forEach(configurable -> {
            final HudConfig hudConfig = (HudConfig) configurable;
            if (hudConfig.getHudComponent().getName().equalsIgnoreCase(name)) {
                hudConfig.onLoad(jsonObject);
            }
        });
    }

}
