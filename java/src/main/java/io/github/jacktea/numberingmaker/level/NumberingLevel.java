package io.github.jacktea.numberingmaker.level;

import io.github.jacktea.numberingmaker.NumberingFormat;
import io.github.jacktea.numberingmaker.NumberingLevelConfig;
import io.github.jacktea.numberingmaker.NumberingMakerState;

public class NumberingLevel {
    private NumberingFormat format;
    private int start;
    private int level;
    private String pattern;
    private String suffix;
    private int value;
    private boolean running;
    private boolean changed;

    public String generateNumber(String[] values) {
        if (running) {
            value++;
            changed = true;
        }
        running = true;
        values[level] = getValueLabel();
        String output = pattern == null ? "" : pattern;
        for (int i = 0; i < values.length; i++) {
            output = output.replace("%" + (i + 1), values[i]);
        }
        return output;
    }

    public void setStart(int start) {
        this.start = start;
        this.value = start;
    }

    public void reset() {
        this.value = start;
        this.running = false;
        this.changed = false;
    }

    public void updateConfig(NumberingLevelConfig config) {
        if (config.getStart() != null) {
            setStart(config.getStart());
        }
        if (config.getLevel() != null) {
            this.level = config.getLevel();
        }
        if (config.getPattern() != null) {
            this.pattern = config.getPattern();
        }
        if (config.getSuffix() != null) {
            this.suffix = config.getSuffix();
        }
    }

    public String getValueLabel() {
        return String.valueOf(value);
    }

    public NumberingMakerState.NumberingLevelState getState() {
        NumberingMakerState.NumberingLevelState state = new NumberingMakerState.NumberingLevelState();
        state.setValue(value);
        state.setRunning(running);
        state.setChanged(changed);
        return state;
    }

    public void setState(NumberingMakerState.NumberingLevelState state) {
        if (state == null) {
            return;
        }
        this.value = state.getValue();
        this.running = state.isRunning();
        this.changed = state.isChanged();
    }

    public NumberingFormat getFormat() {
        return format;
    }

    public void setFormat(NumberingFormat format) {
        this.format = format;
    }

    public int getStart() {
        return start;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }
}
