package me.rigamortis.seppuku.api.util.shader;

import org.lwjgl.opengl.ARBFragmentShader;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.ARBVertexShader;
import org.lwjgl.opengl.GL11;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * @author noil
 * @since 8/8/17
 */
public class Shader {

    protected boolean useShader;

    protected int program = 0;

    public Shader() {
        this.init();
    }

    private void init() {
        int vertShader = 0, fragShader = 0;

        try {
            vertShader = createShader("/assets/seppukumod/shaders/vert", ARBVertexShader.GL_VERTEX_SHADER_ARB);
            fragShader = createShader("/assets/seppukumod/shaders/frag", ARBFragmentShader.GL_FRAGMENT_SHADER_ARB);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        } finally {
            if (vertShader == 0 || fragShader == 0) {
                return;
            }
        }

        this.program = ARBShaderObjects.glCreateProgramObjectARB();

        if (this.program == 0) {
            return;
        }

        ARBShaderObjects.glAttachObjectARB(this.program, vertShader);
        ARBShaderObjects.glAttachObjectARB(this.program, fragShader);

        ARBShaderObjects.glLinkProgramARB(this.program);
        if (ARBShaderObjects.glGetObjectParameteriARB(this.program, ARBShaderObjects.GL_OBJECT_LINK_STATUS_ARB) == GL11.GL_FALSE) {
            System.err.println(getLogInfo(this.program));
            return;
        }

        ARBShaderObjects.glValidateProgramARB(this.program);
        if (ARBShaderObjects.glGetObjectParameteriARB(this.program, ARBShaderObjects.GL_OBJECT_VALIDATE_STATUS_ARB) == GL11.GL_FALSE) {
            System.err.println(getLogInfo(this.program));
            return;
        }

        this.useShader = true;
    }

    public void grab() {
        if (this.useShader) {
            ARBShaderObjects.glUseProgramObjectARB(program);
        }
    }

    public void release() {
        if (this.useShader) {
            ARBShaderObjects.glUseProgramObjectARB(0);
        }
    }

    public int createShader(String filename, int shaderType) throws Exception {
        int shader = 0;
        try {
            shader = ARBShaderObjects.glCreateShaderObjectARB(shaderType);

            if (shader == 0) {
                return 0;
            }

            ARBShaderObjects.glShaderSourceARB(shader, readFileAsString(filename));
            ARBShaderObjects.glCompileShaderARB(shader);

            if (ARBShaderObjects.glGetObjectParameteriARB(shader, ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB) == GL11.GL_FALSE) {
                throw new RuntimeException("Error creating shader: " + getLogInfo(shader));
            }
            return shader;
        } catch (Exception e) {
            ARBShaderObjects.glDeleteObjectARB(shader);
            throw e;
        }
    }

    private String getLogInfo(int obj) {
        return ARBShaderObjects.glGetInfoLogARB(obj, ARBShaderObjects.glGetObjectParameteriARB(obj, ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB));
    }

    private String readFileAsString(String filename) throws Exception {
        StringBuilder source = new StringBuilder();
        InputStream in = Shader.class.getResourceAsStream(filename);
        Exception exception = null;
        BufferedReader reader;

        try {
            reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

            Exception innerException = null;
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    source.append(line).append("\n");
                }
            } catch (Exception e) {
                exception = e;
            } finally {
                try {
                    reader.close();
                } catch (Exception e) {
                    innerException = e;
                }
            }
            if (innerException != null) {
                throw innerException;
            }
        } catch (Exception e) {
            exception = e;
        } finally {
            try {
                in.close();
            } catch (Exception e) {
                if (exception == null) {
                    exception = e;
                } else {
                    e.printStackTrace();
                }
            }
            if (exception != null) {
                throw exception;
            }
        }
        return source.toString();
    }
}
