# CI/CD 配置指南

## 概述

项目配置了 3 个 GitHub Actions workflow，自动化编译、测试和发布流程：

| 文件 | 触发条件 | 功能 | 何时使用 |
|------|---------|------|---------|
| `ci.yml` | push 到 main | 编译 + 单元测试 | 每次提交代码 |
| `pr.yml` | PR 到 main | 编译 + 单元测试 + Lint | 提交 Pull Request 时 |
| `release.yml` | push tag `v*` | 测试 + 打包 Release APK + 发布 | 正式发布版本时 |

---

## 日常开发：每次写完代码

### 你做什么

```bash
# 写完代码后
git add <文件>
git commit -m "feat: 新功能描述"
git push origin main
```

### CI 自动做什么

```
push 到 main
  → GitHub Actions 自动触发 ci.yml
  → 步骤1: 检出代码
  → 步骤2: 安装 JDK 17
  → 步骤3: 安装 Gradle
  → 步骤4: chmod +x gradlew
  → 步骤5: 编译检查 (compileDebugKotlin)
  → 步骤6: 单元测试 (testDebugUnitTest, ~580 个用例)
  → 步骤7: 上传测试报告（即使失败也上传）
```

### 在哪看结果

1. 打开 [https://github.com/Dreamsheep0324/Knots/actions](https://github.com/Dreamsheep0324/Knots/actions)
2. 点击最新的 "CI" run
3. 绿色 ✅ = 全部通过，红色 ❌ = 有失败
4. 点进去可以看详细日志和下载测试报告

### 如果 CI 失败了

- 点进失败的 step 查看日志
- 常见原因：编译错误、测试失败
- 修复后重新 push，CI 会自动重跑
- **CI 失败不会阻止你的 push**，但说明代码有问题

---

## 正式发布：发布新版本

### 前置条件（只需做一次）

在 GitHub 仓库 **Settings → Secrets and variables → Actions** 中添加 4 个 Secrets：

| Secret 名称 | 值 | 说明 |
|-------------|-----|------|
| `RELEASE_KEYSTORE_BASE64` | base64 编码的 keystore 文件 | 见下方生成方法 |
| `RELEASE_KEYSTORE_PASSWORD` | `123456` | keystore 密码 |
| `RELEASE_KEY_ALIAS` | `Knots` | key 别名 |
| `RELEASE_KEY_PASSWORD` | `123456` | key 密码 |

**生成 base64 编码**（在本地 PowerShell 执行）：

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("F:\code\zhenshu\Knotskey.jks"))
```

把输出的长字符串复制到 `RELEASE_KEYSTORE_BASE64` Secret 中。

### 发布步骤

```bash
# 1. 更新版本号
#    编辑 app/build.gradle.kts:
#    versionCode = 10302  →  10303
#    versionName = "1.3.2"  →  "1.3.3"

# 2. 更新 CHANGELOG.md
#    在文件顶部添加 ## [1.3.3] - 2026-xx-xx 章节

# 3. 更新 memory/context.md 版本纪年表

# 4. 本地验证
.\gradlew.bat clean
.\gradlew.bat testDebugUnitTest
.\gradlew.bat :app:assembleRelease

# 5. 真机测试
adb install -r .\app\build\outputs\apk\release\app-release.apk

# 6. 提交并推送
git add -A
git commit -m "chore: release v1.3.3"
git push origin main

# 7. 打 Tag 触发自动发布 ← 关键步骤
git tag v1.3.3
git push origin v1.3.3
```

### 打 Tag 后自动发生什么

```
push tag v1.3.3
  → GitHub Actions 自动触发 release.yml
  → 步骤1: 检出代码
  → 步骤2: 安装 JDK 17
  → 步骤3: 安装 Gradle
  → 步骤4: chmod +x gradlew
  → 步骤5: 提取版本号 (v1.3.3 → 1.3.3)
  → 步骤6: 运行单元测试（全绿才继续）
  → 步骤7: 从 Secrets 解码 keystore + 生成 keystore.properties
  → 步骤8: 打包 Release APK (assembleRelease)
  → 步骤9: 重命名 APK 为 Knots-v1.3.3.apk
  → 步骤10: 从 CHANGELOG.md 提取 v1.3.3 的内容作为 Release Notes
  → 步骤11: 创建 GitHub Release + 上传 APK
  → 步骤12: 清理签名文件
```

### 在哪看结果

1. 打开 [https://github.com/Dreamsheep0324/Knots/actions](https://github.com/Dreamsheep0324/Knots/actions)
2. 点击 "Release" workflow 的最新 run
3. 绿色 ✅ = 发布成功
4. 打开 [https://github.com/Dreamsheep0324/Knots/releases](https://github.com/Dreamsheep0324/Knots/releases) 查看 Release
5. Release 标题为 `v1.3.3`，正文是 CHANGELOG.md 中对应版本的内容
6. APK 文件名为 `Knots-v1.3.3.apk`，可直接下载安装

### 如果 Release workflow 失败了

- **测试失败**：修复代码，删除 tag，重新打 tag
  ```bash
  git tag -d v1.3.3          # 删除本地 tag
  git push origin :refs/tags/v1.3.3  # 删除远程 tag
  # 修复后重新打 tag
  git tag v1.3.3
  git push origin v1.3.3
  ```
- **签名失败**：检查 GitHub Secrets 是否配置正确
- **Release 已存在**：先在 GitHub Release 页面删除旧 Release，再重新打 tag

---

## PR 流程（可选，单人项目可忽略）

PR 适合多人协作或重大变更时使用：

```bash
# 1. 创建新分支
git checkout -b feature/new-feature

# 2. 写代码，提交
git add <文件>
git commit -m "feat: new feature"
git push origin feature/new-feature

# 3. 在 GitHub 页面创建 Pull Request
#    base: main ← compare: feature/new-feature

# 4. PR workflow 自动运行编译 + 测试 + Lint

# 5. 全绿后点击 "Merge pull request" 合并到 main

# 6. 删除分支
git checkout main
git branch -d feature/new-feature
git push origin --delete feature/new-feature
```

---

## 常见问题

### CI 跑了多久？

- CI（编译+测试）：约 3-5 分钟
- Release（测试+打包+发布）：约 5-10 分钟

### CI 失败会阻止 push 吗？

不会。CI 是事后验证，你的 push 总是成功的。但 CI 失败说明代码有问题，应该修复。

### Release 会随意发布吗？

不会。只有你手动 `git tag v1.x.x && git push origin v1.x.x` 才会触发。日常 push 代码不会发布。

### 签名会和本地不同吗？

不会。CI 从 GitHub Secrets 读取同一个 keystore 文件，签名完全一致，用户可以正常覆盖安装。

### 如何手动触发 Release workflow？

在 GitHub Actions 页面选择 "Release" workflow，点击 "Run workflow"，输入版本号（如 `v1.3.3`）。但推荐用 tag 触发，更可靠。

---

## 文件结构

```
.github/
├── CI_CD_GUIDE.md          # 本文件
└── workflows/
    ├── ci.yml              # 持续集成（push 触发）
    ├── pr.yml              # PR 质量检查（PR 触发）
    └── release.yml         # 持续交付（tag 触发）
```
