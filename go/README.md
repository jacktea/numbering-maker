# numbering-maker-go

Go 实现的多语言编号生成器。

## 功能特性

- 支持多种格式：阿拉伯数字、字母、罗马数字、中文数字等。
- 支持单层、多层及混合层级编号。
- 状态可导出与导入（`GetState`/`SetState`），支持分页生成。
- 自动处理层级重置（当父级变更时）。

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
go get github.com/jacktea/numbering-maker/go
```

## 快速开始

```go
package main

import (
	"fmt"
	"github.com/jacktea/numbering-maker/go"
)

func main() {
	maker := numberingmaker.CreateMultilevelNumberingMaker(1)
	
	fmt.Println(maker.GenerateNumbering(0)) // 输出: 1.
	fmt.Println(maker.GenerateNumbering(1)) // 输出: 1.1.
	fmt.Println(maker.GenerateNumbering(1)) // 输出: 1.2.
	fmt.Println(maker.GenerateNumbering(2)) // 输出: 1.2.1.
}
```

## 不同层级使用不同编码器

```go
package main

import (
	"fmt"

	numberingmaker "github.com/jacktea/numbering-maker/go"
)

func main() {
	maker := numberingmaker.CreateMultilevelNumberingMaker(1)

	maker.Levels[0] = numberingmaker.CreateLevel(numberingmaker.DecimalFormat, &numberingmaker.NumberingLevelConfig{
		Start:   intPtr(1),
		Level:   intPtr(0),
		Pattern: stringPtr("%1."),
	})
	maker.Levels[1] = numberingmaker.CreateLevel(numberingmaker.UpperRomanFormat, &numberingmaker.NumberingLevelConfig{
		Start:   intPtr(1),
		Level:   intPtr(1),
		Pattern: stringPtr("%1.%2."),
	})
	maker.Levels[2] = numberingmaker.CreateLevel(numberingmaker.ChineseSimplifiedFormat, &numberingmaker.NumberingLevelConfig{
		Start:   intPtr(1),
		Level:   intPtr(2),
		Pattern: stringPtr("%1.%2.%3."),
	})

	fmt.Println(maker.GenerateNumbering(0)) // 输出: 1.
	fmt.Println(maker.GenerateNumbering(1)) // 输出: 1.I.
	fmt.Println(maker.GenerateNumbering(1)) // 输出: 1.II.
	fmt.Println(maker.GenerateNumbering(2)) // 输出: 1.II.一.
	fmt.Println(maker.GenerateNumbering(2)) // 输出: 1.II.二.
}

func intPtr(value int) *int {
	return &value
}

func stringPtr(value string) *string {
	return &value
}
```

## 运行测试

```bash
go test ./...
```
