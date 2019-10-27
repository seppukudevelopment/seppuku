package me.rigamortis.seppuku.api.util;

/**
 * Author Seth
 * 4/29/2019 @ 1:26 AM.
 */
public final class Timer {

    private long time;

    public Timer() {
        time = -1;
    }

    public boolean passed(double ms) {
        return System.currentTimeMillis() - this.time >= ms;
    }

    public void reset() {
        this.time = System.currentTimeMillis();
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
