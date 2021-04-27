package me.rigamortis.seppuku.impl.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.config.Configurable;
import me.rigamortis.seppuku.api.util.FileUtil;
import me.rigamortis.seppuku.impl.module.render.SearchModule;
import net.minecraft.block.Block;

import java.io.File;

/**
 * @author noil
 */
public final class SearchConfig extends Configurable {

    private final SearchModule searchModule;

    public SearchConfig(File dir) {
        super(FileUtil.createJsonFile(dir, "SearchIds"));
        this.searchModule = (SearchModule) Seppuku.INSTANCE.getModuleManager().find("Search");
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (this.searchModule == null)
            return;

        JsonArray searchIdsJsonArray = null;

        final JsonElement blockIds = this.getJsonObject().get("SearchBlockIds");
        if (blockIds != null)
            searchIdsJsonArray = blockIds.getAsJsonArray();

        final SearchModule searchModule = (SearchModule) Seppuku.INSTANCE.getModuleManager().find("Search");
        if (searchModule != null) {
            if (searchIdsJsonArray != null) {
                for (JsonElement jsonElement : searchIdsJsonArray) {
                    searchModule.add(jsonElement.getAsInt());
                }
            }
            if (searchModule.getBlockIds().getValue().isEmpty()) {
                searchModule.add("furnace");
            }
        }
    }

    @Override
    public void onSave() {
        if (this.searchModule == null)
            return;

        JsonObject save = new JsonObject();

        JsonArray searchIdsJsonArray = new JsonArray();
        for (Block block : this.searchModule.getBlockIds().getValue())
            searchIdsJsonArray.add(Block.getIdFromBlock(block));

        save.add("SearchBlockIds", searchIdsJsonArray);

        this.saveJsonObjectToFile(save);
    }
}
