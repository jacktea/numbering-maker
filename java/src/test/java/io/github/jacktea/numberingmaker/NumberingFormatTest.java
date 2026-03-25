package io.github.jacktea.numberingmaker;

import io.github.jacktea.numberingmaker.level.BulletNumberingLevel;
import io.github.jacktea.numberingmaker.level.ChineseNumberingLevel;
import io.github.jacktea.numberingmaker.level.DecimalNumberingLevel;
import io.github.jacktea.numberingmaker.level.LetterNumberingLevel;
import io.github.jacktea.numberingmaker.level.NumberingLevel;
import io.github.jacktea.numberingmaker.level.NumberingLevelFactory;
import io.github.jacktea.numberingmaker.level.OrdinalNumberingLevel;
import io.github.jacktea.numberingmaker.level.RomanNumberingLevel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class NumberingFormatTest {

    @Test
    void factoryCreatesConcreteLevelTypes() {
        NumberingLevelConfig config = new NumberingLevelConfig(1, 0, "%1.", null);

        NumberingLevel decimal = NumberingLevelFactory.createLevel(NumberingFormat.DECIMAL, config);
        NumberingLevel upperLetter = NumberingLevelFactory.createLevel(NumberingFormat.UPPER_LETTER, config);
        NumberingLevel lowerRoman = NumberingLevelFactory.createLevel(NumberingFormat.LOWER_ROMAN, config);
        NumberingLevel ordinal = NumberingLevelFactory.createLevel(NumberingFormat.ORDINAL, config);
        NumberingLevel bullet = NumberingLevelFactory.createLevel(NumberingFormat.BULLET, config);
        NumberingLevel chinese = NumberingLevelFactory.createLevel(NumberingFormat.CHINESE_TRADITIONAL, config);

        assertInstanceOf(DecimalNumberingLevel.class, decimal);
        assertInstanceOf(LetterNumberingLevel.class, upperLetter);
        assertInstanceOf(RomanNumberingLevel.class, lowerRoman);
        assertInstanceOf(OrdinalNumberingLevel.class, ordinal);
        assertInstanceOf(BulletNumberingLevel.class, bullet);
        assertInstanceOf(ChineseNumberingLevel.class, chinese);
    }

    @Test
    void conversions_coverTypicalAndBoundaryValues() {
        NumberingLevel upperLetter = NumberingLevelFactory.createLevel(NumberingFormat.UPPER_LETTER, new NumberingLevelConfig(26, 0, "%1", null));
        NumberingLevel lowerRoman = NumberingLevelFactory.createLevel(NumberingFormat.LOWER_ROMAN, new NumberingLevelConfig(58, 0, "%1", null));
        NumberingLevel ordinal = NumberingLevelFactory.createLevel(NumberingFormat.ORDINAL, new NumberingLevelConfig(11, 0, "%1", null));
        NumberingLevel chineseSimplified = NumberingLevelFactory.createLevel(NumberingFormat.CHINESE_SIMPLIFIED, new NumberingLevelConfig(10, 0, "%1", null));
        NumberingLevel chineseTraditional = NumberingLevelFactory.createLevel(NumberingFormat.CHINESE_TRADITIONAL, new NumberingLevelConfig(1234, 0, "%1", null));
        NumberingLevel bullet = NumberingLevelFactory.createLevel(NumberingFormat.BULLET, new NumberingLevelConfig(1, 0, "%1", null));

        assertEquals("Z", upperLetter.getValueLabel());
        assertEquals("lviii", lowerRoman.getValueLabel());
        assertEquals("11th", ordinal.getValueLabel());
        assertEquals("十", chineseSimplified.getValueLabel());
        assertEquals("壹仟貳佰叁拾肆", chineseTraditional.getValueLabel());
        assertEquals("", bullet.getValueLabel());
    }
}
