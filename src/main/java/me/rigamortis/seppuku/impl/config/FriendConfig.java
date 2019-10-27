package me.rigamortis.seppuku.impl.config;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.config.Configurable;
import me.rigamortis.seppuku.api.friend.Friend;
import me.rigamortis.seppuku.impl.management.ConfigManager;

import java.io.*;

/**
 * Author Seth
 * 4/18/2019 @ 10:43 PM.
 */
public final class FriendConfig extends Configurable {

    public FriendConfig() {
        super(ConfigManager.CONFIG_PATH + "Friends.cfg");
    }

    @Override
    public void load() {
        try{
            final File file = new File(this.getPath());

            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            final BufferedReader reader = new BufferedReader(new FileReader(this.getPath()));

            String line;
            while ((line = reader.readLine()) != null) {
                final String[] split = line.split(":");
                if(split[0] != null && split[1] != null) {
                    final Friend friend = new Friend(split[0], split[1]);
                    if(split.length > 2 && split[2] != null) {
                        friend.setUuid(split[2]);
                    }
                    if(!Seppuku.INSTANCE.getFriendManager().getFriendList().contains(friend)) {
                        Seppuku.INSTANCE.getFriendManager().getFriendList().add(friend);
                    }
                }
            }

            reader.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save() {
        try{
            final File file = new File(this.getPath());

            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            final BufferedWriter writer = new BufferedWriter(new FileWriter(this.getPath()));

            for(Friend friend : Seppuku.INSTANCE.getFriendManager().getFriendList()) {
                writer.write(friend.getName() + ":" + friend.getAlias() + ":" + friend.getUuid());
                writer.newLine();
            }
            writer.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
