package me.rigamortis.seppuku.api.value;

import javax.annotation.Nullable;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Regex {
    private String patternString;
    private Pattern pattern;

    public Regex() {
        this.setPatternString("");
    }

    public Regex(String patternString) {
        this.setPatternString(patternString);
    }

    public void setPatternString(String patternString) {
        this.patternString = patternString;
        if(patternString == "") {
            this.pattern = null;
            return;
        }

        try {
            this.pattern = Pattern.compile(patternString);
        } catch (PatternSyntaxException exception) {
            this.pattern = null;
        }
    }

    public String getPatternString() {
        return this.patternString;
    }

    @Nullable
    public Pattern getPattern() {
        return this.pattern;
    }

    @Override
    public String toString() {
        return this.patternString;
    }
}