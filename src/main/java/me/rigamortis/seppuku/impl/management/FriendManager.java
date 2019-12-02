package me.rigamortis.seppuku.impl.management;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.friend.Friend;
import me.rigamortis.seppuku.api.util.StringUtil;
import net.minecraft.entity.Entity;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Author Seth
 * 4/16/2019 @ 11:29 PM.
 */
public final class FriendManager {

    private List<Friend> friendList = new ArrayList<>();

    /**
     * When we add friends we grab their UUID from mojangs api
     *
     * @param name
     * @param alias
     */
    public void add(String name, String alias, boolean grabUUID) {
        final Friend friend = new Friend(name, alias);
        this.friendList.add(friend);

        Seppuku.INSTANCE.getConfigManager().saveAll();

        if (grabUUID) {
            try {
                new Thread(() -> {
                    final String url = "https://api.mojang.com/users/profiles/minecraft/" + name;
                    try {
                        final String json = IOUtils.toString(new URL(url));
                        if (json.isEmpty()) {
                            return;
                        }

                        final JSONObject obj = (JSONObject) JSONValue.parseWithException(json);
                        final String uuid = obj.get("id").toString();
                        friend.setUuid(uuid);
                        Seppuku.INSTANCE.getConfigManager().saveAll();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns a friend based on the alias
     *
     * @param alias
     * @return
     */
    public Friend find(String alias) {
        for (Friend friend : this.getFriendList()) {
            if (alias.equalsIgnoreCase(friend.getAlias()) || alias.equalsIgnoreCase(friend.getName())) {
                return friend;
            }
        }
        return null;
    }

    /**
     * Returns a friend based on the uuid
     *
     * @param uuid
     * @return
     */
    public Friend findUUID(String uuid) {
        for (Friend friend : this.getFriendList()) {
            if (uuid.equals(friend.getUuid())) {
                return friend;
            }
        }
        return null;
    }

    public Friend isFriend(Entity e) {
        Friend ret = null;

        final Friend byName = this.find(e.getName());

        if (byName != null) {
            ret = byName;
        }

        final Friend byUUID = this.findUUID(e.getUniqueID().toString().replace("-", ""));

        if (byUUID != null) {
            ret = byUUID;
        }

        return ret;
    }

    /**
     * Returns the most similar friend based on the input
     *
     * @param input
     * @return
     */
    public Friend findSimilar(String input) {
        Friend friend = null;
        double similarity = 0.0f;

        for (Friend f : this.getFriendList()) {
            final double currentSimilarity = StringUtil.levenshteinDistance(input, friend.getName());

            if (currentSimilarity >= similarity) {
                similarity = currentSimilarity;
                friend = f;
            }
        }

        return friend;
    }

    public void unload() {
        this.friendList.clear();
    }

    public List<Friend> getFriendList() {
        return friendList;
    }

    public void setFriendList(List<Friend> friendList) {
        this.friendList = friendList;
    }
}
