package io.github.jacktea.numberingmaker;

public enum MultiLevelType {
    SINGLE_LEVEL("singleLevel"),
    MULTI_LEVEL("multilevel"),
    HYBRID_MULTILEVEL("hybridMultilevel");

    private final String value;

    MultiLevelType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static MultiLevelType fromValue(String value) {
        for (MultiLevelType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException(value);
    }
}
