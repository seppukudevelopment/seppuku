package me.rigamortis.seppuku.api.value;

/**
 * Author Seth
 * 4/17/2019 @ 5:58 AM.
 */
public class Value<T> {

    private String displayName;
    private String[] alias;
    private T value;
    private T type;

    private Value parent;

    public Value() {

    }

    public Value(String displayName, String[] alias, T value) {
        this.displayName = displayName;
        this.alias = alias;
        this.value = value;
    }

    public Value(String displayName, String[] alias, T value, T type) {
        this.displayName = displayName;
        this.alias = alias;
        this.value = value;
        this.type = type;
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

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public T getType() {
        return type;
    }

    public void setType(T type) {
        this.type = type;
    }

    public Value getParent() {
        return parent;
    }

    public void setParent(Value parent) {
        this.parent = parent;
    }
}
