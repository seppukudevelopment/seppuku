package me.rigamortis.seppuku.impl.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.config.Configurable;
import me.rigamortis.seppuku.api.util.FileUtil;

import java.io.File;

/**
 * @author noil
 */
public final class IgnoreConfig extends Configurable {

    public IgnoreConfig(File dir) {
        super(FileUtil.createJsonFile(dir, "Ignored"));
    }

    @Override
    public void onLoad() {
        super.onLoad();

        final JsonArray ignoredJsonArray = this.getJsonObject().get("Ignored").getAsJsonArray();

        for (JsonElement jsonElement : ignoredJsonArray) {
            final String blacklistedName = jsonElement.getAsString();
            Seppuku.INSTANCE.getIgnoredManager().add(blacklistedName);
        }
    }

    @Override
    public void onSave() {
        JsonObject save = new JsonObject();
        JsonArray ignoredJsonArray = new JsonArray();
        Seppuku.INSTANCE.getIgnoredManager().getIgnoredList().forEach(ignored -> ignoredJsonArray.add(ignored.getName()));
        save.add("Ignored", ignoredJsonArray);
        this.saveJsonObjectToFile(save);
    }
}
