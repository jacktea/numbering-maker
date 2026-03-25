package io.github.jacktea.numberingmaker;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class NumberingPool {
    private final Map<Integer, NumberingMaker> data = new HashMap<>();

    public void addNumbering(int numId, NumberingMaker numberingMaker) {
        data.put(numId, numberingMaker);
    }

    public void addNumberings(Collection<NumberingMaker> makers) {
        for (NumberingMaker maker : makers) {
            data.put(maker.getNumId(), maker);
        }
    }

    public NumberingMaker get(int numId) {
        return data.get(numId);
    }
}
