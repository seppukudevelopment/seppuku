package me.rigamortis.seppuku.impl.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.config.Configurable;
import me.rigamortis.seppuku.api.gui.hud.component.DraggableHudComponent;
import me.rigamortis.seppuku.api.gui.hud.component.HudComponent;
import me.rigamortis.seppuku.api.util.FileUtil;
import me.rigamortis.seppuku.api.value.Value;
import me.rigamortis.seppuku.impl.gui.hud.anchor.AnchorPoint;

import java.io.File;
import java.util.Map;

/**
 * @author noil
 */
public final class HudConfig extends Configurable {

    private HudComponent hudComponent;

    public HudConfig(File dir, HudComponent hudComponent) {
        super(FileUtil.createJsonFile(dir, hudComponent.getName()));
        this.hudComponent = hudComponent;
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (this.hudComponent instanceof DraggableHudComponent) {
            final DraggableHudComponent draggableHudComponent = (DraggableHudComponent) this.hudComponent;
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
                    case "Locked":
                        draggableHudComponent.setLocked(entry.getValue().getAsBoolean());
                        break;
                    case "Anchor":
                        if (!entry.getValue().getAsString().equals("NONE")) {
                            for (AnchorPoint anchorPoint : Seppuku.INSTANCE.getHudManager().getAnchorPoints()) {
                                if (anchorPoint.getPoint().equals(AnchorPoint.Point.valueOf(entry.getValue().getAsString()))) {
                                    draggableHudComponent.setAnchorPoint(anchorPoint);
                                }
                            }
                        }
                        break;
                    case "Glue":
                        if (!entry.getValue().getAsString().equals("NONE")) {
                            draggableHudComponent.setGlued((DraggableHudComponent) Seppuku.INSTANCE.getHudManager().findComponent(entry.getValue().getAsString()));
                        }
                        break;
                    case "GlueSide":
                        if (!entry.getValue().getAsString().equals("NONE")) {
                            draggableHudComponent.setGlueSide(DraggableHudComponent.GlueSide.valueOf(entry.getValue().getAsString()));
                        }
                        break;

                }

                this.loadValues(entry);
            });
        } else {
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
                }

                this.loadValues(entry);
            });
        }
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

        if (hudComponent instanceof DraggableHudComponent) {
            DraggableHudComponent draggableHudComponent = (DraggableHudComponent) hudComponent;
            componentsListJsonObject.addProperty("Locked", draggableHudComponent.isLocked());
            componentsListJsonObject.addProperty("Anchor", draggableHudComponent.getAnchorPoint() == null ? "NONE" : draggableHudComponent.getAnchorPoint().getPoint().name());
            componentsListJsonObject.addProperty("Glue", draggableHudComponent.getGlued() == null ? "NONE" : draggableHudComponent.getGlued().getName());
            componentsListJsonObject.addProperty("GlueSide", draggableHudComponent.getGlued() == null ? "NONE" : ((DraggableHudComponent) hudComponent).getGlueSide().name());
        }

        if (hudComponent.getValueList().size() != 0) {
            hudComponent.getValueList().forEach(value -> {
                if (value.getValue() instanceof Boolean)
                    componentsListJsonObject.addProperty(value.getName(), (Boolean) value.getValue());
                else if (value.getValue() instanceof Number && !(value.getValue() instanceof Enum)) {
                    if (value.getValue().getClass() == Float.class) {
                        componentsListJsonObject.addProperty(value.getName(), (Float) value.getValue());
                    } else if (value.getValue().getClass() == Double.class) {
                        componentsListJsonObject.addProperty(value.getName(), (Double) value.getValue());
                    } else if (value.getValue().getClass() == Integer.class) {
                        componentsListJsonObject.addProperty(value.getName(), (Integer) value.getValue());
                    }
                } else if (value.getValue() instanceof Enum) {
                    componentsListJsonObject.addProperty(value.getName(), ((Enum) value.getValue()).name());
                }
            });
        }

        this.saveJsonObjectToFile(componentsListJsonObject);
    }

    private void loadValues(Map.Entry<String, JsonElement> entry) {
        for (Value val : hudComponent.getValueList()) {
            if (val.getName().equalsIgnoreCase(entry.getKey())) {
                if (val.getValue() instanceof Boolean) {
                    val.setValue(entry.getValue().getAsBoolean());
                } else if (val.getValue() instanceof Number && !(val.getValue() instanceof Enum)) {
                    if (val.getValue().getClass() == Float.class) {
                        val.setValue(entry.getValue().getAsFloat());
                    } else if (val.getValue().getClass() == Double.class) {
                        val.setValue(entry.getValue().getAsDouble());
                    } else if (val.getValue().getClass() == Integer.class) {
                        val.setValue(entry.getValue().getAsInt());
                    }
                } else if (val.getValue() instanceof Enum) {
                    val.setEnumValue(entry.getValue().getAsString());
                }
            }
        }
    }
}
