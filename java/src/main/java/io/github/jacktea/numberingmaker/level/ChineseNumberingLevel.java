package io.github.jacktea.numberingmaker.level;

public class ChineseNumberingLevel extends NumberingLevel {
    private static final String[] SIMPLIFIED_DIGITS = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
    private static final String[] TRADITIONAL_DIGITS = {"零", "壹", "貳", "叁", "肆", "伍", "陸", "柒", "捌", "玖"};
    private static final String[] SIMPLIFIED_UNITS = {"", "十", "百", "千", "万", "十", "百", "千", "亿"};
    private static final String[] TRADITIONAL_UNITS = {"", "拾", "佰", "仟", "萬", "拾", "佰", "仟", "億"};

    private final boolean traditional;

    public ChineseNumberingLevel(boolean traditional) {
        this.traditional = traditional;
    }

    @Override
    public String getValueLabel() {
        return convertToChinese(getValue());
    }

    private String convertToChinese(int number) {
        String[] digits = traditional ? TRADITIONAL_DIGITS : SIMPLIFIED_DIGITS;
        String[] units = traditional ? TRADITIONAL_UNITS : SIMPLIFIED_UNITS;
        if (number == 0) {
            return digits[0];
        }
        StringBuilder builder = new StringBuilder();
        int unitPos = 0;
        boolean needZero = false;
        while (number > 0) {
            int digit = number % 10;
            if (digit == 0) {
                if (builder.length() > 0 && !needZero) {
                    builder.insert(0, digits[0]);
                }
                needZero = true;
            } else {
                if (needZero) {
                    builder.insert(0, digits[0]);
                    needZero = false;
                }
                builder.insert(0, digits[digit] + units[unitPos]);
            }
            number /= 10;
            unitPos++;
        }
        if (!traditional && builder.length() > 1 && builder.charAt(0) == '一' && builder.charAt(1) == '十') {
            builder.deleteCharAt(0);
        }
        String result = builder.toString();
        result = result.replaceAll("零{4}", units[4]);
        result = result.replaceAll("零{8}", units[8]);
        result = result.replaceAll("零{2,}", "零");
        result = result.replaceAll("零$", "");
        return result;
    }
}
