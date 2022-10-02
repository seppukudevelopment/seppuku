package me.rigamortis.seppuku.api.camera;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.ClippingHelperImpl;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.ForgeHooksClient;
import org.lwjgl.util.glu.Project;

import static org.lwjgl.opengl.GL11.GL_QUADS;

/**
 * Author Seth
 * 12/9/2019 @ 6:11 AM.
 */
public class Camera {

    private final int WIDTH_RESOLUTION = 420;
    private final int HEIGHT_RESOLUTION = 420;
    private final Minecraft mc = Minecraft.getMinecraft();
    private Vec3d pos;
    private Vec3d prevPos;
    private float yaw;
    private float pitch;
    private boolean recording;
    private boolean valid;
    private boolean rendering;
    private boolean firstUpdate;
    private float farPlaneDistance;
    private Framebuffer frameBuffer;
    private int frameCount;

    public Camera() {
        this.pos = new Vec3d(0, 0, 0);
        this.yaw = 0;
        this.pitch = 0;
        this.frameBuffer = new Framebuffer(WIDTH_RESOLUTION, HEIGHT_RESOLUTION, true);
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

    public void renderWorld(float partialTicks, long nano) {
        //mc.entityRenderer.updateLightMap(partialTicks);

        if (mc.getRenderViewEntity() == null) {
            mc.setRenderViewEntity(mc.player);
        }

        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.5F);
        renderWorldPass(2, partialTicks, nano);
    }

    public void setupCameraTransform(float partialTicks) {
        this.farPlaneDistance = (float) (mc.gameSettings.renderDistanceChunks * 16);

        GlStateManager.matrixMode(5889);
        GlStateManager.loadIdentity();

        Project.gluPerspective(90.0f, (float) this.mc.displayWidth / (float) this.mc.displayHeight, 0.05F, this.farPlaneDistance * MathHelper.SQRT_2);
        GlStateManager.matrixMode(5888);
        GlStateManager.loadIdentity();

        mc.entityRenderer.orientCamera(partialTicks);
    }

    private void renderWorldPass(int pass, float partialTicks, long finishTimeNano) {
        if (mc.getRenderViewEntity() == null)
            return;

        RenderGlobal renderglobal = mc.renderGlobal;
        GlStateManager.enableCull();
        GlStateManager.viewport(0, 0, mc.displayWidth, mc.displayHeight);
        //this.updateFogColor(partialTicks);
        GlStateManager.clear(16640);
        setupCameraTransform(partialTicks);
        ActiveRenderInfo.updateRenderInfo(mc.getRenderViewEntity(), mc.gameSettings.thirdPersonView == 2);
        ClippingHelperImpl.getInstance();
        ICamera icamera = new Frustum();
        Entity entity = mc.getRenderViewEntity();
        double d0 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) partialTicks;
        double d1 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) partialTicks;
        double d2 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) partialTicks;
        icamera.setPosition(d0, d1, d2);

        GlStateManager.shadeModel(7425);
        this.mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        RenderHelper.disableStandardItemLighting();
        renderglobal.setupTerrain(entity, partialTicks, icamera, this.frameCount++, this.mc.player.isSpectator());

        if (pass == 0 || pass == 2) {
            mc.renderGlobal.updateChunks(finishTimeNano);
        }

        GlStateManager.matrixMode(5888);
        GlStateManager.pushMatrix();
        GlStateManager.disableAlpha();
        renderglobal.renderBlockLayer(BlockRenderLayer.SOLID, partialTicks, pass, entity);
        GlStateManager.enableAlpha();
        mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, mc.gameSettings.mipmapLevels > 0);
        renderglobal.renderBlockLayer(BlockRenderLayer.CUTOUT_MIPPED, partialTicks, pass, entity);
        mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
        mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
        renderglobal.renderBlockLayer(BlockRenderLayer.CUTOUT, partialTicks, pass, entity);
        mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
        GlStateManager.shadeModel(7424);
        GlStateManager.alphaFunc(516, 0.1F);

        //entities
        GlStateManager.matrixMode(5888);
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        RenderHelper.enableStandardItemLighting();
        ForgeHooksClient.setRenderPass(0);
        renderglobal.renderEntities(entity, icamera, partialTicks);
        ForgeHooksClient.setRenderPass(0);
        RenderHelper.disableStandardItemLighting();
        mc.entityRenderer.disableLightmap();
        GlStateManager.matrixMode(5888);
        GlStateManager.popMatrix();

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        this.mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
        renderglobal.drawBlockDamageTexture(Tessellator.getInstance(), Tessellator.getInstance().getBuffer(), entity, partialTicks);
        this.mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
        GlStateManager.disableBlend();

        //particles
//        mc.entityRenderer.enableLightmap();
//        mc.effectRenderer.renderLitParticles(entity, partialTicks);
//        RenderHelper.disableStandardItemLighting();
//        mc.effectRenderer.renderParticles(entity, partialTicks);
//        mc.entityRenderer.disableLightmap();

        GlStateManager.depthMask(false);
        GlStateManager.enableCull();
        //this.renderRainSnow(partialTicks);
        GlStateManager.depthMask(true);
        //renderglobal.renderWorldBorder(entity, partialTicks);
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.depthMask(false);
        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.shadeModel(7425);
        renderglobal.renderBlockLayer(BlockRenderLayer.TRANSLUCENT, partialTicks, pass, entity);

        //entities
        RenderHelper.enableStandardItemLighting();
        ForgeHooksClient.setRenderPass(1);
        renderglobal.renderEntities(entity, icamera, partialTicks);
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        ForgeHooksClient.setRenderPass(-1);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.shadeModel(7424);
        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.disableFog();

        //Seppuku.INSTANCE.getEventManager().dispatchEvent(new EventRender3D(mc.getRenderPartialTicks()));
        //Seppuku.INSTANCE.getEventManager().dispatchEvent(new EventCameraRender3D(mc.getRenderPartialTicks()));
    }

    public void updateFbo() {
        final Minecraft mc = Minecraft.getMinecraft();

        if (!this.firstUpdate) {
            mc.renderGlobal.markBlockRangeForRenderUpdate(
                    (int) mc.player.posX - 256,
                    (int) mc.player.posY - 256,
                    (int) mc.player.posZ - 256,
                    (int) mc.player.posX + 256,
                    (int) mc.player.posY + 256,
                    (int) mc.player.posZ + 256);
            this.firstUpdate = true;
        }
        if (mc.player != null) {
            this.setPrevPos(new Vec3d(mc.player.posX, mc.player.posY, mc.player.posZ));
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
            int frameLimit = mc.gameSettings.limitFramerate;
            float fovSetting = mc.gameSettings.fovSetting;

            int width = mc.displayWidth;
            int height = mc.displayHeight;

            mc.displayWidth = WIDTH_RESOLUTION;
            mc.displayHeight = HEIGHT_RESOLUTION;

            this.setCameraPos(this.getPos());
            this.setCameraAngle(this.yaw, this.pitch);

            mc.player.setSprinting(false);

            mc.gameSettings.hideGUI = true;
            mc.gameSettings.clouds = 0;
            mc.gameSettings.thirdPersonView = 0;
            mc.gameSettings.gammaSetting = 100;
            mc.gameSettings.ambientOcclusion = 0;
            mc.gameSettings.viewBobbing = false;
            mc.gameSettings.particleSetting = 0;
            mc.gameSettings.entityShadows = false;
            mc.gameSettings.limitFramerate = 10;
            mc.gameSettings.fovSetting = 90;

            this.setRecording(true);
            frameBuffer.bindFramebuffer(true);

            renderWorld(mc.timer.renderPartialTicks, System.nanoTime());
            //TODO force gui scale here?
            mc.entityRenderer.setupOverlayRendering();

            //final ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());
            //Seppuku.INSTANCE.getEventManager().dispatchEvent(new EventRender2D(mc.getRenderPartialTicks(), res));

            frameBuffer.unbindFramebuffer();
            this.setRecording(false);

            mc.player.posX = getPrevPos().x;
            mc.player.posY = getPrevPos().y;
            mc.player.posZ = getPrevPos().z;

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
            mc.gameSettings.limitFramerate = frameLimit;
            mc.gameSettings.fovSetting = fovSetting;

            mc.displayWidth = width;
            mc.displayHeight = height;

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

    public void setCameraPos(Vec3d pos) {
        mc.player.posX = pos.x;
        mc.player.posY = pos.y;
        mc.player.posZ = pos.z;

        mc.player.prevPosX = pos.x;
        mc.player.prevPosY = pos.y;
        mc.player.prevPosZ = pos.z;

        mc.player.lastTickPosX = pos.x;
        mc.player.lastTickPosY = pos.y;
        mc.player.lastTickPosZ = pos.z;
    }

    public void setCameraAngle(float yaw, float pitch) {
        mc.player.rotationYaw = yaw;
        mc.player.prevRotationYaw = yaw;
        mc.player.rotationPitch = pitch;
        mc.player.prevRotationPitch = pitch;
    }

    public Framebuffer getFrameBuffer() {
        return frameBuffer;
    }

    public void setFrameBuffer(Framebuffer frameBuffer) {
        this.frameBuffer = frameBuffer;
    }

    public Vec3d getPos() {
        return pos;
    }

    public void setPos(Vec3d pos) {
        this.pos = pos;
    }

    public Vec3d getPrevPos() {
        return prevPos;
    }

    public void setPrevPos(Vec3d prevPos) {
        this.prevPos = prevPos;
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
