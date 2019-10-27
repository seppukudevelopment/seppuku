package me.rigamortis.seppuku.api.util;

/**
 * Author Seth
 * 4/16/2019 @ 8:32 AM.
 */
public final class StringUtil {

    public static boolean isInt(String s) {
        try{
            Integer.parseInt(s);
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isFloat(String s) {
        try{
            Float.parseFloat(s);
            return true;
        }catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isDouble(String s) {
        try{
            Double.parseDouble(s);
            return true;
        }catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isBoolean(String s) {
        try{
            Boolean.parseBoolean(s);
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isLong(String s, int radix) {
        try{
            Long.parseLong(s, radix);
            return true;
        }catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isNumber(String s) {
        if(isInt(s) || isFloat(s) || isDouble(s) || isLong(s, 16)) {
            return true;
        }
        return false;
    }

    public static float similarityLength(String first, String second) {
        return (float)Math.abs(first.length() - second.length()) / 100;
    }

    public static boolean findMatching(String first, String second) {
        return second.toLowerCase().contains(first.toLowerCase());
    }

    /**
     * Credits https://stackoverflow.com/questions/955110/similarity-string-comparison-in-java
     * @param s1
     * @param s2
     * @return
     */
    public static double levenshteinDistance(String s1, String s2) {
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) {
            longer = s2; shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0) {
            return 1.0;
        }
        return (longerLength - editDistance(longer, shorter)) / (double) longerLength;
    }

    public static int editDistance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0)
                    costs[j] = j;
                else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1))
                            newValue = Math.min(Math.min(newValue, lastValue),
                                    costs[j]) + 1;
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0)
                costs[s2.length()] = lastValue;
        }
        return costs[s2.length()];
    }

}
