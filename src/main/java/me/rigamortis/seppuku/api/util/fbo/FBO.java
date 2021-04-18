package me.rigamortis.seppuku.api.util.fbo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Framebuffer;

/**
 * @author noil
 * @since 8/8/17
 */
public class FBO {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private final Framebuffer framebuffer;

    public FBO(boolean useDepth) {
        this.framebuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, useDepth);
        setColor(0, 0, 0, 0);
    }

    public void bindFramebuffer() {
        framebuffer.bindFramebuffer(true);
    }

    public void unbindFramebuffer() {
        framebuffer.unbindFramebuffer();
    }

    public void resizeFramebuffer() {
        framebuffer.createBindFramebuffer(mc.displayWidth, mc.displayHeight);
        framebuffer.unbindFramebuffer();
    }

    public void setColor(float r, float g, float b, float a) {
        if (framebuffer.framebufferWidth != mc.displayWidth || framebuffer.framebufferHeight != mc.displayHeight) {
            resizeFramebuffer();
        }
        framebuffer.setFramebufferColor(r, g, b, a);
        framebuffer.framebufferClear();
    }

    public Framebuffer getFramebuffer() {
        return framebuffer;
    }
}
