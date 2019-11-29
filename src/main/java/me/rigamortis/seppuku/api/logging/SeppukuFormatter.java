package me.rigamortis.seppuku.api.logging;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Author Seth
 * 4/4/2019 @ 10:46 PM.
 */
public final class SeppukuFormatter extends Formatter {

    public String format(LogRecord record) {
        final StringBuilder sb = new StringBuilder();
        sb.append("[" + new SimpleDateFormat("HH.mm.ss").format(new Date()) + "] ");
        sb.append("[Seppuku]: ");
        sb.append(formatMessage(record));
        sb.append("\n");
        return sb.toString();
    }

}