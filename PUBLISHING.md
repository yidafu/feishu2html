# 📦 Publishing Guide - 发布指南

## 前置准备

### 1. 注册 Sonatype 账号

1. 访问 [Sonatype JIRA](https://issues.sonatype.org/)
2. 创建账号
3. 创建一个 Issue 申请 `dev.yidafu.feishu2html` 组ID
4. 等待审核通过（通常 1-2 个工作日）

### 2. 生成 GPG 密钥

```bash
# 生成密钥
gpg --gen-key

# 查看密钥
gpg --list-keys

# 导出公钥到密钥服务器
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID

# 导出私钥（用于签名）
gpg --export-secret-keys -a YOUR_KEY_ID > private-key.asc
```

### 3. 配置本地凭证

复制 `local.properties.example` 为 `local.properties`：

```bash
cp local.properties.example local.properties
```

编辑 `local.properties` 填写：
- `ossrhUsername` - Sonatype 用户名
- `ossrhPassword` - Sonatype 密码
- `signing.keyId` - GPG 密钥 ID（最后 8 位）
- `signing.password` - GPG 密钥密码

**⚠️ 注意**：`local.properties` 包含敏感信息，已在 `.gitignore` 中，不会被提交到 Git。

---

## 发布流程

### 发布 SNAPSHOT 版本

SNAPSHOT 版本会自动发布，无需手动审批：

```bash
# 确保版本号以 -SNAPSHOT 结尾
# 在 build.gradle.kts 中：version = "1.0.0-SNAPSHOT"

# 发布所有平台
./gradlew publishAllPublicationsToSonatypeRepository

# 或发布特定平台
./gradlew publishJvmPublicationToSonatypeRepository
./gradlew publishJsPublicationToSonatypeRepository
```

**SNAPSHOT 仓库**：
```
https://s01.oss.sonatype.org/content/repositories/snapshots/
```

### 发布 Release 版本

Release 版本需要手动审批和发布：

```bash
# 1. 更新版本号（移除 -SNAPSHOT）
# 在 build.gradle.kts 中：version = "1.0.0"

# 2. 发布到 staging 仓库
./gradlew publishAllPublicationsToSonatypeRepository

# 3. 登录 Sonatype Nexus 进行审批
# https://s01.oss.sonatype.org/

# 4. 在 "Staging Repositories" 中找到你的仓库
# 5. 点击 "Close" 进行验证
# 6. 验证通过后点击 "Release" 发布到 Maven Central

# 7. 等待同步到 Maven Central（2-4 小时）
# https://repo1.maven.org/maven2/dev/yidafu/feishu2html/
```

---

## 使用 Maven Central 的包

### Gradle (Kotlin DSL)

**Multiplatform 项目**：
```kotlin
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("dev.yidafu.feishu2html:feishu2html:1.0.0")
            }
        }
    }
}
```

**JVM 项目**：
```kotlin
dependencies {
    implementation("dev.yidafu.feishu2html:feishu2html-jvm:1.0.0")
}
```

**JS 项目**：
```kotlin
dependencies {
    implementation("dev.yidafu.feishu2html:feishu2html-js:1.0.0")
}
```

### Maven

```xml
<dependency>
    <groupId>dev.yidafu.feishu2html</groupId>
    <artifactId>feishu2html-jvm</artifactId>
    <version>1.0.0</version>
</dependency>
```

---

## 环境变量配置（CI/CD）

在 GitHub Actions 或其他 CI/CD 中使用环境变量：

```yaml
# .github/workflows/publish.yml
env:
  OSSRH_USERNAME: ${{ secrets.OSSRH_USERNAME }}
  OSSRH_PASSWORD: ${{ secrets.OSSRH_PASSWORD }}
  SIGNING_KEY: ${{ secrets.SIGNING_KEY }}
  SIGNING_PASSWORD: ${{ secrets.SIGNING_PASSWORD }}
```

在 GitHub Secrets 中配置：
- `OSSRH_USERNAME`
- `OSSRH_PASSWORD`
- `SIGNING_KEY` （GPG 私钥，base64 编码或 ASCII armor 格式）
- `SIGNING_PASSWORD`

---

## 常用命令

### 本地验证

```bash
# 验证 POM 文件
./gradlew generatePomFileForKotlinMultiplatformPublication

# 查看生成的 POM
cat build/publications/kotlinMultiplatform/pom-default.xml

# 发布到本地 Maven 仓库（用于测试）
./gradlew publishToMavenLocal

# 检查本地发布的文件
ls ~/.m2/repository/dev/yidafu/feishu2html/
```

### 签名测试

```bash
# 测试签名配置
./gradlew signKotlinMultiplatformPublication

# 查看签名文件
find build/libs -name "*.asc"
```

### 发布任务

```bash
# 查看所有发布任务
./gradlew tasks --group publishing

# 发布所有平台
./gradlew publish

# 发布到 Sonatype
./gradlew publishAllPublicationsToSonatypeRepository

# 发布特定平台
./gradlew publishJvmPublicationToSonatypeRepository
./gradlew publishJsPublicationToSonatypeRepository
./gradlew publishMacosArm64PublicationToSonatypeRepository
```

---

## 发布检查清单

### 首次发布前

- [ ] 在 Sonatype JIRA 申请组 ID
- [ ] 等待 Sonatype 审核通过
- [ ] 生成 GPG 密钥对
- [ ] 上传公钥到密钥服务器
- [ ] 配置 `local.properties`
- [ ] 测试本地发布：`./gradlew publishToMavenLocal`

### 每次发布前

- [ ] 更新版本号
- [ ] 运行所有测试：`./gradlew jvmTest`
- [ ] 验证所有平台编译：`./gradlew build`
- [ ] 更新 CHANGELOG.md（如果有）
- [ ] 检查 POM 信息正确

### 发布 SNAPSHOT

- [ ] 确保版本号包含 `-SNAPSHOT`
- [ ] 运行：`./gradlew publish`
- [ ] 验证 SNAPSHOT 仓库中的文件

### 发布 Release

- [ ] 移除版本号中的 `-SNAPSHOT`
- [ ] 运行：`./gradlew publish`
- [ ] 登录 Sonatype Nexus
- [ ] Close staging repository
- [ ] Release to Maven Central
- [ ] 创建 Git tag：`git tag v1.0.0`
- [ ] 推送 tag：`git push origin v1.0.0`

---

## 故障排查

### 签名失败

```bash
# 检查 GPG 配置
gpg --list-secret-keys

# 测试签名
echo "test" | gpg --clearsign
```

### 上传失败

```bash
# 检查凭证
./gradlew publish --info

# 检查网络连接
curl -I https://s01.oss.sonatype.org/
```

### POM 验证失败

常见问题：
- 缺少 `licenses` 信息
- 缺少 `developers` 信息
- 缺少 `scm` 信息
- 缺少源码和文档 JAR

---

## 参考资料

- [Sonatype OSSRH Guide](https://central.sonatype.org/publish/publish-guide/)
- [GPG 签名指南](https://central.sonatype.org/publish/requirements/gpg/)
- [Kotlin Multiplatform Publishing](https://kotlinlang.org/docs/multiplatform-publish-lib.html)
- [Gradle Maven Publish Plugin](https://docs.gradle.org/current/userguide/publishing_maven.html)

---

**最后更新**: 2025-10-19

