package com.munichre.bsmrv;

public class MrParseUtils {

    private static final String COMMA = ",";
    private static final String POINT = ".";
    private static final String PERCENT = "%";
    private static final String BAR = "-";

    private MrParseUtils() {
        throw new IllegalStateException(String.format("%s is not meant to be instantiated", this.getClass().getName()));
    }

    public static boolean parseBooleanFromInteger(String v) {
        try {
            final int n = Integer.parseInt(v);
            return n != 0;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("error interpreting %s as boolean", v));
        }
    }

    public static int parseInteger(String v) {
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("error interpreting %s as integer", v));
        }
    }

    public static double parseDouble(String v) {
        final String vWithPoint = correctDecimalSeparator(v.trim());

        if (vWithPoint.contains(PERCENT)) {
            //            final String withOutPercentSign = vWithPoint.substring(0, vWithPoint.length() - 2);
            final String withOutPercentSign = vWithPoint.substring(0, vWithPoint.length() - 1);
            if (withOutPercentSign.contains(PERCENT)) {
                throw new IllegalArgumentException(String.format(
                        "error interpreting %s as double in %; more than one % sign", v));
            }
            try {
                return Double.parseDouble(withOutPercentSign) / 100.0d;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(String.format("error interpreting %s as double in %", v));
            }

        } else if (vWithPoint.equals(BAR)) {
            return 0.0d;

        } else {
            try {
                return Double.parseDouble(vWithPoint);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(String.format("error interpreting %s as double", v));
            }
        }
    }

    private static String correctDecimalSeparator(String v) {
        //    if (v.contains(COMMA) && v.contains(POINT)) {
        //      throw new IllegalArgumentException(
        //          String.format("%s contains ',' and '.', cannot determine decimal separator", v));
        //    }
        //    return v.replace(COMMA, POINT);

        if (v.contains(COMMA) && v.contains(POINT)) {
            v = v.replace(POINT, "");
        }
        return v.replace(COMMA, POINT);
    }

}
