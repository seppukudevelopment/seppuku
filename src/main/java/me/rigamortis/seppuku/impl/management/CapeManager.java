package me.rigamortis.seppuku.impl.management;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.cape.CapeUser;
import me.rigamortis.seppuku.api.event.player.EventCapeLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import javax.imageio.ImageIO;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * @author Seth
 * @author noil
 */
public final class CapeManager {

    private List<CapeUser> capeUserList = new ArrayList<>();

    private HashMap<String, ResourceLocation> capesMap = new HashMap<>();

    public CapeManager() {
        //this.downloadCapeUsers();
        //this.downloadCapes();
        Seppuku.INSTANCE.getEventManager().addEventListener(this);
    }

    @Listener
    public void displayCape(EventCapeLocation event) {
        if (Minecraft.getMinecraft().player != null && event.getPlayer() != Minecraft.getMinecraft().player) {
            String uuid = event.getPlayer().getUniqueID().toString().replace("-", "");
            if (this.hasCape(uuid)) {
                final ResourceLocation cape = this.getCape(event.getPlayer());
                if (cape != null) {
                    event.setLocation(cape);
                    event.setCanceled(true);
                }
            }
        }
    }

    public boolean hasCapeForUuid(String uuid) {
        for (CapeUser capeUser : this.capeUserList) {
            if (capeUser.getUuid().equals(uuid)) {
                return true;
            }
        }
        return false;
    }

    public boolean findCape(String uuid) {
        if (hasCapeForUuid(uuid))
            return true;

        try {
            URL url = new URL("https://seppuku.pw/cape/" + uuid);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.addRequestProperty("User-Agent", "Mozilla/4.76");
            final BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.toLowerCase().startsWith("no") && line.toLowerCase().endsWith("png")) {
                    this.capeUserList.add(new CapeUser(uuid, line));
                } else {
                    return false;
                }
            }

            reader.close();
            return true;
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return false;
    }

    /**
     * Download and cache each cape for each user
     * TODO thread this
     */
    public void downloadCape(String uuid) {
        CapeUser existingUser = null;
        for (CapeUser capeUser : this.capeUserList) {
            if (capeUser.getUuid().equals(uuid)) {
                existingUser = capeUser;
                break;
            }
        }
        if (existingUser != null) {
            if (this.capesMap.containsKey(existingUser.getCape())) {
                return;
            }
        }

        try {
            Minecraft.getMinecraft().getTextureManager();
            for (CapeUser user : this.capeUserList) {
                if (user != null) {
                    if (Objects.equals(user.getUuid(), uuid)) {
                        final ResourceLocation cape = this.findResource(user.getCape());

                        if (cape == null) {
                            URL url = new URL(user.getCape());
                            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                            httpURLConnection.addRequestProperty("User-Agent", "Mozilla/4.76");
                            final DynamicTexture texture = new DynamicTexture(ImageIO.read(httpURLConnection.getInputStream()));
                            final ResourceLocation location = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("seppuku/capes", texture);
                            this.capesMap.put(user.getCape(), location);
                        }
                    }
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    /**
     * Returns a ResourceLocation for a key
     *
     * @param key
     * @return
     */
    public ResourceLocation findResource(String key) {
        for (Map.Entry<String, ResourceLocation> entry : this.capesMap.entrySet()) {
            if (entry.getKey().equals(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

//this.capeUserList.add(new CapeUser(split[0], split[1]));
//    /**
//     * Read a list of UUIDS and their cape names
//     */
//    protected void downloadCapeUsers() {
//        try {
//            URL url = new URL("https://seppuku.pw/files/capes_new.txt");
//            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
//            httpURLConnection.addRequestProperty("User-Agent", "Mozilla/4.76");
//            final BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
//
//            String line;
//            while ((line = reader.readLine()) != null) {
//                final String[] split = line.split(";");
//                this.capeUserList.add(new CapeUser(split[0], split[1]));
//            }
//
//            reader.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    public boolean hasCape(String uuid) {
        if (this.findCape(uuid)) {
            this.downloadCape(uuid);
            return true;
        }
        return false;
    }

    /**
     * Returns a ResourceLocation for a player
     *
     * @param player
     * @return
     */
    public ResourceLocation getCape(AbstractClientPlayer player) {
        final CapeUser user = this.find(player);
        if (user != null) {
            return this.findResource(user.getCape());
        }
        return null;
    }

    /**
     * Returns a CapeUser for a player
     *
     * @param player
     * @return
     */
    public CapeUser find(AbstractClientPlayer player) {
        if (this.capeUserList.isEmpty())
            return null;

        for (CapeUser user : this.capeUserList) {
            if (user.getUuid().equals(player.getUniqueID().toString().replace("-", ""))) {
                return user;
            }
        }
        return null;
    }

    public void unload() {
        this.capeUserList.clear();
        Seppuku.INSTANCE.getEventManager().removeEventListener(this);
    }

    public List<CapeUser> getCapeUserList() {
        return capeUserList;
    }

    public void setCapeUserList(List<CapeUser> capeUserList) {
        this.capeUserList = capeUserList;
    }

    public HashMap<String, ResourceLocation> getCapesMap() {
        return capesMap;
    }

    public void setCapesMap(HashMap<String, ResourceLocation> capesMap) {
        this.capesMap = capesMap;
    }
}