package me.rigamortis.seppuku.api.value;

/**
 * Author Seth
 * 4/17/2019 @ 5:59 AM.
 */
public class BooleanValue extends Value {

    public BooleanValue() {
    }

    public BooleanValue(String displayName, String[] alias, Object value) {
        super(displayName, alias, value);
    }

    public boolean getBoolean() {
        return (Boolean) this.getValue();
    }

    public void setBoolean(boolean val) {
        this.setValue(val);
    }

}
