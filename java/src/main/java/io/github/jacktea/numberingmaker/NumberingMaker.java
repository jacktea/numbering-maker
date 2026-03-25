package io.github.jacktea.numberingmaker;

import io.github.jacktea.numberingmaker.level.DecimalNumberingLevel;
import io.github.jacktea.numberingmaker.level.NumberingLevel;

import java.util.ArrayList;
import java.util.List;

public class NumberingMaker {
    private int numId;
    private MultiLevelType multiLevelType;
    private List<NumberingLevel> levels = new ArrayList<>();

    public int getNumId() {
        return numId;
    }

    public void setNumId(int numId) {
        this.numId = numId;
    }

    public MultiLevelType getMultiLevelType() {
        return multiLevelType;
    }

    public void setMultiLevelType(MultiLevelType multiLevelType) {
        this.multiLevelType = multiLevelType;
    }

    public List<NumberingLevel> getLevels() {
        return levels;
    }

    public void setLevels(List<NumberingLevel> levels) {
        this.levels = levels;
    }

    public String generateNumbering(int level) {
        int size = levels.size();
        if (level >= size || level < 0) {
            return "";
        }
        String[] values = new String[size];
        for (int i = 0; i < size; i++) {
            values[i] = levels.get(i).getValueLabel();
        }
        NumberingLevel numberingLevel = levels.get(level);
        String result = numberingLevel.generateNumber(values);
        if (numberingLevel.isChanged()) {
            resetAfterLevel(level + 1);
        }
        return result;
    }

    public NumberingLevel getLevel(int level) {
        if (level >= 0 && level < levels.size()) {
            return levels.get(level);
        }
        return null;
    }

    public NumberingMakerState getState() {
        NumberingMakerState state = new NumberingMakerState();
        List<NumberingMakerState.NumberingLevelState> stateList = new ArrayList<>();
        for (NumberingLevel level : levels) {
            stateList.add(level.getState());
        }
        state.setLevelStates(stateList);
        return state;
    }

    public void setState(NumberingMakerState state) {
        if (state == null || state.getLevelStates() == null) {
            return;
        }
        List<NumberingMakerState.NumberingLevelState> stateList = state.getLevelStates();
        for (int i = 0; i < Math.min(levels.size(), stateList.size()); i++) {
            levels.get(i).setState(stateList.get(i));
        }
    }

    public static NumberingMaker createMultilevelNumberingMaker(int numId) {
        NumberingMaker numberingMaker = new NumberingMaker();
        numberingMaker.setNumId(numId);
        numberingMaker.setMultiLevelType(MultiLevelType.MULTI_LEVEL);
        String pattern = "";
        for (int i = 0; i < 9; i++) {
            DecimalNumberingLevel level = new DecimalNumberingLevel();
            level.setFormat(NumberingFormat.DECIMAL);
            level.setLevel(i);
            level.setStart(1);
            pattern = pattern + "%" + (i + 1) + ".";
            level.setPattern(pattern);
            numberingMaker.getLevels().add(level);
        }
        return numberingMaker;
    }

    private void resetAfterLevel(int level) {
        for (int i = level; i < levels.size(); i++) {
            levels.get(i).reset();
        }
    }
}
