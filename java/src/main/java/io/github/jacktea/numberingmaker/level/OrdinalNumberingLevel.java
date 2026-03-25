package io.github.jacktea.numberingmaker.level;

public class OrdinalNumberingLevel extends NumberingLevel {
    @Override
    public String getValueLabel() {
        int value = getValue();
        int mod100 = value % 100;
        if (mod100 >= 11 && mod100 <= 13) {
            return value + "th";
        }
        return switch (value % 10) {
            case 1 -> value + "st";
            case 2 -> value + "nd";
            case 3 -> value + "rd";
            default -> value + "th";
        };
    }
}
