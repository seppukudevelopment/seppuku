package me.rigamortis.seppuku.api.value;

/**
 * Author Seth
 * 4/17/2019 @ 6:05 AM.
 */
public class StringValue extends Value {

    public StringValue() {
    }

    public StringValue(String displayName, String[] alias, Object value) {
        super(displayName, alias, value);
    }

    public String getString() {
        return (String) this.getValue();
    }

    public void setString(String val) {
        this.setValue(val);
    }

}
