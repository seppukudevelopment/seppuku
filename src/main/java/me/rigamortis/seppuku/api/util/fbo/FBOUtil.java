package me.rigamortis.seppuku.api.util.fbo;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author noil
 * @since 8/8/17
 */
public class FBOUtil {

    private static final Map<String, FBO> framebuffers = Maps.newHashMap();

    public static FBO createFramebuffer(String name, boolean useDepth) {
        FBO fbo = new FBO(useDepth);
        framebuffers.put(name, fbo);
        return fbo;
    }

    public static FBO getFramebuffer(String name) {
        if (framebuffers.containsKey(name))
            return framebuffers.get(name);
        return createFramebuffer(name, true);
    }

    public static void deleteFramebuffer(String name) {
        framebuffers.remove(name);
    }

    public static Map<String, FBO> getFramebuffers() {
        return framebuffers;
    }
}
