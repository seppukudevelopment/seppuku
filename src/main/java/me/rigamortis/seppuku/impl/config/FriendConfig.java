package me.rigamortis.seppuku.impl.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.config.Configurable;
import me.rigamortis.seppuku.api.friend.Friend;
import me.rigamortis.seppuku.api.util.FileUtil;

import java.io.File;

/**
 * @author noil
 */
public final class FriendConfig extends Configurable {

    public FriendConfig(File dir) {
        super(FileUtil.createJsonFile(dir, "Friends"));
    }

    @Override
    public void onLoad() {
        super.onLoad();
        this.getJsonObject().entrySet().forEach(entry -> {
            final String name = entry.getKey();
            final String alias = entry.getValue().getAsJsonArray().get(0).getAsString();
            final String uuid = entry.getValue().getAsJsonArray().get(1).getAsString();
            Seppuku.INSTANCE.getFriendManager().getFriendList().add(new Friend(name, uuid, alias));
        });
    }

    @Override
    public void onSave() {
        JsonObject friendsListJsonObject = new JsonObject();
        Seppuku.INSTANCE.getFriendManager().getFriendList().forEach(friend -> {
            JsonArray array = new JsonArray();
            array.add(friend.getAlias());
            array.add(friend.getUuid());
            friendsListJsonObject.add(friend.getName(), array);
        });
        this.saveJsonObjectToFile(friendsListJsonObject);
    }
}
