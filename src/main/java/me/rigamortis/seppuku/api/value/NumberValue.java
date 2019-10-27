package me.rigamortis.seppuku.api.value;

/**
 * Author Seth
 * 4/17/2019 @ 5:59 AM.
 */
public class NumberValue<T> extends Value {

    private T min;
    private T max;
    private T increment;

    public NumberValue() {
    }

    public NumberValue(String displayName, String[] alias, Object value) {
        super(displayName, alias, value);
    }

    public NumberValue(String displayName, String[] alias, Object value, Object type, T min, T max, T increment) {
        super(displayName, alias, value, type);
        this.min = min;
        this.max = max;
        this.increment = increment;
    }

    public float getFloat() {
        return (Float) this.getValue();
    }

    public void setFloat(float val) {
        this.setValue(val);
    }

    public int getInt() {
        return (Integer) this.getValue();
    }

    public void setInt(int val) {
        this.setValue(val);
    }

    public double getDouble() {
        return (Double) this.getValue();
    }

    public void setDouble(double val) {
        this.setValue(val);
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

    public T getIncrement() {
        return increment;
    }

    public void setIncrement(T increment) {
        this.increment = increment;
    }
}