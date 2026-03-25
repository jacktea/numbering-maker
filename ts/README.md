# @jacktea/numbering-maker (TypeScript)

TypeScript/JavaScript 实现的多语言编号生成器。

## 功能特性

- 支持多种格式：阿拉伯数字、字母、罗马数字、中文数字等。
- 支持单层、多层及混合层级编号。
- 状态可保存与恢复，支持分页生成。
- 纯 ESM 包。
- 零依赖。

## 支持的格式

- `decimal` (数字)
- `upperLetter` (大写字母)
- `lowerLetter` (小写字母)
- `upperRoman` (大写罗马数字)
- `lowerRoman` (小写罗马数字)
- `ordinal` (序数词)
- `bullet` (符号)
- `chineseSimplified` (简体中文)
- `chineseTraditional` (繁体中文)

## 安装

```bash
npm install @jacktea/numbering-maker
```

或使用 yarn/pnpm:

```bash
pnpm add @jacktea/numbering-maker
yarn add @jacktea/numbering-maker
```

## 快速开始

```typescript
import { createMultilevelNumberingMaker } from '@jacktea/numbering-maker';

const maker = createMultilevelNumberingMaker(1);

console.log(maker.generateNumbering(0)); // 输出: 1.
console.log(maker.generateNumbering(1)); // 输出: 1.1.
console.log(maker.generateNumbering(1)); // 输出: 1.2.
console.log(maker.generateNumbering(2)); // 输出: 1.2.1.
```

## 不同层级使用不同编码器

```typescript
import {
  createMultilevelNumberingMaker,
  createNumberingLevel
} from '@jacktea/numbering-maker';

const maker = createMultilevelNumberingMaker(1);

maker.levels[0] = createNumberingLevel('decimal', {
  start: 1,
  level: 0,
  pattern: '%1.'
});
maker.levels[1] = createNumberingLevel('upperRoman', {
  start: 1,
  level: 1,
  pattern: '%1.%2.'
});
maker.levels[2] = createNumberingLevel('chineseSimplified', {
  start: 1,
  level: 2,
  pattern: '%1.%2.%3.'
});

console.log(maker.generateNumbering(0)); // 输出: 1.
console.log(maker.generateNumbering(1)); // 输出: 1.I.
console.log(maker.generateNumbering(1)); // 输出: 1.II.
console.log(maker.generateNumbering(2)); // 输出: 1.II.一.
console.log(maker.generateNumbering(2)); // 输出: 1.II.二.
```

## 运行测试

```bash
npm test
```
