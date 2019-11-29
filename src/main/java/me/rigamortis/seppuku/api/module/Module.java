package me.rigamortis.seppuku.api.module;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.value.old.*;

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

    public Module(String displayName, String[] alias, String desc, String key, int color, boolean hidden, boolean enabled, ModuleType type) {
        this.displayName = displayName;
        this.alias = alias;
        this.desc = desc;
        this.key = key;
        this.color = color;
        this.hidden = hidden;
        this.enabled = enabled;
        this.type = type;
    }

    public Module(String displayName, String[] alias, String desc, String key, int color, ModuleType type) {
        this.displayName = displayName;
        this.alias = alias;
        this.desc = desc;
        this.key = key;
        this.color = color;
        this.type = type;
    }

    public Module(String displayName, String[] alias, String key, int color, ModuleType type) {
        this.displayName = displayName;
        this.alias = alias;
        this.key = key;
        this.color = color;
        this.type = type;
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
        if(this.isEnabled()) {
            this.onEnable();
        }else{
            this.onDisable();
        }
        this.onToggle();
    }

    public String getMetaData() {
        return null;
    }

    public String toUsageString() {
        if(this.valueList.size() <= 0) {
            return null;
        }

        final StringBuilder sb = new StringBuilder();

        for(Value v : this.getValueList()) {
            if(v instanceof BooleanValue) {
                sb.append(v.getDisplayName() + "\n");
            }
            if(v instanceof NumberValue && !(v instanceof OptionalValue)) {
                sb.append(v.getDisplayName() + " <Amount>\n");
            }
            if(v instanceof StringValue) {
                sb.append(v.getDisplayName() + " <String>\n");
            }
            if(v instanceof OptionalValue) {
                final OptionalValue val = (OptionalValue) v;

                final StringBuilder options = new StringBuilder();

                final int size = val.getOptions().length;

                for(int i = 0; i < val.getOptions().length; i++) {
                    final String option = val.getOptions()[i];

                    options.append(option + ((i == size - 1) ? "" : "|"));
                }

                sb.append(v.getDisplayName() + " <" + options.toString() + ">\n");
            }
        }

        final String s = sb.toString();

        return s.substring(0, s.length() - 1);
    }

    public Value find(String alias) {
        for(Value v : this.getValueList()) {
            for(String s : v.getAlias()) {
                if(alias.equalsIgnoreCase(s)) {
                    return v;
                }
            }
            if(v.getDisplayName().equalsIgnoreCase(alias)) {
                return v;
            }
        }
        return null;
    }

    public void unload() {
        this.valueList.clear();
    }

    public enum ModuleType {
        COMBAT, MOVEMENT, RENDER, PLAYER, WORLD, MISC, HIDDEN, UI
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String[] getAlias() {
        return alias;
    }

    public void setAlias(String[] alias) {
        this.alias = alias;
    }

    public String getDesc() {
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
}
