package me.rigamortis.seppuku.api.util.shader;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import java.nio.FloatBuffer;

/**
 * @author noil
 * @since 8/8/17
 */
public class ShaderNormal extends Shader {

    @Override
    public void grab() {
        super.grab();

        int DiffuseSampler = ARBShaderObjects.glGetUniformLocationARB(program, "DiffuseSampler");
        int TexelSize = ARBShaderObjects.glGetUniformLocationARB(program, "TexelSize");

        ARBShaderObjects.glUniform1iARB(DiffuseSampler, 0);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());

        FloatBuffer texelSizeBuffer = BufferUtils.createFloatBuffer(2);
        texelSizeBuffer.position(0);
        texelSizeBuffer.put(1.0F / sr.getScaledWidth());
        texelSizeBuffer.put(1.0F / sr.getScaledHeight());

        // Flip the float buffer
        texelSizeBuffer.flip();

        ARBShaderObjects.glUniform2ARB(TexelSize, texelSizeBuffer);
    }
}
