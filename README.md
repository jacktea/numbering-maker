# numbering-maker

Java、Go 和 TypeScript 的多语言编号生成器实现。

## 支持的格式

三种实现均对齐了以下核心格式：

- `decimal` (数字)
- `upperLetter` (大写字母)
- `lowerLetter` (小写字母)
- `upperRoman` (大写罗马数字)
- `lowerRoman` (小写罗马数字)
- `ordinal` (序数词)
- `bullet` (符号)
- `chineseSimplified` (简体中文)
- `chineseTraditional` (繁体中文)

## 仓库布局

- `java/`: 基于 Maven 的 Java 实现，包含 JUnit 5 测试
- `go/`: Go 模块，包含 `go test`
- `ts/`: 纯 ESM TypeScript 包，由 `tsc` 编译

## 核心行为

- `NumberingMaker` 管理 `numId`、`multiLevelType` 和 `levels`。
- `generateNumbering(level)`：当请求的层级超出范围时，返回空字符串。
- 当父级发生变化时，所有后续层级将重置为初始状态。
- `getState()` 和 `setState()`：支持分页或检查点恢复。
- `suffix` 作为层级元数据保留，用于未来的适配器；当前的输出由 `pattern` 驱动。

## 示例

三种实现都遵循相同的演进过程：

```text
level 0 -> 1.
level 1 -> 1.1.
level 1 -> 1.2.
level 2 -> 1.2.1.
level 2 -> 1.2.2.
```

## 快速开始

### Java

```bash
cd java
mvn test
```

### Go

```bash
cd go
go test ./...
```

### TypeScript

```bash
cd ts
npm test
```

## 版本更新

使用根目录下的脚本同步更新 Java 和 TypeScript 的版本：

```bash
./bump-version.sh patch
```

其他形式：

- `./bump-version.sh minor`
- `./bump-version.sh major`
- `./bump-version.sh set 1.2.3`
- `./bump-version.sh patch --dry-run`

标签政策：

- 在实际发布版本时创建 git 标签。
- 仅在提交版本更新后创建标签。
- 如果仅在本地验证，请跳过标签，直到发布提交准备就绪。

## 统一发布

运行根目录下的发布脚本，验证三个包并收集产物：

```bash
./release.sh
```

参数选项：

- `--output <dir>`: 将发布产物写入自定义目录。
- `--skip-tests`: 打包现有源码而不重新运行测试。

生成的发布产物：

- `release/java/numbering-maker-java-<version>.jar`
- `release/go/numbering-maker-go-<version>.tar.gz`
- `release/ts/jacktea-numbering-maker-<version>.tgz`
- `release/SHA256SUMS`
- `release/manifest.txt`

## npm 发布

发布 `ts/` 目录下的 TypeScript 包到 npmjs：

```bash
./publish-npm.sh
```

常用选项：

- `--dry-run`: 验证发布内容而不上传。
- `--skip-tests`: 仅在 `npm run build` 后发布。

该脚本发布到 `https://registry.npmjs.org/`，使用公共访问权限，并在 `.cache/npm` 下使用本地 npm 缓存。

对于 CI 或预编译的压缩包，还支持：

- `--skip-auth`: 跳过 `npm whoami` 验证。
- `--tarball <file>`: 发布预编译的 `.tgz` 文件。

## GitHub Tag 发布

此仓库可以通过推送 `vX.Y.Z` 格式的标签进行发布，或者通过手动触发 Release 工作流针对现有标签执行发布。

工作流文件：

- `.github/workflows/release.yml`

工作流内容：

- 验证推送的标签版本是否与 `ts/package.json` 和 `java/pom.xml` 一致。
- 通过 `./release.sh` 运行统一构建。
- 创建 GitHub Release 并上传：
  - Java jar 包
  - Go 源码存档
  - TypeScript npm 压缩包
  - `SHA256SUMS`
  - `manifest.txt`
- 通过 npm Trusted Publishing 将 `ts/` 发布到 npmjs。

触发模式：

- 推送触发：推送 `vX.Y.Z` 标签时自动运行。
- 手动触发：从 GitHub Actions 界面运行 `Release`，并提供 `release_tag=vX.Y.Z`。

一次性设置：

1. 创建 GitHub 仓库并配置 `origin`。
2. 推送仓库内容，包括 `.github/workflows/release.yml`。
3. 在 npmjs 上，打开 `@jacktea/numbering-maker` 的包设置。
4. 添加 Trusted Publisher（受信任的发布者）：
   `GitHub user/org` + `repository` + `workflow filename=release.yml`
5. 使用 GitHub 托管的 runner。
6. 在第一次成功发布后，可选择加强 npm 包安全性，要求 2FA 并禁用经典 token。

每版本发布本地步骤：

```bash
./bump-version.sh patch
git add ts/package.json java/pom.xml
git commit -m "chore: bump version to X.Y.Z"
git tag -a vX.Y.Z -m "Release vX.Y.Z"
git push origin <branch> --follow-tags
```

手动重运行流程：

1. 打开 GitHub Actions。
2. 选择 `Release` 工作流。
3. 点击 `Run workflow`。
4. 输入现有的标签，例如 `v0.1.1`。
5. 针对该标签运行。

重要规则：

- 标签必须在版本更新提交之后创建。
- 标签必须与代码版本完全一致。
- 工作流专为通过 OIDC 进行公共 npm 发布而设计，不使用长效 `NPM_TOKEN`。

## 作用范围

此仓库仅提供纯内存编号库。
