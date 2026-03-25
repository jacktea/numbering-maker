package io.github.jacktea.numberingmaker.level;

public class RomanNumberingLevel extends NumberingLevel {
    private static final int[] VALUES = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
    private static final String[] UPPER_SYMBOLS = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
    private static final String[] LOWER_SYMBOLS = {"m", "cm", "d", "cd", "c", "xc", "l", "xl", "x", "ix", "v", "iv", "i"};

    private final boolean lowerCase;

    public RomanNumberingLevel(boolean lowerCase) {
        this.lowerCase = lowerCase;
    }

    @Override
    public String getValueLabel() {
        int num = getValue();
        String[] symbols = lowerCase ? LOWER_SYMBOLS : UPPER_SYMBOLS;
        StringBuilder roman = new StringBuilder();
        for (int i = 0; i < VALUES.length; i++) {
            while (num >= VALUES[i]) {
                num -= VALUES[i];
                roman.append(symbols[i]);
            }
        }
        return roman.toString();
    }
}
