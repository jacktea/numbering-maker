export type MultiLevelType = "singleLevel" | "multilevel" | "hybridMultilevel";

export const MultiLevelTypes = {
  singleLevel: "singleLevel",
  multilevel: "multilevel",
  hybridMultilevel: "hybridMultilevel"
} as const;

export type NumberingFormat =
  | "decimal"
  | "upperLetter"
  | "lowerLetter"
  | "upperRoman"
  | "lowerRoman"
  | "ordinal"
  | "bullet"
  | "chineseSimplified"
  | "chineseTraditional";

export interface NumberingLevelConfig {
  start?: number;
  level?: number;
  pattern?: string;
  suffix?: string;
}

export interface NumberingLevelState {
  value: number;
  running: boolean;
  changed: boolean;
}

export interface NumberingMakerState {
  levelStates: NumberingLevelState[];
}

export class NumberingLevel {
  format: NumberingFormat = "decimal";
  start = 1;
  level = 0;
  pattern = "";
  suffix = "";
  value = 1;
  running = false;
  changed = false;

  generateNumber(values: string[]): string {
    if (this.running) {
      this.value += 1;
      this.changed = true;
    }
    this.running = true;
    values[this.level] = this.getValueLabel();
    let output = this.pattern ?? "";
    for (let i = 0; i < values.length; i += 1) {
      output = output.replaceAll(`%${i + 1}`, values[i]);
    }
    return output;
  }

  setStart(start: number): void {
    this.start = start;
    this.value = start;
  }

  reset(): void {
    this.value = this.start;
    this.running = false;
    this.changed = false;
  }

  updateConfig(config: NumberingLevelConfig): void {
    if (config.start !== undefined) {
      this.setStart(config.start);
    }
    if (config.level !== undefined) {
      this.level = config.level;
    }
    if (config.pattern !== undefined) {
      this.pattern = config.pattern;
    }
    if (config.suffix !== undefined) {
      this.suffix = config.suffix;
    }
  }

  getValueLabel(): string {
    return String(this.value);
  }

  getState(): NumberingLevelState {
    return {
      value: this.value,
      running: this.running,
      changed: this.changed
    };
  }

  setState(state: NumberingLevelState): void {
    this.value = state.value;
    this.running = state.running;
    this.changed = state.changed;
  }
}

export class DecimalNumberingLevel extends NumberingLevel {}

export class BulletNumberingLevel extends NumberingLevel {
  override getValueLabel(): string {
    return "";
  }
}

export class LetterNumberingLevel extends NumberingLevel {
  constructor(private readonly lowerCase: boolean) {
    super();
  }

  override getValueLabel(): string {
    const base = this.lowerCase ? "a".charCodeAt(0) : "A".charCodeAt(0);
    let value = this.value;
    let label = "";
    while (value > 0) {
      value -= 1;
      const remainder = value % 26;
      label = String.fromCharCode(base + remainder) + label;
      value = Math.floor(value / 26);
    }
    return label;
  }
}

export class RomanNumberingLevel extends NumberingLevel {
  constructor(private readonly lowerCase: boolean) {
    super();
  }

  override getValueLabel(): string {
    const values = [1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1];
    const upperSymbols = ["M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"];
    const lowerSymbols = ["m", "cm", "d", "cd", "c", "xc", "l", "xl", "x", "ix", "v", "iv", "i"];
    const symbols = this.lowerCase ? lowerSymbols : upperSymbols;
    let num = this.value;
    let result = "";
    for (let i = 0; i < values.length; i += 1) {
      while (num >= values[i]) {
        num -= values[i];
        result += symbols[i];
      }
    }
    return result;
  }
}

export class OrdinalNumberingLevel extends NumberingLevel {
  override getValueLabel(): string {
    const value = this.value;
    const mod100 = value % 100;
    if (mod100 >= 11 && mod100 <= 13) {
      return `${value}th`;
    }
    switch (value % 10) {
      case 1:
        return `${value}st`;
      case 2:
        return `${value}nd`;
      case 3:
        return `${value}rd`;
      default:
        return `${value}th`;
    }
  }
}

export class ChineseNumberingLevel extends NumberingLevel {
  constructor(private readonly traditional: boolean) {
    super();
  }

  override getValueLabel(): string {
    return convertToChinese(this.value, this.traditional);
  }
}

export function createNumberingLevel(format: NumberingFormat, config?: NumberingLevelConfig): NumberingLevel {
  let level: NumberingLevel;
  switch (format) {
    case "decimal":
      level = new DecimalNumberingLevel();
      break;
    case "upperLetter":
      level = new LetterNumberingLevel(false);
      break;
    case "lowerLetter":
      level = new LetterNumberingLevel(true);
      break;
    case "upperRoman":
      level = new RomanNumberingLevel(false);
      break;
    case "lowerRoman":
      level = new RomanNumberingLevel(true);
      break;
    case "ordinal":
      level = new OrdinalNumberingLevel();
      break;
    case "bullet":
      level = new BulletNumberingLevel();
      break;
    case "chineseSimplified":
      level = new ChineseNumberingLevel(false);
      break;
    case "chineseTraditional":
      level = new ChineseNumberingLevel(true);
      break;
  }
  level.format = format;
  if (config) {
    level.updateConfig(config);
  }
  return level;
}

export class NumberingMaker {
  numId = 0;
  multiLevelType: MultiLevelType = "multilevel";
  levels: NumberingLevel[] = [];

  generateNumbering(level: number): string {
    if (level < 0 || level >= this.levels.length) {
      return "";
    }
    const values = this.levels.map((current) => current.getValueLabel());
    const current = this.levels[level];
    const result = current.generateNumber(values);
    if (current.changed) {
      for (let i = level + 1; i < this.levels.length; i += 1) {
        this.levels[i].reset();
      }
    }
    return result;
  }

  getLevel(level: number): NumberingLevel | undefined {
    return this.levels[level];
  }

  getState(): NumberingMakerState {
    return {
      levelStates: this.levels.map((level) => level.getState())
    };
  }

  setState(state: NumberingMakerState): void {
    const size = Math.min(this.levels.length, state.levelStates.length);
    for (let i = 0; i < size; i += 1) {
      this.levels[i].setState(state.levelStates[i]);
    }
  }
}

export function createMultilevelNumberingMaker(numId: number): NumberingMaker {
  const maker = new NumberingMaker();
  maker.numId = numId;
  maker.multiLevelType = "multilevel";
  let pattern = "";
  for (let i = 0; i < 9; i += 1) {
    const level = new DecimalNumberingLevel();
    level.format = "decimal";
    level.level = i;
    level.setStart(1);
    pattern += `%${i + 1}.`;
    level.pattern = pattern;
    maker.levels.push(level);
  }
  return maker;
}

function convertToChinese(number: number, traditional: boolean): string {
  const simplifiedDigits = ["零", "一", "二", "三", "四", "五", "六", "七", "八", "九"];
  const traditionalDigits = ["零", "壹", "貳", "叁", "肆", "伍", "陸", "柒", "捌", "玖"];
  const simplifiedUnits = ["", "十", "百", "千", "万", "十", "百", "千", "亿"];
  const traditionalUnits = ["", "拾", "佰", "仟", "萬", "拾", "佰", "仟", "億"];
  const digits = traditional ? traditionalDigits : simplifiedDigits;
  const units = traditional ? traditionalUnits : simplifiedUnits;
  if (number === 0) {
    return digits[0];
  }
  const parts: string[] = [];
  let unitPos = 0;
  let needZero = false;
  while (number > 0) {
    const digit = number % 10;
    if (digit === 0) {
      if (parts.length > 0 && !needZero) {
        parts.unshift(digits[0]);
      }
      needZero = true;
    } else {
      if (needZero) {
        parts.unshift(digits[0]);
        needZero = false;
      }
      parts.unshift(digits[digit] + units[unitPos]);
    }
    number = Math.floor(number / 10);
    unitPos += 1;
  }
  let result = parts.join("");
  if (!traditional && result.startsWith("一十")) {
    result = result.slice(1);
  }
  result = result.replace(/零{4}/g, units[4]);
  result = result.replace(/零{8}/g, units[8]);
  result = result.replace(/零{2,}/g, "零");
  result = result.replace(/零$/g, "");
  return result;
}
