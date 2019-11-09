package me.rigamortis.seppuku.api.util;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

/**
 * created by noil on 11/5/19 at 7:53 PM
 */
public final class NetworkUtil {

    public static String resolveUsername(UUID id) {
        final String url = "https://api.mojang.com/user/profiles/" + id.toString().replace("-", "") + "/names";
        try {
            final String nameJson = IOUtils.toString(new URL(url));
            if (nameJson != null) {
                final JSONArray nameValue = (JSONArray) JSONValue.parseWithException(nameJson);
                if (nameValue != null) {
                    final String playerSlot = nameValue.get(nameValue.size() - 1).toString();
                    if (playerSlot != null) {
                        final JSONObject nameObject = (JSONObject) JSONValue.parseWithException(playerSlot);
                        if (nameObject != null) {
                            return nameObject.get("name").toString();
                        }
                    }
                }
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
