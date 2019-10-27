package me.rigamortis.seppuku.api.patch;

import me.rigamortis.seppuku.api.patch.access.AccessPatch;

/**
 * Author Seth
 * 4/4/2019 @ 11:23 PM.
 */
public class ClassPatch {

    private String mcpName;
    private String notchName;
    private boolean debug;
    private AccessPatch accessPatch;

    public ClassPatch(String mcpName) {
        this.mcpName = mcpName;
    }

    public ClassPatch(String mcpName, String notchName) {
        this.mcpName = mcpName;
        this.notchName = notchName;
    }

    public String getMcpName() {
        return mcpName;
    }

    public void setMcpName(String mcpName) {
        this.mcpName = mcpName;
    }

    public String getNotchName() {
        return notchName;
    }

    public void setNotchName(String notchName) {
        this.notchName = notchName;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public AccessPatch getAccessPatch() {
        return accessPatch;
    }

    public void setAccessPatch(AccessPatch accessPatch) {
        this.accessPatch = accessPatch;
    }
}
