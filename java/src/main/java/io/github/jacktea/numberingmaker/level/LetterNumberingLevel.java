package io.github.jacktea.numberingmaker.level;

public class LetterNumberingLevel extends NumberingLevel {
    private final boolean lowerCase;

    public LetterNumberingLevel(boolean lowerCase) {
        this.lowerCase = lowerCase;
    }

    @Override
    public String getValueLabel() {
        char base = lowerCase ? 'a' : 'A';
        int value = getValue();
        StringBuilder label = new StringBuilder();
        while (value > 0) {
            value--;
            int remainder = value % 26;
            label.insert(0, (char) (remainder + base));
            value = value / 26;
        }
        return label.toString();
    }
}
