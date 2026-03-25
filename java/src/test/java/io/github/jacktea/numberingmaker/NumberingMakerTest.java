package io.github.jacktea.numberingmaker;

import io.github.jacktea.numberingmaker.level.NumberingLevelFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class NumberingMakerTest {

    @Test
    void createMultilevelNumberingMaker_hasNineLevels() {
        NumberingMaker maker = NumberingMaker.createMultilevelNumberingMaker(1);

        assertEquals(9, maker.getLevels().size());
        assertEquals(MultiLevelType.MULTI_LEVEL, maker.getMultiLevelType());
    }

    @Test
    void generateNumbering_andRestoreState_followExpectedSequence() {
        NumberingMaker maker1 = NumberingMaker.createMultilevelNumberingMaker(1);

        assertEquals("1.", maker1.generateNumbering(0));
        assertEquals("1.1.", maker1.generateNumbering(1));
        assertEquals("1.2.", maker1.generateNumbering(1));
        assertEquals("1.2.1.", maker1.generateNumbering(2));
        assertEquals("1.2.2.", maker1.generateNumbering(2));

        NumberingMakerState state = maker1.getState();
        assertNotNull(state);
        assertEquals(9, state.getLevelStates().size());

        NumberingMaker maker2 = NumberingMaker.createMultilevelNumberingMaker(1);
        maker2.setState(state);

        assertEquals("1.2.3.", maker1.generateNumbering(2));
        assertEquals("1.2.3.", maker2.generateNumbering(2));
        assertEquals("1.3.", maker1.generateNumbering(1));
        assertEquals("1.3.", maker2.generateNumbering(1));
        assertEquals("1.3.1.", maker1.generateNumbering(2));
        assertEquals("1.3.1.", maker2.generateNumbering(2));
    }

    @Test
    void generateNumbering_outOfRange_returnsEmptyString() {
        NumberingMaker maker = NumberingMaker.createMultilevelNumberingMaker(1);

        assertEquals("", maker.generateNumbering(-1));
        assertEquals("", maker.generateNumbering(9));
    }

    @Test
    void rowNumberRollbackSequence_matchesExpectedPattern() {
        NumberingMaker maker = NumberingMaker.createMultilevelNumberingMaker(1);

        assertEquals("1.", maker.generateNumbering(0));
        assertEquals("1.1.", maker.generateNumbering(1));
        assertEquals("1.2.", maker.generateNumbering(1));
        assertEquals("1.2.1.", maker.generateNumbering(2));
        assertEquals("1.2.2.", maker.generateNumbering(2));

        assertEquals("1.2.3.", maker.generateNumbering(2));
        assertEquals("1.3.", maker.generateNumbering(1));
        assertEquals("1.3.1.", maker.generateNumbering(2));
    }

    @Test
    void readmeMixedEncoderExample_producesExpectedSequence() {
        NumberingMaker maker = NumberingMaker.createMultilevelNumberingMaker(1);

        maker.getLevels().set(0, NumberingLevelFactory.createLevel(
            NumberingFormat.DECIMAL,
            new NumberingLevelConfig(1, 0, "%1.", null)
        ));
        maker.getLevels().set(1, NumberingLevelFactory.createLevel(
            NumberingFormat.UPPER_ROMAN,
            new NumberingLevelConfig(1, 1, "%1.%2.", null)
        ));
        maker.getLevels().set(2, NumberingLevelFactory.createLevel(
            NumberingFormat.CHINESE_SIMPLIFIED,
            new NumberingLevelConfig(1, 2, "%1.%2.%3.", null)
        ));

        assertEquals("1.", maker.generateNumbering(0));
        assertEquals("1.I.", maker.generateNumbering(1));
        assertEquals("1.II.", maker.generateNumbering(1));
        assertEquals("1.II.一.", maker.generateNumbering(2));
        assertEquals("1.II.二.", maker.generateNumbering(2));
    }
}
