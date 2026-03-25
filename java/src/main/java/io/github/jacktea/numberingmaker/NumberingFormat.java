package io.github.jacktea.numberingmaker;

public enum NumberingFormat {
    DECIMAL("decimal"),
    UPPER_LETTER("upperLetter"),
    LOWER_LETTER("lowerLetter"),
    UPPER_ROMAN("upperRoman"),
    LOWER_ROMAN("lowerRoman"),
    ORDINAL("ordinal"),
    BULLET("bullet"),
    CHINESE_SIMPLIFIED("chineseSimplified"),
    CHINESE_TRADITIONAL("chineseTraditional");

    private final String value;

    NumberingFormat(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static NumberingFormat fromValue(String value) {
        for (NumberingFormat format : values()) {
            if (format.value.equals(value)) {
                return format;
            }
        }
        throw new IllegalArgumentException(value);
    }
}
