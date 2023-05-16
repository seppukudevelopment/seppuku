package me.rigamortis.seppuku.impl.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.config.Configurable;
import me.rigamortis.seppuku.api.util.FileUtil;
import me.rigamortis.seppuku.impl.module.world.NukerModule;
import net.minecraft.block.Block;

import java.io.File;

/**
 * @author Old Chum
 * @since 3/30/2023
 */
public class NukerFilterConfig extends Configurable {
    private final NukerModule nukerModule;

    public NukerFilterConfig(File dir) {
        super(FileUtil.createJsonFile(dir, "NukerFilter"));
        this.nukerModule = (NukerModule) Seppuku.INSTANCE.getModuleManager().find("Nuker");
    }

    @Override
    public void onLoad(JsonObject jsonObject) {
        super.onLoad(jsonObject);

        if (this.nukerModule == null)
            return;

        JsonArray xrayIdsJsonArray = null;

        final JsonElement blockIds = this.getJsonObject().get("NukerFilterIds");
        if (blockIds != null)
            xrayIdsJsonArray = blockIds.getAsJsonArray();

        final NukerModule nukerModule = (NukerModule) Seppuku.INSTANCE.getModuleManager().find("Nuker");
        if (nukerModule != null) {
            if (xrayIdsJsonArray != null) {
                for (JsonElement jsonElement : xrayIdsJsonArray) {
                    nukerModule.add(jsonElement.getAsInt());
                }
            }
        }
    }

    @Override
    public void onSave() {
        if (this.nukerModule == null)
            return;

        JsonObject save = new JsonObject();

        JsonArray xrayIdsJsonArray = new JsonArray();
        for (Block block : this.nukerModule.getFilter().getValue())
            xrayIdsJsonArray.add(Block.getIdFromBlock(block));

        save.add("NukerFilterIds", xrayIdsJsonArray);

        this.saveJsonObjectToFile(save);
    }
}
