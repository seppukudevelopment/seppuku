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
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author Seth
 * 7/9/2019 @ 5:47 PM.
 */
public final class CapeManager {

    private List<CapeUser> capeUserList = new ArrayList<>();

    private HashMap<String, ResourceLocation> capesMap = new HashMap();

    public CapeManager() {
        this.downloadCapeUsers();
        this.downloadCapes();
        Seppuku.INSTANCE.getEventManager().addEventListener(this);
    }

    @Listener
    public void displayCape(EventCapeLocation event) {
        if (Minecraft.getMinecraft().player != null && event.getPlayer() != Minecraft.getMinecraft().player) {
            final ResourceLocation cape = this.getCape(event.getPlayer());
            if (cape != null) {
                event.setLocation(cape);
                event.setCanceled(true);
            }
        }
    }

    /**
     * Download and cache each cape for each user
     * TODO thread this
     */
    protected void downloadCapes() {
        try {
            if (Minecraft.getMinecraft().getTextureManager() != null) {
                for (CapeUser user : this.capeUserList) {
                    if (user != null) {
                        final ResourceLocation cape = this.findResource(user.getCape());

                        if (cape == null) {
                            final DynamicTexture texture = new DynamicTexture(ImageIO.read(new URL("http://seppuku.pw/files/" + user.getCape())));
                            if (texture != null) {
                                final ResourceLocation location = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("seppuku/capes", texture);
                                if (location != null) {
                                    this.capesMap.put(user.getCape(), location);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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

    /**
     * Read a list of UUIDS and their cape names
     */
    protected void downloadCapeUsers() {
        try {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(new URL("http://seppuku.pw/files/capes.txt").openStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                final String[] split = line.split(":");
                this.capeUserList.add(new CapeUser(split[0], split[1]));
            }

            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean hasCape() {
        for (CapeUser capeUser : this.capeUserList) {
            if (capeUser.getUuid().equals(Minecraft.getMinecraft().session.getProfile().getId().toString().replace("-", ""))) {
                return true;
            }
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
            final ResourceLocation res = this.findResource(user.getCape());
            if (res != null) {
                return res;
            }
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