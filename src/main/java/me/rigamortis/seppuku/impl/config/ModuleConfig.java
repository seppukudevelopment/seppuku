package me.rigamortis.seppuku.impl.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.rigamortis.seppuku.api.config.Configurable;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.FileUtil;
import me.rigamortis.seppuku.api.value.Regex;
import me.rigamortis.seppuku.api.value.Shader;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.item.Item;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author noil
 */
public class ModuleConfig extends Configurable {

    private final Module module;

    public ModuleConfig(File dir, Module module) {
        super(FileUtil.createJsonFile(dir, module.getDisplayName()));
        this.module = module;
    }

    @Override
    public void onLoad(JsonObject jsonObject) {
        super.onLoad(jsonObject);

        this.getJsonObject().entrySet().forEach(entry -> {
            if (entry.getKey().equalsIgnoreCase("Color")) {
                module.setColor((int) Long.parseLong(entry.getValue().getAsString(), 16));
            }

            if (entry.getKey().equalsIgnoreCase("Hidden")) {
                module.setHidden(entry.getValue().getAsBoolean());
            }

            if (entry.getKey().equalsIgnoreCase("Keybind")) {
                module.setKey(entry.getValue().getAsString());
            }

            if (entry.getKey().equalsIgnoreCase("Name")) {
                module.setDisplayName(entry.getValue().getAsString());
            }

            // Check if we are already enabled
            if (entry.getKey().equalsIgnoreCase("Enabled") && !module.isEnabled() && module.getType() != Module.ModuleType.HIDDEN) {
                if (entry.getValue().getAsBoolean()) {
                    module.toggle();
                }
            }

            for (Value val : module.getValueList()) {
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
                    } else if (val.getValue() instanceof String && !(val.getValue() instanceof Enum)) {
                        val.setValue(entry.getValue().getAsString());
                    } else if (val.getValue() instanceof Enum) {
                        val.setEnumValue(entry.getValue().getAsString());
                    } else if (val.getValue() instanceof Color) {
                        val.setValue(new Color((int) Long.parseLong(entry.getValue().getAsString(), 16)));
                    } else if (val.getValue() instanceof Regex) {
                        val.setValue(new Regex(entry.getValue().getAsString()));
                    } else if (val.getValue() instanceof Shader) {
                        val.setValue(new Shader(entry.getValue().getAsString()));
                    } else if (val.getValue() instanceof List) {
                        List<?> unknownList = (List<?>) val.getValue();
                        if (unknownList.stream().allMatch(o -> o instanceof Item)) {
                            List<Item> itemList = new ArrayList<>();
                            JsonArray unknownArray = (JsonArray) entry.getValue();
                            unknownArray.forEach(jsonElement -> {
                                itemList.add(Item.getItemById(jsonElement.getAsInt()));
                            });
                            val.setValue(itemList);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onSave() {
        JsonObject moduleJsonObject = new JsonObject();
        moduleJsonObject.addProperty("Name", module.getDisplayName());
        moduleJsonObject.addProperty("Color", Integer.toHexString(module.getColor()).toUpperCase());
        moduleJsonObject.addProperty("Hidden", module.isHidden());
        moduleJsonObject.addProperty("Keybind", (module.getKey() != null) ? module.getKey() : "NONE");
        moduleJsonObject.addProperty("Enabled", module.isEnabled());
        if (module.getValueList().size() != 0) {
            module.getValueList().forEach(value -> {
                if (value.getValue() instanceof Boolean)
                    moduleJsonObject.addProperty(value.getName(), (Boolean) value.getValue());
                else if (value.getValue() instanceof Number && !(value.getValue() instanceof Enum)) {
                    if (value.getValue().getClass() == Float.class) {
                        moduleJsonObject.addProperty(value.getName(), (Float) value.getValue());
                    } else if (value.getValue().getClass() == Double.class) {
                        moduleJsonObject.addProperty(value.getName(), (Double) value.getValue());
                    } else if (value.getValue().getClass() == Integer.class) {
                        moduleJsonObject.addProperty(value.getName(), (Integer) value.getValue());
                    }
                } else if (value.getValue() instanceof String && !(value.getValue() instanceof Enum)) {
                    moduleJsonObject.addProperty(value.getName(), (String) value.getValue());
                } else if (value.getValue() instanceof Enum) {
                    moduleJsonObject.addProperty(value.getName(), ((Enum) value.getValue()).name());
                } else if (value.getValue() instanceof Color) {
                    moduleJsonObject.addProperty(value.getName(), Integer.toHexString(((Color) value.getValue()).getRGB()).toUpperCase());
                } else if (value.getValue() instanceof Regex) {
                    moduleJsonObject.addProperty(value.getName(), ((Regex) value.getValue()).getPatternString());
                } else if (value.getValue() instanceof Shader) {
                    moduleJsonObject.addProperty(value.getName(), ((Shader) value.getValue()).getShaderID());
                } else if (value.getValue() instanceof List) {
                    List<?> unknownList = (List<?>) value.getValue();
                    if (unknownList.stream().allMatch(o -> o instanceof Item)) {
                        List<Item> itemList = (List<Item>) unknownList;
                        JsonArray itemsJsonArray = new JsonArray();
                        itemList.forEach(item -> {
                            itemsJsonArray.add(Item.getIdFromItem(item));
                        });
                        moduleJsonObject.add(value.getName(), itemsJsonArray);
                    }
                }
            });
        }
        this.saveJsonObjectToFile(moduleJsonObject);
    }

    public Module getModule() {
        return module;
    }
}
