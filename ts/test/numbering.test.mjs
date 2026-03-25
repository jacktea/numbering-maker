import test from "node:test";
import assert from "node:assert/strict";
import {
  BulletNumberingLevel,
  ChineseNumberingLevel,
  DecimalNumberingLevel,
  LetterNumberingLevel,
  OrdinalNumberingLevel,
  RomanNumberingLevel,
  createMultilevelNumberingMaker,
  createNumberingLevel
} from "../dist/src/index.js";

test("createMultilevelNumberingMaker has nine levels", () => {
  const maker = createMultilevelNumberingMaker(1);

  assert.equal(maker.levels.length, 9);
  assert.equal(maker.multiLevelType, "multilevel");
});

test("generateNumbering and restore state follow expected sequence", () => {
  const maker1 = createMultilevelNumberingMaker(1);

  assert.equal(maker1.generateNumbering(0), "1.");
  assert.equal(maker1.generateNumbering(1), "1.1.");
  assert.equal(maker1.generateNumbering(1), "1.2.");
  assert.equal(maker1.generateNumbering(2), "1.2.1.");
  assert.equal(maker1.generateNumbering(2), "1.2.2.");

  const state = maker1.getState();
  assert.equal(state.levelStates.length, 9);

  const maker2 = createMultilevelNumberingMaker(1);
  maker2.setState(state);

  assert.equal(maker1.generateNumbering(2), "1.2.3.");
  assert.equal(maker2.generateNumbering(2), "1.2.3.");
  assert.equal(maker1.generateNumbering(1), "1.3.");
  assert.equal(maker2.generateNumbering(1), "1.3.");
  assert.equal(maker1.generateNumbering(2), "1.3.1.");
  assert.equal(maker2.generateNumbering(2), "1.3.1.");
});

test("generateNumbering out of range returns empty string", () => {
  const maker = createMultilevelNumberingMaker(1);

  assert.equal(maker.generateNumbering(-1), "");
  assert.equal(maker.generateNumbering(9), "");
});

test("row number rollback sequence matches expected pattern", () => {
  const maker = createMultilevelNumberingMaker(1);

  assert.equal(maker.generateNumbering(0), "1.");
  assert.equal(maker.generateNumbering(1), "1.1.");
  assert.equal(maker.generateNumbering(1), "1.2.");
  assert.equal(maker.generateNumbering(2), "1.2.1.");
  assert.equal(maker.generateNumbering(2), "1.2.2.");
  assert.equal(maker.generateNumbering(2), "1.2.3.");
  assert.equal(maker.generateNumbering(1), "1.3.");
  assert.equal(maker.generateNumbering(2), "1.3.1.");
});

test("factory creates concrete level types", () => {
  const config = { start: 1, level: 0, pattern: "%1." };

  assert.ok(createNumberingLevel("decimal", config) instanceof DecimalNumberingLevel);
  assert.ok(createNumberingLevel("upperLetter", config) instanceof LetterNumberingLevel);
  assert.ok(createNumberingLevel("lowerRoman", config) instanceof RomanNumberingLevel);
  assert.ok(createNumberingLevel("ordinal", config) instanceof OrdinalNumberingLevel);
  assert.ok(createNumberingLevel("bullet", config) instanceof BulletNumberingLevel);
  assert.ok(createNumberingLevel("chineseTraditional", config) instanceof ChineseNumberingLevel);
});

test("conversions cover typical and boundary values", () => {
  const upperLetter = createNumberingLevel("upperLetter", { start: 26, level: 0, pattern: "%1" });
  const lowerRoman = createNumberingLevel("lowerRoman", { start: 58, level: 0, pattern: "%1" });
  const ordinal = createNumberingLevel("ordinal", { start: 11, level: 0, pattern: "%1" });
  const chineseSimplified = createNumberingLevel("chineseSimplified", { start: 10, level: 0, pattern: "%1" });
  const chineseTraditional = createNumberingLevel("chineseTraditional", { start: 1234, level: 0, pattern: "%1" });
  const bullet = createNumberingLevel("bullet", { start: 1, level: 0, pattern: "%1" });

  assert.equal(upperLetter.getValueLabel(), "Z");
  assert.equal(lowerRoman.getValueLabel(), "lviii");
  assert.equal(ordinal.getValueLabel(), "11th");
  assert.equal(chineseSimplified.getValueLabel(), "十");
  assert.equal(chineseTraditional.getValueLabel(), "壹仟貳佰叁拾肆");
  assert.equal(bullet.getValueLabel(), "");
});
