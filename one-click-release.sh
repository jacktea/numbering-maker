#!/usr/bin/env bash
set -euo pipefail

# 获取当前激活的 branch
BRANCH=$(git rev-parse --abbrev-ref HEAD)

echo "Current branch: ${BRANCH}"

# 1. 执行版本升级 (patch)
echo "Bumping version..."
./bump-version.sh patch

# 2. 获取升级后的新版本号 (从 ts/package.json 读取)
NEW_VERSION=$(node -e "const pkg=require('./ts/package.json'); process.stdout.write(pkg.version);")
echo "New version: ${NEW_VERSION}"

# 3. Git 操作: 添加文件
echo "Adding files to git..."
git add ts/package.json java/pom.xml

# 4. Git 操作: 提交
echo "Committing version bump..."
git commit -m "chore: bump version to ${NEW_VERSION}"

# 5. Git 操作: 打标签
echo "Creating tag v${NEW_VERSION}..."
git tag -a "v${NEW_VERSION}" -m "Release v${NEW_VERSION}"

# 6. 推送至 origin 和 tags
echo "Pushing to origin ${BRANCH} with tags..."
git push origin "${BRANCH}" --follow-tags

echo "----------------------------------------------"
echo "Done! Release v${NEW_VERSION} has been published."
