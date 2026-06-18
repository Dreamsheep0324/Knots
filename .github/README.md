# CI/CD 配置指南

## 概述

项目配置了 3 个 GitHub Actions workflow：

| 文件 | 触发条件 | 功能 |
|------|---------|------|
| `ci.yml` | push 到 main / PR | 编译 + 单元测试，防止回归 |
| `pr.yml` | PR 到 main | 编译 + 单元测试 + Lint，PR 质量检查 |
| `release.yml` | push tag `v*` / 手动 | 测试 + 打包 Release APK + 发布 GitHub Release |

## 日常开发流程

```
1. 写代码 → push 到 main
   → CI 自动跑编译 + 测试（不发布）

2. 测试通过，确认可以发布
   → git tag v1.3.2 && git push origin v1.3.2
   → Release workflow 自动打包 + 发布

3. 如果想补充 Release 说明
   → 发布后在 GitHub Release 页面手动编辑
```

## 首次使用前：配置 GitHub Secrets

Release workflow 需要签名信息才能打包 Release APK。需要在 GitHub 仓库的 **Settings → Secrets and variables → Actions** 中添加以下 4 个 Secrets：

### 1. RELEASE_KEYSTORE_BASE64

将本地 keystore 文件编码为 base64：

```bash
# Linux/Mac
base64 -i app/keystore.jks | tr -d '\n'

# Windows PowerShell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("app\keystore.jks"))
```

将输出的 base64 字符串添加为 Secret 值。

### 2. RELEASE_KEYSTORE_PASSWORD

keystore 文件的密码。

### 3. RELEASE_KEY_ALIAS

签名 key 的别名。

### 4. RELEASE_KEY_PASSWORD

签名 key 的密码。

> **注意**：这些 Secrets 只在 Release workflow 中使用，CI 和 PR workflow 不需要签名信息（只编译和测试，不打包 Release APK）。

## 验证配置

1. **CI 验证**：push 任意代码到 main，在 GitHub Actions 页面查看 CI 是否通过
2. **Release 验证**：打一个测试 tag（如 `v0.0.1-test`），查看 Release workflow 是否正常

## 文件说明

```
.github/
└── workflows/
    ├── ci.yml        # 持续集成
    ├── pr.yml        # PR 质量检查
    └── release.yml  # 持续交付
```
