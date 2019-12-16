package me.rigamortis.seppuku.impl.management;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.camera.Camera;
import me.rigamortis.seppuku.api.event.minecraft.EventUpdateFramebufferSize;
import me.rigamortis.seppuku.api.event.render.EventRenderOverlay;
import net.minecraft.client.Minecraft;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.util.ArrayList;
import java.util.List;

/**
 * Author Seth
 * 12/9/2019 @ 6:09 AM.
 */
public final class CameraManager {

    private List<Camera> cameraList = new ArrayList();

    public CameraManager() {
        Seppuku.INSTANCE.getEventManager().addEventListener(this);
    }

    public void update() {
        if (Minecraft.getMinecraft().inGameHasFocus && Minecraft.getMinecraft().currentScreen == null) {
            for (Camera cam : this.cameraList) {
                if (cam != null && !cam.isRecording() && cam.isRendering()) {
                    cam.updateFbo();
                }
            }
        }
    }

    @Listener
    public void renderOverlay(EventRenderOverlay event) {
        if (Minecraft.getMinecraft().inGameHasFocus && Minecraft.getMinecraft().currentScreen == null) {
            for (Camera cam : this.cameraList) {
                if (cam != null && cam.isRecording()) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @Listener
    public void fboResize(EventUpdateFramebufferSize event) {
        for (Camera cam : this.cameraList) {
            if (cam != null) {
                cam.resize();
            }
        }
    }

    public void addCamera(Camera cam) {
        this.cameraList.add(cam);
    }

    public void unload() {
        this.cameraList.clear();
        Seppuku.INSTANCE.getEventManager().removeEventListener(this);
    }

    public List<Camera> getCameraList() {
        return cameraList;
    }

    public void setCameraList(List<Camera> cameraList) {
        this.cameraList = cameraList;
    }
}
