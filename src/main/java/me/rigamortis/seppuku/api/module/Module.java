package me.rigamortis.seppuku.api.module;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.value.Regex;
import me.rigamortis.seppuku.api.value.Shader;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.HoverEvent;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Author Seth
 * 4/7/2019 @ 10:02 PM.
 */
public class Module {

    private String displayName;
    private String[] alias;
    private String desc;
    private String key;
    private int color;
    private boolean hidden;
    private boolean enabled;
    private ModuleType type;

    private List<Value> valueList = new ArrayList<Value>();

    public Module() {

    }

    public Module(String displayName, String[] alias, String key, int color, ModuleType type) {
        this.displayName = displayName;
        this.alias = alias;
        this.key = key;
        this.color = color;
        this.type = type;
    }

    public Module(String displayName, String[] alias, String desc, String key, int color, ModuleType type) {
        this(displayName, alias, key, color, type);
        this.desc = desc;
    }

    public Module(String displayName, String[] alias, String desc, String key, int color, boolean hidden, boolean enabled, ModuleType type) {
        this(displayName, alias, desc, key, color, type);
        this.hidden = hidden;
        this.enabled = enabled;
    }

    public void onEnable() {
        Seppuku.INSTANCE.getEventManager().addEventListener(this);
    }

    public void onDisable() {
        Seppuku.INSTANCE.getEventManager().removeEventListener(this);
    }

    public void onToggle() {

    }

    public void toggle() {
        this.setEnabled(!this.isEnabled());
        if (this.isEnabled()) {
            this.onEnable();
        } else {
            this.onDisable();
        }
        this.onToggle();
    }

    public String getMetaData() {
        return null;
    }

    public TextComponentString toUsageTextComponent() {
        if (this.valueList.size() <= 0) {
            return null;
        }

        final String valuePrefix = " " + ChatFormatting.RESET;
        final TextComponentString msg = new TextComponentString("");
        final DecimalFormat df = new DecimalFormat("#.##");

        for (Value v : this.getValueList()) {
            if (v.getValue() instanceof Boolean) {
                msg.appendSibling(new TextComponentString(valuePrefix + v.getName() + ": " + ((Boolean) v.getValue() ? ChatFormatting.GREEN : ChatFormatting.RED) + v.getValue()).setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(v.getName() + "\n" + ChatFormatting.GOLD + ((v.getDesc() == null || v.getDesc().equals("")) ? "There is no description for this boolean value." : v.getDesc()) + ChatFormatting.RESET + "\n " + ChatFormatting.GRAY + "<true / false>")))));
            }

            if (v.getValue() instanceof Number && !(v.getValue() instanceof Enum)) {
                msg.appendSibling(new TextComponentString(valuePrefix + v.getName() + ChatFormatting.GRAY + " <amount>" + ChatFormatting.RESET + ": " + ChatFormatting.AQUA + (df.format(v.getValue()))).setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(v.getName() + "\n" + ChatFormatting.GOLD + ((v.getDesc() == null || v.getDesc().equals("")) ? "There is no description for this number value." : v.getDesc()) + ChatFormatting.RESET + "\n " + ChatFormatting.GRAY + "<" + v.getMin() + " - " + v.getMax() + ">")))));
            }

            if (v.getValue() instanceof String) {
                msg.appendSibling(new TextComponentString(valuePrefix + v.getName() + ChatFormatting.GRAY + " <text>" + ChatFormatting.RESET + ": " + v.getValue()).setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(v.getName() + "\n" + ChatFormatting.GOLD + ((v.getDesc() == null || v.getDesc().equals("")) ? "There is no description for this string value." : v.getDesc()) + ChatFormatting.RESET + "\n " + ChatFormatting.GRAY + "<text>")))));
            }

            if (v.getValue() instanceof Enum) {
                final Enum val = (Enum) v.getValue();
                final StringBuilder options = new StringBuilder();
                final int size = val.getClass().getEnumConstants().length;

                for (int i = 0; i < size; i++) {
                    final Enum option = val.getClass().getEnumConstants()[i];
                    options.append(option.name().toLowerCase() + ((i == size - 1) ? "" : ", "));
                }

                msg.appendSibling(new TextComponentString(valuePrefix + v.getName() + ChatFormatting.GRAY + " <" + options + ">" + ChatFormatting.RESET + ": " + ChatFormatting.YELLOW + val.name().toLowerCase()).setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(v.getName() + "\n" + ChatFormatting.GOLD + ((v.getDesc() == null || v.getDesc().equals("")) ? "There is no description for this enum value." : v.getDesc()) + ChatFormatting.RESET + "\n " + ChatFormatting.GRAY + "<" + options + ">")))));
            }

            if (v.getValue() instanceof Regex) {
                msg.appendSibling(new TextComponentString(valuePrefix + v.getName() + ChatFormatting.GRAY + " <regex>" + ChatFormatting.RESET + ": " + v.getValue()).setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(v.getName() + "\n" + ChatFormatting.GOLD + ((v.getDesc() == null || v.getDesc().equals("")) ? "There is no description for this regular expression value." : v.getDesc()) + ChatFormatting.RESET + "\n " + ChatFormatting.GRAY + "<regex>")))));
            }

            if (v.getValue() instanceof Shader) {
                msg.appendSibling(new TextComponentString(valuePrefix + v.getName() + ChatFormatting.GRAY + " <shader>" + ChatFormatting.RESET + ": " + v.getValue()).setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(v.getName() + "\n" + ChatFormatting.GOLD + ((v.getDesc() == null || v.getDesc().equals("")) ? "There is no description for this shader ID value." : v.getDesc()) + ChatFormatting.RESET + "\n " + ChatFormatting.GRAY + "<shader>")))));
            }
        }

        return msg;
    }

    public Value findValue(String alias) {
        for (Value v : this.getValueList()) {
            for (String s : v.getAlias()) {
                if (alias.equalsIgnoreCase(s)) {
                    return v;
                }
            }

            if (v.getName().equalsIgnoreCase(alias)) {
                return v;
            }
        }
        return null;
    }

    public void unload() {
        this.valueList.clear();
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        if (displayName.equals("true") || displayName.equals("false")) { // bug in earlier versions of seppuku <= 3.0.6
            this.displayName = this.getAlias()[0];
            return;
        }
        this.displayName = displayName;
    }

    public String[] getAlias() {
        return alias;
    }

    public void setAlias(String[] alias) {
        this.alias = alias;
    }

    public String getDesc() {
        if (this.desc == null) {
            return "No description to be found.";
        }
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public ModuleType getType() {
        return type;
    }

    public void setType(ModuleType type) {
        this.type = type;
    }

    public List<Value> getValueList() {
        return valueList;
    }

    public void setValueList(List<Value> valueList) {
        this.valueList = valueList;
    }

    public enum ModuleType {
        COMBAT, MOVEMENT, RENDER, PLAYER, WORLD, MISC, HIDDEN, UI
    }
}
