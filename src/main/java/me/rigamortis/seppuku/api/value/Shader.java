package me.rigamortis.seppuku.api.value;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.util.shader.ShaderProgram;

import javax.annotation.Nullable;

public class Shader {
    private String id;

    public Shader(String id) {
        this.setShaderID(id);
    }

    public Shader() {
        this("");
    }

    public String getShaderID() {
        return this.id;
    }

    public void setShaderID(String id) {
        this.id = id;
    }

    @Nullable
    public ShaderProgram getShaderProgram() {
        if (this.id.equals("")) {
            return null;
        }

        return Seppuku.INSTANCE.getShaderManager().getShader(this.id);
    }

    @Override
    public String toString() {
        if (this.id.equals("")) {
            return "no shader picked";
        } else {
            ShaderProgram sp = this.getShaderProgram();
            if (sp == null) {
                return "missing shader (" + this.id + ")";
            } else {
                return sp.getName();
            }
        }
    }
}