package io.github.jacktea.numberingmaker;

public class NumberingLevelConfig {
    private Integer start;
    private Integer level;
    private String pattern;
    private String suffix;

    public NumberingLevelConfig() {
    }

    public NumberingLevelConfig(Integer start, Integer level, String pattern, String suffix) {
        this.start = start;
        this.level = level;
        this.pattern = pattern;
        this.suffix = suffix;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
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
}
