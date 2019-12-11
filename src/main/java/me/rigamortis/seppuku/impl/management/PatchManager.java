package me.rigamortis.seppuku.impl.management;

import me.rigamortis.seppuku.api.patch.ClassPatch;
import me.rigamortis.seppuku.api.util.ReflectionUtil;
import me.rigamortis.seppuku.impl.patch.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Author Seth
 * 4/4/2019 @ 11:42 PM.
 */
public final class PatchManager {

    private List<ClassPatch> patchList = new ArrayList<ClassPatch>();

    private Environment env;

    public PatchManager(Environment env) {
        //set our environment for mappings
        this.setEnv(env);

        //add internal patches
        this.patchList.add(new MinecraftPatch());
        this.patchList.add(new EntityRendererPatch());
        this.patchList.add(new WorldPatch());
        this.patchList.add(new NetworkManagerPatch());
        this.patchList.add(new PlayerControllerMPPatch());
        this.patchList.add(new VisGraphPatch());
        this.patchList.add(new EntityPlayerSPPatch());
        this.patchList.add(new EntityPlayerPatch());
        this.patchList.add(new GuiScreenBookPatch());
        this.patchList.add(new GuiIngameForgePatch());
        this.patchList.add(new ItemRendererPatch());
        this.patchList.add(new RenderManagerPatch());
        this.patchList.add(new RenderLivingBasePatch());
        this.patchList.add(new EntityPigPatch());
        this.patchList.add(new EntityLlamaPatch());
        this.patchList.add(new AbstractHorsePatch());
        this.patchList.add(new BlockPatch());
        this.patchList.add(new BlockSoulSandPatch());
        this.patchList.add(new KeyBindingPatch());
        this.patchList.add(new BlockModelRendererPatch());
        //this.patchList.add(new BlockFluidRendererPatch());
        this.patchList.add(new ActiveRenderInfoPatch());
        this.patchList.add(new BlockSlimePatch());
        this.patchList.add(new BlockLiquidPatch());
        this.patchList.add(new BlockStairsPatch());
        this.patchList.add(new BlockPanePatch());
        this.patchList.add(new BlockFencePatch());
        this.patchList.add(new EntityPatch());
        this.patchList.add(new AbstractClientPlayerPatch());
        this.patchList.add(new BiomeColorHelperPatch());
        this.patchList.add(new GuiBossOverlayPatch());
        this.patchList.add(new NetHandlerPlayClientPatch());
        this.patchList.add(new ChunkPatch());
        this.patchList.add(new GuiScreenPatch());
        this.patchList.add(new GuiChatPatch());

        //load custom external patches
        //TODO this needs more testing
        loadExternalPatches();
    }

    /**
     * This is where we load custom written patches from disk
     * This allows users to insert their own events or modify
     * the bytecode of any class loading during runtime
     */
    void loadExternalPatches() {
        try {
            //create a directory at "Seppuku/Patches"
            final File dir = new File("Seppuku/Patches");

            //if it doesnt exist create it
            if (!dir.exists()) {
                dir.mkdirs();
            }

            //all jars/zip files in the dir
            //loop though all classes within the jar/zip
            for (Class clazz : ReflectionUtil.getClassesEx(dir.getPath())) {
                if (clazz != null) {
                    //if we have found a class and the class inherits "ClassPatch"
                    if (ClassPatch.class.isAssignableFrom(clazz)) {
                        //create a new instance of the class
                        final ClassPatch patch = (ClassPatch) clazz.newInstance();

                        if (patch != null) {
                            //add the class to our list of patches
                            this.patchList.add(patch);
                            System.out.println("[Seppuku] Found external patch " + patch.getMcpName().replace(".", "/"));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Find a matching patch based on the input class name and our set environment
     *
     * @param name
     * @return
     */
    public ClassPatch findClassPatch(String name) {
        for (ClassPatch patch : this.patchList) {
            if (patch != null) {
                String patchName = patch.getMcpName();

                if (this.env == Environment.RELEASE) {
                    if (patch.getNotchName() != null && patch.getNotchName().length() > 0) {
                        patchName = patch.getNotchName();
                    }
                }

                if (name.equals(patchName)) {
                    return patch;
                }
            }
        }
        return null;
    }

    public List<ClassPatch> getPatchList() {
        return patchList;
    }

    public void setPatchList(List<ClassPatch> patchList) {
        this.patchList = patchList;
    }

    public Environment getEnv() {
        return env;
    }

    public void setEnv(Environment env) {
        this.env = env;
    }

    public enum Environment {
        IDE, RELEASE
    }

}
