package me.rigamortis.seppuku.impl.config;

import com.google.gson.JsonObject;
import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.config.Configurable;
import me.rigamortis.seppuku.api.util.FileUtil;

import java.io.File;

/**
 * @author noil
 */
public final class ClientConfig extends Configurable {

    public ClientConfig(File dir) {
        super(FileUtil.createJsonFile(dir, "Client"));
    }

    @Override
    public void onLoad(JsonObject jsonObject) {
        super.onLoad(jsonObject);

        this.getJsonObject().entrySet().forEach(entry -> {
            if (entry.getKey().equalsIgnoreCase("CustomMainMenuHidden")) {
                Seppuku.INSTANCE.getConfigManager().setCustomMainMenuHidden(entry.getValue().getAsBoolean());
            }
        });
    }

    @Override
    public void onSave() {
        JsonObject clientConfigJsonObject = new JsonObject();
        clientConfigJsonObject.addProperty("CustomMainMenuHidden", Seppuku.INSTANCE.getConfigManager().isCustomMainMenuHidden());
        this.saveJsonObjectToFile(clientConfigJsonObject);
    }
}

