package me.rigamortis.seppuku.impl.management;


import com.google.common.collect.Maps;
import me.rigamortis.seppuku.Seppuku;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.logging.Level;

public final class APIManager {

    private final Map<String, String> uuidNameCache = Maps.newConcurrentMap();

    public APIManager() {
    }

    public void unload() {
        this.uuidNameCache.clear();
    }

    public String resolveName(String uuid) {
        uuid = uuid.replace("-", "");
        if (uuidNameCache.containsKey(uuid)) {
            return uuidNameCache.get(uuid);
        }

        final String url = "https://api.mojang.com/user/profiles/" + uuid + "/names";
        try {
            final String nameJson = IOUtils.toString(new URL(url), StandardCharsets.UTF_8);
            if (nameJson != null && nameJson.length() > 0) {
                final JSONArray jsonArray = (JSONArray) JSONValue.parseWithException(nameJson);
                if (jsonArray != null) {
                    final JSONObject latestName = (JSONObject) jsonArray.get(jsonArray.size() - 1);
                    if (latestName != null) {
                        return latestName.get("name").toString();
                    }
                }
            }
        } catch (IOException | ParseException e) {
            //e.printStackTrace();
            Seppuku.INSTANCE.getLogger().log(Level.INFO, "Couldn't connect to api.mojang.com for the uuid resolver.");
        }

        return null;
    }

}
