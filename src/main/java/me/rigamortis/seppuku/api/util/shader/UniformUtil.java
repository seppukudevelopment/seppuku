package me.rigamortis.seppuku.api.util.shader;

import me.rigamortis.seppuku.Seppuku;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.GLContext;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.logging.Level;

import static org.lwjgl.opengl.ARBShaderObjects.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL21.*;

/**
 * Util for setting uniforms with any supported type
 */
public final class UniformUtil {
    public static interface UValue {
        public void set(int loc);
    }

    public static class UIVec1 implements UValue {
        public int v0;

        public UIVec1(int v0) {
            this.v0 = v0;
        }

        public void set(int loc) {
            UniformUtil.set(loc, this.v0);
        }
    }

    public static class UIVec2 implements UValue {
        public int v0;
        public int v1;

        public UIVec2(int v0, int v1) {
            this.v0 = v0;
            this.v1 = v1;
        }

        public void set(int loc) {
            UniformUtil.set(loc, this.v0, this.v1);
        }
    }

    public static class UIVec3 implements UValue {
        public int v0;
        public int v1;
        public int v2;

        public UIVec3(int v0, int v1, int v2) {
            this.v0 = v0;
            this.v1 = v1;
            this.v2 = v2;
        }

        public void set(int loc) {
            UniformUtil.set(loc, this.v0, this.v1, this.v2);
        }
    }

    public static class UIVec4 implements UValue {
        public int v0;
        public int v1;
        public int v2;
        public int v3;

        public UIVec4(int v0, int v1, int v2, int v3) {
            this.v0 = v0;
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
        }

        public void set(int loc) {
            UniformUtil.set(loc, this.v0, this.v1, this.v2, this.v3);
        }
    }

    public static class UFVec1 implements UValue {
        public float v0;

        public UFVec1(float v0) {
            this.v0 = v0;
        }

        public void set(int loc) {
            UniformUtil.set(loc, this.v0);
        }
    }

    public static class UFVec2 implements UValue {
        public float v0;
        public float v1;

        public UFVec2(float v0, float v1) {
            this.v0 = v0;
            this.v1 = v1;
        }

        public void set(int loc) {
            UniformUtil.set(loc, this.v0, this.v1);
        }
    }

    public static class UFVec3 implements UValue {
        public float v0;
        public float v1;
        public float v2;

        public UFVec3(float v0, float v1, float v2) {
            this.v0 = v0;
            this.v1 = v1;
            this.v2 = v2;
        }

        public void set(int loc) {
            UniformUtil.set(loc, this.v0, this.v1, this.v2);
        }
    }

    public static class UFVec4 implements UValue {
        public float v0;
        public float v1;
        public float v2;
        public float v3;

        public UFVec4(float v0, float v1, float v2, float v3) {
            this.v0 = v0;
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
        }

        public void set(int loc) {
            UniformUtil.set(loc, this.v0, this.v1, this.v2, this.v3);
        }
    }

    public static class UIArray implements UValue {
        public int vecSize;
        public IntBuffer buffer;

        public UIArray(int vecSize, IntBuffer buffer) {
            if (vecSize < 1 || vecSize > 4) {
                throw new RuntimeException("Invalid vector size for UIArray: " + String.valueOf(vecSize));
            }

            this.vecSize = vecSize;
            this.buffer = buffer;
        }

        public void set(int loc) {
            UniformUtil.set(loc, this.vecSize, this.buffer);
        }
    }

    public static class UFArray implements UValue {
        public int vecSize;
        public FloatBuffer buffer;

        public UFArray(int vecSize, FloatBuffer buffer) {
            if (vecSize < 1 || vecSize > 4) {
                throw new RuntimeException("Invalid vector size for UFArray: " + String.valueOf(vecSize));
            }

            this.vecSize = vecSize;
            this.buffer = buffer;
        }

        public void set(int loc) {
            UniformUtil.set(loc, this.vecSize, this.buffer);
        }
    }

    public static class UMatrix implements UValue {
        public int matrixSize;
        public boolean transpose;
        public FloatBuffer buffer;

        public UMatrix(int matrixSize, boolean transpose, FloatBuffer buffer) {
            if (matrixSize < 2 || matrixSize > 4) {
                throw new RuntimeException("Invalid matrix size for UMatrix: " + String.valueOf(matrixSize));
            }

            this.matrixSize = matrixSize;
            this.transpose = transpose;
            this.buffer = buffer;
        }

        public void set(int loc) {
            UniformUtil.set(loc, this.matrixSize, this.transpose, this.buffer);
        }
    }

    private static Boolean arbShaders = null;
    private static boolean usingArbShaders() {
        if (arbShaders == null) {
            // OpenGlHelper.arbShaders is private :(
            ContextCapabilities contextcapabilities = GLContext.getCapabilities();
            arbShaders = new Boolean(contextcapabilities.OpenGL21);
        }

        return (boolean)arbShaders;
    }

    private static boolean canWarn = true;
    private static void showWarning(String message) {
        if (canWarn) {
            canWarn = false;
            Seppuku.INSTANCE.getLogger().log(Level.WARNING, message + ". No further warning will be shown");
        }
    }

    // methods for setting regular uniforms
    public static void set(int loc, float v0) {
        if (usingArbShaders()) {
            glUniform1fARB(loc, v0);
        } else {
            glUniform1f(loc, v0);
        }
    }

    public static void set(int loc, float v0, float v1) {
        if (usingArbShaders()) {
            glUniform2fARB(loc, v0, v1);
        } else {
            glUniform2f(loc, v0, v1);
        }
    }

    public static void set(int loc, float v0, float v1, float v2) {
        if (usingArbShaders()) {
            glUniform3fARB(loc, v0, v1, v2);
        } else {
            glUniform3f(loc, v0, v1, v2);
        }
    }

    public static void set(int loc, float v0, float v1, float v2, float v3) {
        if (usingArbShaders()) {
            glUniform4fARB(loc, v0, v1, v2, v3);
        } else {
            glUniform4f(loc, v0, v1, v2, v3);
        }
    }

    public static void set(int loc, int v0) {
        if (usingArbShaders()) {
            glUniform1iARB(loc, v0);
        } else {
            glUniform1i(loc, v0);
        }
    }

    public static void set(int loc, int v0, int v1) {
        if (usingArbShaders()) {
            glUniform2iARB(loc, v0, v1);
        } else {
            glUniform2i(loc, v0, v1);
        }
    }

    public static void set(int loc, int v0, int v1, int v2) {
        if (usingArbShaders()) {
            glUniform3iARB(loc, v0, v1, v2);
        } else {
            glUniform3i(loc, v0, v1, v2);
        }
    }

    public static void set(int loc, int v0, int v1, int v2, int v3) {
        if (usingArbShaders()) {
            glUniform4iARB(loc, v0, v1, v2, v3);
        } else {
            glUniform4i(loc, v0, v1, v2, v3);
        }
    }

    public static void set(int loc, boolean v0) {
        set(loc, v0 ? 1 : 0);
    }

    public static void set(int loc, boolean v0, boolean v1) {
        set(loc, v0 ? 1 : 0, v1 ? 1 : 0);
    }

    public static void set(int loc, boolean v0, boolean v1, boolean v2) {
        set(loc, v0 ? 1 : 0, v1 ? 1 : 0, v2 ? 1 : 0);
    }

    public static void set(int loc, boolean v0, boolean v1, boolean v2, boolean v3) {
        set(loc, v0 ? 1 : 0, v1 ? 1 : 0, v2 ? 1 : 0, v3 ? 1 : 0);
    }

    // methods for setting array uniforms. does no bounds checking; WILL crash on invalid data
    public static void set(int loc, int vecSize, FloatBuffer val) {
        switch(vecSize) {
            case 1:
                OpenGlHelper.glUniform1(loc, val);
                break;
            case 2:
                OpenGlHelper.glUniform2(loc, val);
                break;
            case 3:
                OpenGlHelper.glUniform3(loc, val);
                break;
            case 4:
                OpenGlHelper.glUniform4(loc, val);
                break;
            default:
                throw new RuntimeException("Invalid vector size for UniformUtil.set: " + String.valueOf(vecSize));
        }
    }

    public static void set(int loc, int vecSize, IntBuffer val) {
        switch(vecSize) {
            case 1:
                OpenGlHelper.glUniform1(loc, val);
                break;
            case 2:
                OpenGlHelper.glUniform2(loc, val);
                break;
            case 3:
                OpenGlHelper.glUniform3(loc, val);
                break;
            case 4:
                OpenGlHelper.glUniform4(loc, val);
                break;
            default:
                throw new RuntimeException("Invalid vector size for UniformUtil.set: " + String.valueOf(vecSize));
        }
    }

    // methods for setting matrix uniforms. does no bounds checking; WILL crash on invalid data
    public static void set(int loc, int matrixSize, boolean transpose, FloatBuffer val) {
        switch(matrixSize) {
            case 2:
                OpenGlHelper.glUniformMatrix2(loc, transpose, val);
                break;
            case 3:
                OpenGlHelper.glUniformMatrix3(loc, transpose, val);
                break;
            case 4:
                OpenGlHelper.glUniformMatrix4(loc, transpose, val);
                break;
            case 23: // 2x3 not 23
                if (usingArbShaders()) {
                    showWarning("2x3 matrix uniform with location " + String.valueOf(loc) + " not set since this feature is not available with ARB shaders; OpenGL 2.1 required");
                } else {
                    glUniformMatrix2x3(loc, transpose, val);
                }
                break;
            case 32: // 3x2 not 32
                if (usingArbShaders()) {
                    showWarning("3x2 matrix uniform with location " + String.valueOf(loc) + " not set since this feature is not available with ARB shaders; OpenGL 2.1 required");
                } else {
                    glUniformMatrix3x2(loc, transpose, val);
                }
                break;
            case 24: // 2x4 not 24
                if (usingArbShaders()) {
                    showWarning("2x4 matrix uniform with location " + String.valueOf(loc) + " not set since this feature is not available with ARB shaders; OpenGL 2.1 required");
                } else {
                    glUniformMatrix2x4(loc, transpose, val);
                }
                break;
            case 42: // 4x2 not 42
                if (usingArbShaders()) {
                    showWarning("4x2 matrix uniform with location " + String.valueOf(loc) + " not set since this feature is not available with ARB shaders; OpenGL 2.1 required");
                } else {
                    glUniformMatrix4x2(loc, transpose, val);
                }
                break;
            case 34: // 3x4 not 34
                if (usingArbShaders()) {
                    showWarning("3x4 matrix uniform with location " + String.valueOf(loc) + " not set since this feature is not available with ARB shaders; OpenGL 2.1 required");
                } else {
                    glUniformMatrix3x4(loc, transpose, val);
                }
                break;
            case 43: // 4x3 not 43
                if (usingArbShaders()) {
                    showWarning("4x3 matrix uniform with location " + String.valueOf(loc) + " not set since this feature is not available with ARB shaders; OpenGL 2.1 required");
                } else {
                    glUniformMatrix4x3(loc, transpose, val);
                }
                break;
            default:
                throw new RuntimeException("Invalid matrix size for UniformUtil.set: " + String.valueOf(matrixSize));
        }
    }

    // methods for turning a uniform value into a single object so it can be set later
    public static UFVec1 wrap(float v0) {
        return new UFVec1(v0);
    }

    public static UFVec2 wrap(float v0, float v1) {
        return new UFVec2(v0, v1);
    }

    public static UFVec3 wrap(float v0, float v1, float v2) {
        return new UFVec3(v0, v1, v2);
    }

    public static UFVec4 wrap(float v0, float v1, float v2, float v3) {
        return new UFVec4(v0, v1, v2, v3);
    }

    public static UIVec1 wrap(int v0) {
        return new UIVec1(v0);
    }

    public static UIVec2 wrap(int v0, int v1) {
        return new UIVec2(v0, v1);
    }

    public static UIVec3 wrap(int v0, int v1, int v2) {
        return new UIVec3(v0, v1, v2);
    }

    public static UIVec4 wrap(int v0, int v1, int v2, int v3) {
        return new UIVec4(v0, v1, v2, v3);
    }

    public static UIVec1 wrap(boolean v0) {
        return wrap(v0 ? 1 : 0);
    }

    public static UIVec2 wrap(boolean v0, boolean v1) {
        return wrap(v0 ? 1 : 0, v1 ? 1 : 0);
    }

    public static UIVec3 wrap(boolean v0, boolean v1, boolean v2) {
        return wrap(v0 ? 1 : 0, v1 ? 1 : 0, v2 ? 1 : 0);
    }

    public static UIVec4 wrap(boolean v0, boolean v1, boolean v2, boolean v3) {
        return wrap(v0 ? 1 : 0, v1 ? 1 : 0, v2 ? 1 : 0, v3 ? 1 : 0);
    }

    public static UFArray wrap(int vecSize, FloatBuffer val) {
        return new UFArray(vecSize, val);
    }

    public static UIArray wrap(int vecSize, IntBuffer val) {
        return new UIArray(vecSize, val);
    }

    public static UMatrix wrap(int matrixSize, boolean transpose, FloatBuffer val) {
        return new UMatrix(matrixSize, transpose, val);
    }
}
