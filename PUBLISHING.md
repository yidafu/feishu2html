# 📦 Publishing Guide - 发布指南

> **Important Update**: As of June 30, 2025, OSSRH has been shut down and replaced by **Central Portal Publisher**.  
> See: https://central.sonatype.org/pages/ossrh-eol/

> **Plugin Used**: This project uses [NMCP (New Maven Central Publisher)](https://github.com/GradleUp/nmcp) - a modern Gradle plugin purpose-built for Central Portal.

---

## Prerequisites - 前置准备

### 1. Register Central Portal Account

1. Visit [Maven Central Portal](https://central.sonatype.com/)
2. Create an account using the same credentials as your previous OSSRH account (if migrating)
3. Verify your namespace ownership (e.g., `dev.yidafu.feishu2html`)

### 2. Generate Portal User Token

**Important**: You must generate a new Portal user token to replace your OSSRH token.

1. Log in to [Maven Central Portal](https://central.sonatype.com/)
2. Navigate to your account settings
3. Generate a new user token
4. Save the token securely (username + password)

Documentation: [Generating a Portal Token](https://central.sonatype.org/publish/generate-portal-token/)

### 3. Generate GPG Keys

```bash
# Generate key pair
gpg --gen-key

# List keys
gpg --list-keys

# Export public key to keyserver
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID

# Export private key (for signing)
gpg --export-secret-keys -a YOUR_KEY_ID > private-key.asc
```

### 4. Configure Credentials

Copy the example configuration:

```bash
cp local.properties.example local.properties
```

Edit `local.properties` and fill in:

```properties
# Central Portal credentials (Portal user token - REQUIRED)
centralUsername=your-portal-token-username
centralPassword=your-portal-token-password

# GPG signing configuration (REQUIRED for releases)
signing.password=your-gpg-passphrase
signing.key=-----BEGIN PGP PRIVATE KEY BLOCK-----\n...\n-----END PGP PRIVATE KEY BLOCK-----
```

**Note**: The old `ossrhUsername`/`ossrhPassword` names are deprecated. Use `centralUsername`/`centralPassword` instead.

**⚠️ Note**: `local.properties` is git-ignored for security.

---

## Publishing Process

### Using NMCP Plugin

This project uses the **NMCP (New Maven Central Publisher)** plugin, which simplifies publishing to Central Portal with a single command.

### Publish SNAPSHOT Version

SNAPSHOT versions are automatically published without manual approval:

```bash
# Ensure version ends with -SNAPSHOT
# In build.gradle.kts: version = "1.0.0-SNAPSHOT"

# Publish all platforms with one command
./gradlew nmcpPublish

# The plugin will:
# 1. Build all publications
# 2. Sign artifacts (if configured)
# 3. Upload to Central Portal
# 4. Automatically publish (SNAPSHOT mode)
```

**Note**: SNAPSHOT versions are immediately available after successful upload.

### Publish Release Version

Release versions require manual approval in the Portal:

```bash
# 1. Update version (remove -SNAPSHOT)
# In build.gradle.kts: version = "1.0.0"

# 2. Publish to Central Portal
./gradlew nmcpPublish

# The plugin will upload and mark as USER_MANAGED

# 3. Log in to Central Portal
# https://central.sonatype.com/

# 4. Verify and publish your deployment
# - Navigate to "Deployments"
# - Review your published components
# - Click "Publish" to release to Maven Central

# 5. Wait for sync to Maven Central (typically 30 minutes to 4 hours)
# https://repo1.maven.org/maven2/dev/yidafu/feishu2html/
```

### Platform-Specific Publishing

If you need to publish specific platforms only:

```bash
# Publish JVM only
./gradlew publishAllPublicationsToNmcpJvmRepository

# Publish JS only
./gradlew publishAllPublicationsToNmcpJsRepository

# List all nmcp publish tasks
./gradlew tasks --all | grep nmcp
```

---

## Using Published Artifacts

### Gradle (Kotlin DSL)

**Multiplatform Project**:
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

**JVM Project**:
```kotlin
dependencies {
    implementation("dev.yidafu.feishu2html:feishu2html-jvm:1.0.0")
}
```

**JS Project**:
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

## CI/CD Configuration

### Using Environment Variables

In GitHub Actions or other CI/CD platforms:

```yaml
# .github/workflows/publish.yml
env:
  CENTRAL_USERNAME: ${{ secrets.CENTRAL_USERNAME }}
  CENTRAL_PASSWORD: ${{ secrets.CENTRAL_PASSWORD }}
  SIGNING_KEY: ${{ secrets.SIGNING_KEY }}
  SIGNING_PASSWORD: ${{ secrets.SIGNING_PASSWORD }}
```

Configure in GitHub Secrets:
- `CENTRAL_USERNAME` - Portal token username (required)
- `CENTRAL_PASSWORD` - Portal token password (required)
- `SIGNING_KEY` - GPG private key in ASCII armored format (required)
- `SIGNING_PASSWORD` - GPG key passphrase (required)

**Legacy environment variables** (deprecated but still supported):
- `OSSRH_USERNAME` → Use `CENTRAL_USERNAME` instead
- `OSSRH_PASSWORD` → Use `CENTRAL_PASSWORD` instead

---

## Common Commands

### Local Validation

```bash
# Verify POM generation
./gradlew generatePomFileForKotlinMultiplatformPublication

# View generated POM
cat build/publications/kotlinMultiplatform/pom-default.xml

# Publish to local Maven repository (for testing)
./gradlew publishToMavenLocal

# Check locally published files
ls ~/.m2/repository/dev/yidafu/feishu2html/
```

### Signing Tests

```bash
# Test signing configuration
./gradlew signKotlinMultiplatformPublication

# View signature files
find build/libs -name "*.asc"
```

### Publishing Tasks

```bash
# Main publishing command (recommended)
./gradlew nmcpPublish

# List all NMCP tasks
./gradlew tasks --all | grep nmcp

# Publish specific platforms
./gradlew publishAllPublicationsToNmcpJvmRepository
./gradlew publishAllPublicationsToNmcpJsRepository

# Legacy maven-publish tasks (not recommended for Central Portal)
./gradlew publish
```

---

## Migration from OSSRH

If you previously used OSSRH, here's what changed:

### What Changed

1. **New Portal**: Central Portal replaces OSSRH Nexus
2. **New URL**: `https://central.sonatype.com/api/v1/publisher/upload`
3. **New Tokens**: Generate Portal user tokens (old OSSRH tokens don't work)
4. **Same Credentials**: Your OSSRH username/password work for Portal login
5. **Migrated Namespaces**: All OSSRH namespaces were automatically migrated

### What Stays the Same

- ✅ GPG signing still required
- ✅ POM requirements unchanged
- ✅ Artifact structure unchanged
- ✅ Maven Central as final destination

### Quick Migration Steps

1. Log in to [Central Portal](https://central.sonatype.com/) with OSSRH credentials
2. Generate a new Portal user token
3. Update `local.properties` with new token
4. Change repository URL in `build.gradle.kts` (already done)
5. Publish as before

---

## Pre-publish Checklist

### First Time Publishing

- [ ] Central Portal account created
- [ ] Namespace verified and approved
- [ ] GPG key pair generated
- [ ] Public key uploaded to keyserver
- [ ] `local.properties` configured with `centralUsername`/`centralPassword`
- [ ] Test local publishing: `./gradlew publishToMavenLocal`

### Every Release

- [ ] Update version number
- [ ] Run all tests: `./gradlew jvmTest`
- [ ] Verify all platforms compile: `./gradlew build`
- [ ] Update CHANGELOG.md (if exists)
- [ ] Check POM information is correct

### Publishing SNAPSHOT

- [ ] Version ends with `-SNAPSHOT`
- [ ] Run: `./gradlew publish`
- [ ] Verify in Central Portal deployments

### Publishing Release

- [ ] Remove `-SNAPSHOT` from version
- [ ] Run: `./gradlew publish`
- [ ] Log in to Central Portal
- [ ] Review deployment in "Deployments" section
- [ ] Click "Publish" button
- [ ] Create Git tag: `git tag v1.0.0`
- [ ] Push tag: `git push origin v1.0.0`

---

## Troubleshooting

### Signing Failures

```bash
# Check GPG configuration
gpg --list-secret-keys

# Test signing
echo "test" | gpg --clearsign
```

### Upload Failures

```bash
# Check credentials
./gradlew publish --info

# Test network connectivity
curl -I https://central.sonatype.com/
```

### POM Validation Failures

Common issues:
- Missing `licenses` information
- Missing `developers` information
- Missing `scm` information
- Missing source and javadoc JARs

---

## Resources

- [Central Portal Documentation](https://central.sonatype.org/publish/)
- [OSSRH Sunset Notice](https://central.sonatype.org/pages/ossrh-eol/)
- [Generating Portal Tokens](https://central.sonatype.org/publish/generate-portal-token/)
- [GPG Signing Guide](https://central.sonatype.org/publish/requirements/gpg/)
- [Kotlin Multiplatform Publishing](https://kotlinlang.org/docs/multiplatform-publish-lib.html)
- [Gradle Maven Publish Plugin](https://docs.gradle.org/current/userguide/publishing_maven.html)

---

**Last Updated**: 2025-10-19  
**Portal**: Central Portal Publisher (replacing OSSRH)
