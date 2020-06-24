package me.rigamortis.seppuku.api.camera;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.math.Vec3d;

import static org.lwjgl.opengl.GL11.GL_QUADS;

/**
 * Author Seth
 * 12/9/2019 @ 6:11 AM.
 */
public class Camera {

    private Vec3d pos;

    private float yaw;

    private float pitch;

    private boolean recording;

    private boolean valid;

    private boolean rendering;

    private boolean firstUpdate;

    private Framebuffer frameBuffer;

    private final int WIDTH_RESOLUTION = 800;
    private final int HEIGHT_RESOLUTION = 600;

    private final Minecraft mc = Minecraft.getMinecraft();

    public Camera() {
        this.pos = new Vec3d(0, 0, 0);
        this.yaw = 0;
        this.pitch = 0;
        this.frameBuffer = new Framebuffer(WIDTH_RESOLUTION, HEIGHT_RESOLUTION, true);
        this.frameBuffer.createFramebuffer(WIDTH_RESOLUTION, HEIGHT_RESOLUTION);
    }

    public void render(float x, float y, float w, float h) {
        if (OpenGlHelper.isFramebufferEnabled()) {
            GlStateManager.pushMatrix();
            GlStateManager.enableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.disableAlpha();
            GlStateManager.disableBlend();
            GlStateManager.enableColorMaterial();

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            frameBuffer.bindFramebufferTexture();

            final Tessellator tessellator = Tessellator.getInstance();
            final BufferBuilder bufferbuilder = tessellator.getBuffer();
            bufferbuilder.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            bufferbuilder.pos(x, h, 0).tex(0, 0).endVertex();
            bufferbuilder.pos(w, h, 0).tex(1, 0).endVertex();
            bufferbuilder.pos(w, y, 0).tex(1, 1).endVertex();
            bufferbuilder.pos(x, y, 0).tex(0, 1).endVertex();
            tessellator.draw();

            frameBuffer.unbindFramebufferTexture();

            GlStateManager.popMatrix();
        }
    }

    public void updateFbo() {
        if (!this.firstUpdate) {
            mc.renderGlobal.loadRenderers();
            this.firstUpdate = true;
        }
        if (mc.player != null) {
            double posX = mc.player.posX;
            double posY = mc.player.posY;
            double posZ = mc.player.posZ;
            double prevPosX = mc.player.prevPosX;
            double prevPosY = mc.player.prevPosY;
            double prevPosZ = mc.player.prevPosZ;
            double lastTickPosX = mc.player.lastTickPosX;
            double lastTickPosY = mc.player.lastTickPosY;
            double lastTickPosZ = mc.player.lastTickPosZ;

            float rotationYaw = mc.player.rotationYaw;
            float prevRotationYaw = mc.player.prevRotationYaw;
            float rotationPitch = mc.player.rotationPitch;
            float prevRotationPitch = mc.player.prevRotationPitch;
            boolean sprinting = mc.player.isSprinting();

            boolean hideGUI = mc.gameSettings.hideGUI;
            int clouds = mc.gameSettings.clouds;
            int thirdPersonView = mc.gameSettings.thirdPersonView;
            float gamma = mc.gameSettings.gammaSetting;
            int ambientOcclusion = mc.gameSettings.ambientOcclusion;
            boolean viewBobbing = mc.gameSettings.viewBobbing;
            int particles = mc.gameSettings.particleSetting;
            boolean shadows = mc.gameSettings.entityShadows;
            int displayWidth = mc.displayWidth;
            int displayHeight = mc.displayHeight;

            int frameLimit = mc.gameSettings.limitFramerate;
            float fovSetting = mc.gameSettings.fovSetting;

            mc.player.posX = this.getPos().x;
            mc.player.posY = this.getPos().y;
            mc.player.posZ = this.getPos().z;

            mc.player.prevPosX = this.getPos().x;
            mc.player.prevPosY = this.getPos().y;
            mc.player.prevPosZ = this.getPos().z;

            mc.player.lastTickPosX = this.getPos().x;
            mc.player.lastTickPosY = this.getPos().y;
            mc.player.lastTickPosZ = this.getPos().z;

            mc.player.rotationYaw = this.yaw;
            mc.player.prevRotationYaw = this.yaw;
            mc.player.rotationPitch = this.pitch;
            mc.player.prevRotationPitch = this.pitch;
            mc.player.setSprinting(false);

            mc.gameSettings.hideGUI = true;
            mc.gameSettings.clouds = 0;
            mc.gameSettings.thirdPersonView = 0;
            mc.gameSettings.gammaSetting = 100;
            mc.gameSettings.ambientOcclusion = 0;
            mc.gameSettings.viewBobbing = false;
            mc.gameSettings.particleSetting = 0;
            mc.gameSettings.entityShadows = false;
            mc.displayWidth = WIDTH_RESOLUTION;
            mc.displayHeight = HEIGHT_RESOLUTION;

            mc.gameSettings.limitFramerate = 10;
            mc.gameSettings.fovSetting = 110;

            this.setRecording(true);
            frameBuffer.bindFramebuffer(true);

            mc.entityRenderer.renderWorld(mc.timer.renderPartialTicks, System.nanoTime());
            mc.entityRenderer.setupOverlayRendering();

            frameBuffer.unbindFramebuffer();
            this.setRecording(false);

            mc.player.posX = posX;
            mc.player.posY = posY;
            mc.player.posZ = posZ;

            mc.player.prevPosX = prevPosX;
            mc.player.prevPosY = prevPosY;
            mc.player.prevPosZ = prevPosZ;

            mc.player.lastTickPosX = lastTickPosX;
            mc.player.lastTickPosY = lastTickPosY;
            mc.player.lastTickPosZ = lastTickPosZ;

            mc.player.rotationYaw = rotationYaw;
            mc.player.prevRotationYaw = prevRotationYaw;
            mc.player.rotationPitch = rotationPitch;
            mc.player.prevRotationPitch = prevRotationPitch;
            mc.player.setSprinting(sprinting);

            mc.gameSettings.hideGUI = hideGUI;
            mc.gameSettings.clouds = clouds;
            mc.gameSettings.thirdPersonView = thirdPersonView;
            mc.gameSettings.gammaSetting = gamma;
            mc.gameSettings.ambientOcclusion = ambientOcclusion;
            mc.gameSettings.viewBobbing = viewBobbing;
            mc.gameSettings.particleSetting = particles;
            mc.gameSettings.entityShadows = shadows;
            mc.displayWidth = displayWidth;
            mc.displayHeight = displayHeight;
            mc.gameSettings.limitFramerate = frameLimit;
            mc.gameSettings.fovSetting = fovSetting;

            this.setValid(true);
            this.setRendering(false);
        }
    }

    public void resize() {
        this.frameBuffer.createFramebuffer(WIDTH_RESOLUTION, HEIGHT_RESOLUTION);

        if (!isRecording() && isRendering()) {
            this.updateFbo();
        }
    }

    public Vec3d getPos() {
        return pos;
    }

    public void setPos(Vec3d pos) {
        this.pos = pos;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public boolean isRecording() {
        return recording;
    }

    public void setRecording(boolean recording) {
        this.recording = recording;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public boolean isRendering() {
        return rendering;
    }

    public void setRendering(boolean rendering) {
        this.rendering = rendering;
    }
}
