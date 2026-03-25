package io.github.jacktea.numberingmaker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NumberingMakerState implements Serializable {
    private List<NumberingLevelState> levelStates = new ArrayList<>();

    public List<NumberingLevelState> getLevelStates() {
        return levelStates;
    }

    public void setLevelStates(List<NumberingLevelState> levelStates) {
        this.levelStates = levelStates;
    }

    public static class NumberingLevelState implements Serializable {
        private int value;
        private boolean running;
        private boolean changed;

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
}
