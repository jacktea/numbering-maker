package io.github.jacktea.numberingmaker.level;

public class DecimalNumberingLevel extends NumberingLevel {
    @Override
    public String getValueLabel() {
        return String.valueOf(getValue());
    }
}
