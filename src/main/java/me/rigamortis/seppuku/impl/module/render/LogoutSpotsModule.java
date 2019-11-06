package me.rigamortis.seppuku.impl.module.render;

import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import me.rigamortis.seppuku.api.event.player.EventPlayerJoin;
import me.rigamortis.seppuku.api.event.player.EventPlayerLeave;
import me.rigamortis.seppuku.api.event.player.EventPlayerUpdate;
import me.rigamortis.seppuku.api.event.render.EventRender2D;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.texture.Texture;
import me.rigamortis.seppuku.api.util.GLUProjection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.util.Map;

/**
 * created by noil on 11/5/2019 at 6:37 PM
 */
public final class LogoutSpotsModule extends Module {

    private final Map<GameProfile, PlayerData> playerCache = Maps.newConcurrentMap();
    private final Map<GameProfile, PlayerData> logoutCache = Maps.newConcurrentMap();
    private final Texture playerIcon = new Texture("location.png");

    public LogoutSpotsModule() {
        super("LogoutSpots", new String[]{"Logout", "Spots"}, "Draws the location of nearby player logouts.", "NONE", -1, ModuleType.RENDER);
    }

    @Override
    public void onToggle() {
        super.onToggle();
        playerCache.clear();
        logoutCache.clear();
    }

    @Listener
    public void onPlayerUpdate(EventPlayerUpdate event) {
        final Minecraft mc = Minecraft.getMinecraft();

        for (EntityPlayer player : mc.world.playerEntities) {
            if (player == null || player.equals(mc.player))
                continue;
            this.updateCache(player.getGameProfile(), new PlayerData(player.getPosition()));
        }
    }

    @Listener
    public void onRender2D(EventRender2D event) {
        final Minecraft mc = Minecraft.getMinecraft();

        for (GameProfile profile : this.logoutCache.keySet()) {
            final PlayerData data = this.logoutCache.get(profile);
            final GLUProjection.Projection projection = GLUProjection.getInstance().project(data.position.getX() - mc.getRenderManager().renderPosX, data.position.getY() - mc.getRenderManager().renderPosY, data.position.getZ() - mc.getRenderManager().renderPosZ, GLUProjection.ClampMode.NONE, true);
            if (projection != null && projection.isType(GLUProjection.Projection.Type.INSIDE)) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(projection.getX(), projection.getY(), 0);
                playerIcon.render(-8, -16 - 2, 16, 16);
                String text = profile.getName() + " logout";
                float textWidth = mc.fontRenderer.getStringWidth(text);
                mc.fontRenderer.drawStringWithShadow(text, -(textWidth / 2), 0, -1);
                GlStateManager.translate(-projection.getX(), -projection.getY(), 0);
                GlStateManager.popMatrix();
            }
        }
    }

    @Listener
    public void onPlayerLeave(EventPlayerLeave event) {
        final Minecraft mc = Minecraft.getMinecraft();

        for (GameProfile profile : this.playerCache.keySet()) {
            if (!profile.getId().toString().equals(event.getUuid())) // not matching uuid
                continue;

            final PlayerData data = this.playerCache.get(profile);

            if (!hasPlayerLogged(profile)) {
                this.logoutCache.put(profile, data);
            }
        }

        this.playerCache.clear();
    }

    @Listener
    public void onPlayerJoin(EventPlayerJoin event) {
        final Minecraft mc = Minecraft.getMinecraft();

        for (GameProfile profile : this.playerCache.keySet()) {
            if (!profile.getId().toString().equals(event.getUuid())) // not matching uuid
                continue;

            if (hasPlayerLogged(profile)) {
                this.logoutCache.remove(profile);
            }
        }

        this.playerCache.clear();
    }

    private void updateCache(GameProfile profile, PlayerData data) {
        this.playerCache.put(profile, data);
    }

    private boolean hasPlayerLogged(GameProfile profile) {
        return this.logoutCache.containsKey(profile);
    }

    private class PlayerData {
        BlockPos position;

        PlayerData(BlockPos position) {
            this.position = position;
        }
    }
}
