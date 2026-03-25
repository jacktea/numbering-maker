package io.github.jacktea.numberingmaker.level;

import io.github.jacktea.numberingmaker.NumberingFormat;
import io.github.jacktea.numberingmaker.NumberingLevelConfig;

public final class NumberingLevelFactory {
    private NumberingLevelFactory() {
    }

    public static NumberingLevel createLevel(NumberingFormat format, NumberingLevelConfig config) {
        NumberingLevel level = switch (format) {
            case DECIMAL -> new DecimalNumberingLevel();
            case UPPER_LETTER -> new LetterNumberingLevel(false);
            case LOWER_LETTER -> new LetterNumberingLevel(true);
            case UPPER_ROMAN -> new RomanNumberingLevel(false);
            case LOWER_ROMAN -> new RomanNumberingLevel(true);
            case ORDINAL -> new OrdinalNumberingLevel();
            case BULLET -> new BulletNumberingLevel();
            case CHINESE_SIMPLIFIED -> new ChineseNumberingLevel(false);
            case CHINESE_TRADITIONAL -> new ChineseNumberingLevel(true);
        };
        level.setFormat(format);
        if (config != null) {
            level.updateConfig(config);
        }
        return level;
    }
}
