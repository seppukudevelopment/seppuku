package me.rigamortis.seppuku.api.util;

import org.apache.commons.io.IOUtils;
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
        final String url = "https://api.minetools.eu/uuid/" + id.toString().replace("-", "");
        try {
            final String nameJson = IOUtils.toString(new URL(url));
            if (nameJson != null && nameJson.length() > 0) {
                final JSONObject jsonObject = (JSONObject) JSONValue.parseWithException(nameJson);
                if (jsonObject != null) {
                    final String nick = jsonObject.get("name").toString();
                    if (nick != null) {
                        return nick;
                    }
                }
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
