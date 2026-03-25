# numbering-maker-java

Java 实现的多语言编号生成器（Maven）。

## 功能特性

- 支持多种格式：阿拉伯数字、字母、罗马数字、中文数字等。
- 支持单层、多层及混合层级编号。
- 状态可保存与恢复，支持分页生成。
- 自动处理层级重置。
- 兼容 Java 17+。

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

## Maven 依赖

目前尚未发布到中央仓库，可通过本地构建：

```bash
mvn install
```

然后在 `pom.xml` 中引入：

```xml
<dependency>
    <groupId>io.github.jacktea</groupId>
    <artifactId>numbering-maker-java</artifactId>
    <version>0.1.0</version>
</dependency>
```

## 快速开始

```java
import io.github.jacktea.numbering.NumberingMaker;

public class Main {
    public static void main(String[] args) {
        NumberingMaker maker = NumberingMaker.createMultilevelNumberingMaker(1);
        
        System.out.println(maker.generateNumbering(0)); // 输出: 1.
        System.out.println(maker.generateNumbering(1)); // 输出: 1.1.
        System.out.println(maker.generateNumbering(1)); // 输出: 1.2.
        System.out.println(maker.generateNumbering(2)); // 输出: 1.2.1.
    }
}
```

## 运行测试

```bash
mvn test
```
