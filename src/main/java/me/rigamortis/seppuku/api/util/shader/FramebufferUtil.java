package me.rigamortis.seppuku.api.util.shader;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;

import static org.lwjgl.opengl.GL11.*;

public final class FramebufferUtil {
    private static int texID = -1;
    private static int width = 0;
    private static int height = 0;

    public static void bindDepthTexture() {
        bindDepthTextureNoUpdate();

        final Framebuffer fb = Minecraft.getMinecraft().getFramebuffer();
        if (width != fb.framebufferTextureWidth || height != fb.framebufferTextureHeight) {
            width = fb.framebufferTextureWidth;
            height = fb.framebufferTextureHeight;
            GlStateManager.glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, width, height, 0, GL_DEPTH_COMPONENT, GL_UNSIGNED_BYTE, null);
        }

        GlStateManager.glCopyTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, 0, 0, width, height);
    }

    public static void bindDepthTextureNoUpdate() {
        if (texID == -1) {
            texID = GlStateManager.generateTexture();
            GlStateManager.bindTexture(texID);
            GlStateManager.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            GlStateManager.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

            GlStateManager.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
            GlStateManager.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);
        } else {
            GlStateManager.bindTexture(texID);
        }
    }

    public static int getWidth() {
        return width;
    }

    public static int getHeight() {
        return height;
    }
}
