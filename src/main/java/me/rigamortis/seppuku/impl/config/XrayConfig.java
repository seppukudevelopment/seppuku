package me.rigamortis.seppuku.impl.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.config.Configurable;
import me.rigamortis.seppuku.api.util.FileUtil;
import me.rigamortis.seppuku.impl.module.render.XrayModule;
import net.minecraft.block.Block;

import java.io.File;

/**
 * @author noil
 */
public final class XrayConfig extends Configurable {

    private final XrayModule xrayModule;

    public XrayConfig(File dir) {
        super(FileUtil.createJsonFile(dir, "XrayIds"));
        this.xrayModule = (XrayModule) Seppuku.INSTANCE.getModuleManager().find("Xray");
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (this.xrayModule == null)
            return;

        JsonArray xrayIdsJsonArray = null;

        final JsonElement blockIds = this.getJsonObject().get("XrayBlockIds");
        if (blockIds != null)
            xrayIdsJsonArray = blockIds.getAsJsonArray();

        final XrayModule xrayModule = (XrayModule) Seppuku.INSTANCE.getModuleManager().find("Xray");
        if (xrayModule != null) {
            if (xrayIdsJsonArray != null) {
                for (JsonElement jsonElement : xrayIdsJsonArray) {
                    xrayModule.add(jsonElement.getAsInt());
                }
            }
            if (xrayModule.getBlocks().getValue().isEmpty()) {
                xrayModule.add("diamond_ore");
            }
        }
    }

    @Override
    public void onSave() {
        if (this.xrayModule == null)
            return;

        JsonObject save = new JsonObject();

        JsonArray xrayIdsJsonArray = new JsonArray();
        for (Block block : this.xrayModule.getBlocks().getValue())
            xrayIdsJsonArray.add(Block.getIdFromBlock(block));

        save.add("XrayBlockIds", xrayIdsJsonArray);

        this.saveJsonObjectToFile(save);
    }
}
