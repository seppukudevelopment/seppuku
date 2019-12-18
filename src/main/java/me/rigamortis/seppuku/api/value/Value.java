package me.rigamortis.seppuku.api.value;

/**
 * Author Seth
 * 11/18/2019 @ 8:56 PM.
 */
public class Value<T> {

    private String name;
    private String[] alias;
    private String desc;

    private T value;

    private T min;
    private T max;
    private T inc;

    public Value(String name, String[] alias, String desc) {
        this.name = name;
        this.alias = alias;
        this.desc = desc;
    }

    public Value(String name, String[] alias, String desc, T value) {
        this.name = name;
        this.alias = alias;
        this.desc = desc;
        this.value = value;
    }

    public Value(String name, String[] alias, String desc, T value, T min, T max, T inc) {
        this.name = name;
        this.alias = alias;
        this.desc = desc;
        this.value = value;
        this.min = min;
        this.max = max;
        this.inc = inc;
    }

    public <T> T clamp(T value, T min, T max) {
        return ((Comparable) value).compareTo(min) < 0 ? min : (((Comparable) value).compareTo(max) > 0 ? max : value);
    }

    public T getValue() {
        return this.value;
    }

    public void setValue(T value) {
        if (min != null && max != null) {
            final Number val = (Number) value;
            final Number min = (Number) this.min;
            final Number max = (Number) this.max;
            this.value = (T) val;
            //this.value = (T) this.clamp(val, min, max);
        } else {
            this.value = value;
        }
    }

    public int getEnum(String input) {
        for (int i = 0; i < this.value.getClass().getEnumConstants().length; i++) {
            final Enum e = (Enum) this.value.getClass().getEnumConstants()[i];
            if (e.name().equalsIgnoreCase(input)) {
                return i;
            }
        }
        return -1;
    }

    public void setEnumValue(String value) {
        for (Enum e : ((Enum) this.value).getClass().getEnumConstants()) {
            if (e.name().equalsIgnoreCase(value)) {
                this.value = (T) e;
            }
        }
    }

    public T getMin() {
        return min;
    }

    public void setMin(T min) {
        this.min = min;
    }

    public T getMax() {
        return max;
    }

    public void setMax(T max) {
        this.max = max;
    }

    public T getInc() {
        return inc;
    }

    public void setInc(T inc) {
        this.inc = inc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}

