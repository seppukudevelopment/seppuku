package me.rigamortis.seppuku.impl.management;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.util.ResourceUtil;
import me.rigamortis.seppuku.api.util.shader.ShaderProgram;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public final class ShaderManager {
    // XXX a bimap would be better for this but i didnt want an extra library for a single class -rafern
    private final Map<String, ShaderProgram> shaderList = new HashMap<String, ShaderProgram>();
    private final Map<ShaderProgram, String> programToID = new HashMap<ShaderProgram, String>();

    public ShaderManager() {
        this.loadShaders();
    }

    private void loadShaders() {
        // clear shader list
        this.destroyAll();

        // load shader programs (but don't make them, they're lazy loaded)
        try {
            // loop through all shaders in directory, recursively if theres
            // other directories
            this.loadShadersFilesystem(ShaderProgram.shadersFsDir);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            // loop through all shaders in resources, recursively if theres
            // other directories
            this.loadShadersResources(ShaderProgram.SHADER_RES_PATH);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // TODO remove me
        Seppuku.INSTANCE.getLogger().log(Level.INFO, "================= Loaded shaders =================");
        for (Iterator<String> it = this.getShaderList(); it.hasNext(); ) {
            Seppuku.INSTANCE.getLogger().log(Level.INFO, it.next());
        }
        Seppuku.INSTANCE.getLogger().log(Level.INFO, "==================================================");
    }

    public void unload() {
        this.destroyAll();
    }

    public void reload() {
        // XXX should we be using a reload event listener here?
        this.loadShaders();
    }

    private void destroyAll() {
        for (Iterator<String> it = this.getShaderList(); it.hasNext(); ) {
            this.getShader(it.next()).destroy();
        }

        this.shaderList.clear();
        this.programToID.clear();
    }

    private void loadShadersFilesystem(File dir) {
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                this.loadShadersFilesystem(file);
            } else if (file.getName().endsWith(".json")) {
                // path is relative to shaders folder, remove prefix
                final String path = file.getPath().substring(ShaderProgram.SHADER_FS_PATH.length());
                final ShaderProgram shader = ShaderProgram.loadFromJSONNoThrow(path);
                if (shader != null) {
                    this.shaderList.put(path, shader);
                    this.programToID.put(shader, path);
                }
            }
        }
    }

    private void loadShadersResources(String path) {
        Set<String> listings;
        try {
            listings = ResourceUtil.getResourceListing(ShaderManager.class, path, true);
        } catch (Exception e) {
            Seppuku.INSTANCE.getLogger().log(Level.WARNING, "Failed to recurse into resource path '" + path + "' when looking for shader files (see stack trace)");
            e.printStackTrace();
            return;
        }

        for (String listing : listings) {
            if (listing.endsWith(".json")) {
                String resourcePath = "resource://" + listing;
                final ShaderProgram shader = ShaderProgram.loadFromJSONNoThrow(resourcePath);
                if (shader != null) {
                    this.shaderList.put(resourcePath, shader);
                    this.programToID.put(shader, resourcePath);
                }
            }
        }
    }

    public ShaderProgram getShader(String shaderPath) {
        return this.shaderList.get(shaderPath);
    }

    public String getShaderID(ShaderProgram shaderProgram) {
        return this.programToID.get(shaderProgram);
    }

    public Iterator<String> getShaderList() {
        return shaderList.keySet().iterator();
    }
}
