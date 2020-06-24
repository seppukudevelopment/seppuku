package me.rigamortis.seppuku.impl.fml.core;

import me.rigamortis.seppuku.impl.management.PatchManager;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Author Seth
 * 4/5/2019 @ 1:24 AM.
 */
@IFMLLoadingPlugin.TransformerExclusions(value = "me.rigamortis.seppuku.impl.fml.core")
@IFMLLoadingPlugin.MCVersion(value = "1.12.2")
public final class SeppukuLoadingPlugin implements IFMLLoadingPlugin {

    @Override
    public String[] getASMTransformerClass() {
        return new String[] {
                SeppukuClassTransformer.class.getName()
        };
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        final boolean runtimeDeobfuscationEnabled =
                (boolean) data.getOrDefault("runtimeDeobfuscationEnabled", true);
        SeppukuClassTransformer.PATCH_MANAGER = new PatchManager(!runtimeDeobfuscationEnabled);
    }

    @Override
    public String getAccessTransformerClass() {
        return SeppukuAccessTransformer.class.getName();
    }
}
