package me.rigamortis.seppuku.impl.config;

import com.google.gson.JsonObject;
import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.config.Configurable;
import me.rigamortis.seppuku.api.gui.hud.component.DraggableHudComponent;
import me.rigamortis.seppuku.api.util.FileUtil;
import me.rigamortis.seppuku.impl.gui.hud.anchor.AnchorPoint;

import java.io.File;

/**
 * @author noil
 */
public final class HudConfig extends Configurable {

    private DraggableHudComponent hudComponent;

    public HudConfig(File dir, DraggableHudComponent hudComponent) {
        super(FileUtil.createJsonFile(dir, hudComponent.getName()));
        this.hudComponent = hudComponent;
    }

    @Override
    public void onLoad() {
        super.onLoad();

        this.getJsonObject().entrySet().forEach(entry -> {
            switch (entry.getKey()) {
                case "X":
                    hudComponent.setX(entry.getValue().getAsFloat());
                    break;
                case "Y":
                    hudComponent.setY(entry.getValue().getAsFloat());
                    break;
                case "W":
                    hudComponent.setW(entry.getValue().getAsFloat());
                    break;
                case "H":
                    hudComponent.setH(entry.getValue().getAsFloat());
                    break;
                case "Visible":
                    hudComponent.setVisible(entry.getValue().getAsBoolean());
                    break;
                case "Anchor":
                    if (!entry.getValue().getAsString().equals("NONE")) {
                        for (AnchorPoint anchorPoint : Seppuku.INSTANCE.getHudManager().getAnchorPoints()) {
                            if (anchorPoint.getPoint().equals(AnchorPoint.Point.valueOf(entry.getValue().getAsString()))) {
                                hudComponent.setAnchorPoint(anchorPoint);
                            }
                        }
                    }
                    break;
                case "Glue":
                    if (!entry.getValue().getAsString().equals("NONE")) {
                        hudComponent.setGlued((DraggableHudComponent) Seppuku.INSTANCE.getHudManager().findComponent(entry.getValue().getAsString()));
                    }
                    break;
                case "GlueSide":
                    if (!entry.getValue().getAsString().equals("NONE")) {
                        hudComponent.setGlueSide(DraggableHudComponent.GlueSide.valueOf(entry.getValue().getAsString()));
                    }
                    break;
            }
        });
    }

    @Override
    public void onSave() {
        JsonObject componentsListJsonObject = new JsonObject();
        componentsListJsonObject.addProperty("Name", hudComponent.getName());
        componentsListJsonObject.addProperty("X", hudComponent.getX());
        componentsListJsonObject.addProperty("Y", hudComponent.getY());
        componentsListJsonObject.addProperty("W", hudComponent.getW());
        componentsListJsonObject.addProperty("H", hudComponent.getH());
        componentsListJsonObject.addProperty("Visible", hudComponent.isVisible());
        componentsListJsonObject.addProperty("Anchor", hudComponent.getAnchorPoint() == null ? "NONE" : hudComponent.getAnchorPoint().getPoint().name());
        componentsListJsonObject.addProperty("Glue", hudComponent.getGlued() == null ? "NONE" : hudComponent.getGlued().getName());
        componentsListJsonObject.addProperty("GlueSide", hudComponent.getGlued() == null ? "NONE" : hudComponent.getGlueSide().name());
        this.saveJsonObjectToFile(componentsListJsonObject);
    }
}
