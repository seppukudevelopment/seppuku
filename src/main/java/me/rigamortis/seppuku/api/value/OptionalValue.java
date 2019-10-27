package me.rigamortis.seppuku.api.value;

/**
 * Author Seth
 * 4/17/2019 @ 6:00 AM.
 */
public class OptionalValue extends NumberValue {

    private String[] options;

    public OptionalValue(String[] options) {
        this.options = options;
    }

    public OptionalValue(String displayName, String[] alias, Object value, String[] options) {
        super(displayName, alias, value);
        this.options = options;
    }

    public OptionalValue(String displayName, String[] alias, Object value, Object type, Object min, Object max, Object increment, String[] options) {
        super(displayName, alias, value, type, min, max, increment);
        this.options = options;
    }

    public int getOption(String input) {
        for(int i = 0; i < this.getOptions().length; i++) {
            final String s = this.getOptions()[i];
            if(input.equalsIgnoreCase(s)) {
                return i;
            }
        }
        return -1;
    }

    public String getSelectedOption() {
        return this.getOptions()[this.getInt()];
    }

    public String[] getOptions() {
        return options;
    }

    public void setOptions(String[] options) {
        this.options = options;
    }
}
