package me.rigamortis.seppuku.impl.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.config.Configurable;
import me.rigamortis.seppuku.api.gui.menu.AltData;
import me.rigamortis.seppuku.api.util.FileUtil;

import java.io.File;

/**
 * @author noil
 */
public final class AltConfig extends Configurable {

    public AltConfig(File dir) {
        super(FileUtil.createJsonFile(dir, "Alts"));
    }

    @Override
    public void onLoad(JsonObject jsonObject) {
        super.onLoad(jsonObject);
        this.getJsonObject().entrySet().forEach(entry -> {
            final String username = entry.getKey();

            String email = "";
            String password = "";
            JsonArray jsonArray = entry.getValue().getAsJsonArray();

            if (jsonArray != null) {
                if (jsonArray.size() > 0) {
                    if (jsonArray.get(0).isJsonNull()) {
                        email = "";
                    } else {
                        email = jsonArray.get(0).getAsString();
                    }
                    if (jsonArray.get(1).isJsonNull()) {
                        password = "";
                    } else {
                        password = jsonArray.get(1).getAsString();
                    }
                }
            }

            if (!email.equals("") && !password.equals(""))
                Seppuku.INSTANCE.getAltManager().addAlt(new AltData(email, username, password));
            else if (email.equals("") && !password.equals(""))
                Seppuku.INSTANCE.getAltManager().addAlt(new AltData(username, password));
            else
                Seppuku.INSTANCE.getAltManager().addAlt(new AltData(username));
        });
    }

    @Override
    public void onSave() {
        JsonObject altListJsonObject = new JsonObject();
        Seppuku.INSTANCE.getAltManager().getAlts().forEach(alt -> {
            JsonArray array = new JsonArray();
            array.add(alt.getEmail());
            array.add(alt.getPassword());
            altListJsonObject.add(alt.getUsername(), array);
        });
        this.saveJsonObjectToFile(altListJsonObject);
    }
}
