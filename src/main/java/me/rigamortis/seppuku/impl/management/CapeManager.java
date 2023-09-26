package me.rigamortis.seppuku.impl.management;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.cape.CapeUser;
import me.rigamortis.seppuku.api.event.player.EventCapeLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Seth
 * @author noil
 */
public final class CapeManager {

    private final List<CapeUser> capeUserList = new ArrayList<>();

    private final HashMap<String, ResourceLocation> capesMap = new HashMap<>();

    public CapeManager() {
        this.downloadCapeUsers();
        this.downloadCapes();
        Seppuku.INSTANCE.getEventManager().addEventListener(this);
    }

    @Listener
    public void displayCape(EventCapeLocation event) {
        if (event.getPlayer() != null) {
            if (Minecraft.getMinecraft().player != null && event.getPlayer() != Minecraft.getMinecraft().player) {
                if (this.getCape(event.getPlayer()) != null) {
                    event.setLocation(this.getCape(event.getPlayer()));
                    event.setCanceled(true);
                }
            }
        }
    }

    public void downloadCapeUsers() {
        try {
            URL url = new URL("https://seppuku.pw/capes/");
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.addRequestProperty("User-Agent", "Mozilla/4.76");
            final List<String> lines = IOUtils.readLines(httpURLConnection.getInputStream(), StandardCharsets.UTF_8);
            lines.stream().filter(line -> line.contains(";")).forEach(line -> {
                final String[] split = line.split(";");
                if (split.length > 1) {
                    if (split[0] != null && split[1] != null) {
                        if (split[1].toLowerCase().endsWith("png")) {
                            this.capeUserList.add(new CapeUser(split[0], split[1]));
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Download and cache each cape for each user
     * TODO thread this
     */
    public void downloadCapes() {
        for (CapeUser capeUser : this.capeUserList) {
            if (this.findResource(capeUser.getCape()) == null) {
                try {
                    URL url = new URL(capeUser.getCape());
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.addRequestProperty("User-Agent", "Mozilla/4.76");
                    final BufferedImage imageIO = ImageIO.read(httpURLConnection.getInputStream());
                    if (imageIO != null) {
                        if (imageIO.getWidth() <= 2048 && imageIO.getHeight() <= 1024) {
                            final DynamicTexture texture = new DynamicTexture(imageIO);
                            final ResourceLocation location = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("seppuku/capes", texture);
                            this.capesMap.put(capeUser.getCape(), location);
                        }
                    }
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
        }
    }

    /**
     * Returns a ResourceLocation for a key
     *
     * @param key
     * @return
     */
    public ResourceLocation findResource(String key) {
        return this.capesMap.entrySet().stream().filter(entry -> entry.getKey().equals(key)).findFirst().map(Map.Entry::getValue).orElse(null);
    }

    /**
     * Returns a ResourceLocation for a player
     *
     * @param player
     * @return
     */
    public ResourceLocation getCape(AbstractClientPlayer player) {
        ResourceLocation result = null;
        final CapeUser user = this.find(player);

        if (user != null) {
            result = this.findResource(user.getCape());
        }

        return result;
    }

    /**
     * Returns a CapeUser for a player
     *
     * @param player
     * @return
     */
    public CapeUser find(AbstractClientPlayer player) {
        CapeUser result = null;
        if (!this.capeUserList.isEmpty()) {
            for (CapeUser user : this.capeUserList) {
                if (user.getUuid().equals(player.getUniqueID().toString().replace("-", ""))) {
                    result = user;
                    break;
                }
            }
        }

        return result;
    }

    public boolean hasCape() {
        return this.capeUserList.stream().anyMatch(capeUser -> capeUser.getUuid().equals(Minecraft.getMinecraft().session.getProfile().getId().toString().replace("-", "")));
    }

    public void unload() {
        this.capeUserList.clear();
        Seppuku.INSTANCE.getEventManager().removeEventListener(this);
    }

    public List<CapeUser> getCapeUserList() {
        return capeUserList;
    }

    public HashMap<String, ResourceLocation> getCapesMap() {
        return capesMap;
    }
}