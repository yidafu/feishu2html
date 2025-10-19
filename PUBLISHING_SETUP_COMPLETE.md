# ✅ 发布配置完成

**日期**: 2025-10-19  
**状态**: 配置完成  

---

## 完成的配置

### 1. ✅ Gradle Version Catalog (libs.versions.toml)

创建了 `gradle/libs.versions.toml`，统一管理所有依赖：

**优势**:
- 集中版本管理
- 类型安全引用
- 便于批量升级
- 创建了 bundles（ktor-common, kotest）

### 2. ✅ Gradle 升级到 8.10.2

从 8.5 升级到 8.10.2（2024年最新稳定版）

### 3. ✅ Maven 发布配置

配置了完整的 Maven 发布到 Sonatype/Maven Central：

**配置内容**:
- `maven-publish` 插件
- 完整的 POM 信息（licenses, developers, scm）
- Sonatype 仓库配置（Snapshot + Release）
- GPG 签名配置

### 4. ✅ 本地配置文件

- `local.properties.example` - 配置模板
- `PUBLISHING.md` - 完整发布指南
- `gradle.properties` - 项目元数据

### 5. ✅ 安全配置

- 更新 `.gitignore` 排除 `local.properties`
- 支持环境变量配置（CI/CD 友好）

---

## 支持的平台（7个）

- ✅ JVM (Java 17)
- ✅ JS (Node.js)
- ✅ macOS (x64, ARM64)
- ✅ Linux x64
- ✅ Windows x64
- ✅ iOS (x64, ARM64, Simulator)

**移除**: Android Native, Linux ARM64（兼容性问题）

---

## 本地发布测试

```bash
# 发布到本地 Maven
./gradlew publishJvmPublicationToMavenLocal
./gradlew publishJsPublicationToMavenLocal

✅ 成功发布到: ~/.m2/repository/dev/yidafu/feishu2html/
```

**发布的包**:
- `feishu2html-jvm-1.0.0-SNAPSHOT.jar`
- `feishu2html-js-1.0.0-SNAPSHOT.jar`
- POM 文件
- Sources JAR

---

## 下一步

### 首次发布到 Maven Central

1. **注册 Sonatype 账号**
   - 访问 https://issues.sonatype.org/
   - 创建 Issue 申请组 ID

2. **生成 GPG 密钥**
   ```bash
   gpg --gen-key
   gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
   ```

3. **配置本地凭证**
   ```bash
   cp local.properties.example local.properties
   # 编辑 local.properties 填写凭证
   ```

4. **发布 SNAPSHOT**
   ```bash
   ./gradlew publishAllPublicationsToSonatypeRepository
   ```

5. **发布 Release**
   - 更新版本号（移除 -SNAPSHOT）
   - 发布并在 Sonatype Nexus 中审批

详细步骤见 `PUBLISHING.md`

---

## 快速命令

```bash
# 本地测试
./gradlew publishToMavenLocal

# 发布 SNAPSHOT
./gradlew publishAllPublicationsToSonatypeRepository

# 查看发布任务
./gradlew tasks --group publishing

# 验证 POM
./gradlew generatePomFileForJvmPublication
```

---

**配置完成，可以开始发布！** 🚀

