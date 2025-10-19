# macOS Native Usage Guide

> ✅ **Platform Status: Verified**  
> Successfully tested and verified on macOS (both Intel x64 and Apple Silicon ARM64)

## Overview

Feishu2HTML provides native macOS executable support, allowing you to run the converter directly on macOS without JVM or Node.js.

## Prerequisites

- **macOS**: 10.15 (Catalina) or higher
- **Xcode**: Latest version with Command Line Tools installed
- **Architecture**: Supports both Intel (x64) and Apple Silicon (ARM64)

### Install Xcode Command Line Tools

```bash
# Check if already installed
xcode-select -p

# Install if needed
xcode-select --install

# Verify installation
xcodebuild -version
```

## Building the Executable

### For Apple Silicon (M1/M2/M3)

```bash
# Build release executable
./gradlew linkReleaseExecutableMacosArm64

# The executable will be at:
# build/bin/macosArm64/releaseExecutable/feishu2html.kexe
```

### For Intel Macs

```bash
# Build release executable
./gradlew linkReleaseExecutableMacosX64

# The executable will be at:
# build/bin/macosX64/releaseExecutable/feishu2html.kexe
```

### Debug Build

```bash
# Build debug executable (with debug symbols)
./gradlew linkDebugExecutableMacosArm64

# Located at:
# build/bin/macosArm64/debugExecutable/feishu2html.kexe
```

## Running the Application

### Basic Usage

```bash
# Export a single document
./build/bin/macosArm64/releaseExecutable/feishu2html.kexe <app_id> <app_secret> <document_id>

# Export multiple documents
./build/bin/macosArm64/releaseExecutable/feishu2html.kexe <app_id> <app_secret> <doc_id_1> <doc_id_2> <doc_id_3>
```

### Example

```bash
./build/bin/macosArm64/releaseExecutable/feishu2html.kexe \
    cli_a1234567890abcde \
    your_app_secret_here \
    doxcnABC123XYZ456
```

### Create Symlink for Easy Access

```bash
# Create symlink to /usr/local/bin
sudo ln -s "$(pwd)/build/bin/macosArm64/releaseExecutable/feishu2html.kexe" /usr/local/bin/feishu2html

# Now you can run from anywhere
feishu2html <app_id> <app_secret> <document_id>
```

## Verification

### Test Installation

```bash
# 1. Build the executable
./gradlew linkReleaseExecutableMacosArm64

# 2. Check the file exists
ls -lh build/bin/macosArm64/releaseExecutable/feishu2html.kexe

# 3. Make it executable (should already be)
chmod +x build/bin/macosArm64/releaseExecutable/feishu2html.kexe

# 4. Run without arguments (should show help)
./build/bin/macosArm64/releaseExecutable/feishu2html.kexe

# 5. Test with real credentials
./build/bin/macosArm64/releaseExecutable/feishu2html.kexe \
    <your_app_id> \
    <your_app_secret> \
    <your_document_id>
```

### Expected Output

```
Feishu2HTML application started
Received 3 command line arguments
============================================================
Feishu Document to HTML Converter (macOS)
============================================================
App ID: cli_...
Documents to export: 1
============================================================

Initializing Feishu2Html with output directory: ./output
Starting document export process
Exporting single document: doxcn...
...
============================================================
Export completed!
Output directory: ./output
============================================================
Feishu2HTML application terminated
```

## Build Commands Reference

| Command | Description | Output Path |
|---------|-------------|-------------|
| `linkReleaseExecutableMacosArm64` | Build ARM64 release | `build/bin/macosArm64/releaseExecutable/feishu2html.kexe` |
| `linkDebugExecutableMacosArm64` | Build ARM64 debug | `build/bin/macosArm64/debugExecutable/feishu2html.kexe` |
| `linkReleaseExecutableMacosX64` | Build x64 release | `build/bin/macosX64/releaseExecutable/feishu2html.kexe` |
| `linkDebugExecutableMacosX64` | Build x64 debug | `build/bin/macosX64/debugExecutable/feishu2html.kexe` |

## File Locations

```
project/
└── build/
    └── bin/
        ├── macosArm64/
        │   ├── releaseExecutable/
        │   │   └── feishu2html.kexe    # ARM64 release binary
        │   └── debugExecutable/
        │       └── feishu2html.kexe    # ARM64 debug binary
        └── macosX64/
            ├── releaseExecutable/
            │   └── feishu2html.kexe    # Intel release binary
            └── debugExecutable/
                └── feishu2html.kexe    # Intel debug binary
```

## Configuration Options

The macOS executable uses the same `Feishu2HtmlOptions` as other platforms:

```kotlin
val options = Feishu2HtmlOptions(
    appId = "your_app_id",
    appSecret = "your_app_secret",
    outputDir = "./output",          // HTML output directory
    imageDir = "./output/images",    // Image save directory
    fileDir = "./output/files",      // Attachment save directory
    imagePath = "images",            // Relative path for images in HTML
    filePath = "files",              // Relative path for files in HTML
)
```

## Troubleshooting

### Xcode Command Line Tools Not Found

**Error**: `An error occurred during an xcrun execution`

**Solution**:
```bash
# Install Xcode Command Line Tools
xcode-select --install

# If already installed, reset the path
sudo xcode-select --reset

# Verify
xcodebuild -version
```

### Permission Denied

**Error**: `Permission denied` when running the executable

**Solution**:
```bash
chmod +x build/bin/macosArm64/releaseExecutable/feishu2html.kexe
```

### Wrong Architecture

**Error**: `Bad CPU type in executable`

**Solution**: Build for the correct architecture:
- Apple Silicon (M1/M2/M3): Use `macosArm64`
- Intel: Use `macosX64`

Check your architecture:
```bash
uname -m
# arm64 = Apple Silicon
# x86_64 = Intel
```

### Linker Errors

**Error**: Various linker errors during build

**Solution**:
```bash
# Clean and rebuild
./gradlew clean
./gradlew linkReleaseExecutableMacosArm64
```

### Memory Issues

If building fails with out-of-memory errors:

```bash
# Increase Gradle memory
./gradlew linkReleaseExecutableMacosArm64 -Dorg.gradle.jvmargs="-Xmx4g"
```

## Performance

Native executables offer excellent performance:

- **Fast Startup**: No JVM startup overhead
- **Low Memory**: Direct memory management
- **Native Speed**: Compiled to machine code
- **Small Binary**: ~10-20MB executable (vs JVM JAR with runtime)

## Platform-Specific Features

The macOS implementation includes:

- ✅ **POSIX File I/O**: Direct system calls
- ✅ **Darwin HTTP Engine**: Native Ktor HTTP client
- ✅ **Memory Safety**: Kotlin Native memory model
- ✅ **Cross-Platform Code**: >95% shared with other platforms

## Distribution

### Single Binary Distribution

```bash
# Build release
./gradlew linkReleaseExecutableMacosArm64

# Copy executable
cp build/bin/macosArm64/releaseExecutable/feishu2html.kexe ~/Desktop/feishu2html

# Distribute the single file
# Users can run it directly without any dependencies
```

### Universal Binary (Optional)

To create a universal binary for both architectures:

```bash
# Build both
./gradlew linkReleaseExecutableMacosArm64
./gradlew linkReleaseExecutableMacosX64

# Create universal binary
lipo -create \
    build/bin/macosArm64/releaseExecutable/feishu2html.kexe \
    build/bin/macosX64/releaseExecutable/feishu2html.kexe \
    -output feishu2html-universal
```

## Limitations

- **Xcode Required**: Building requires Xcode Command Line Tools
- **macOS Only**: Cannot cross-compile from other platforms
- **Experimental**: Native platform support is in experimental stage

## Advanced Usage

### Environment Variables

```bash
# Use environment variables for credentials
export FEISHU_APP_ID="cli_xxx"
export FEISHU_APP_SECRET="secret_xxx"

# Modify Main.kt to read from env vars if needed
```

### Script Integration

```bash
#!/bin/bash
# batch-export.sh

EXECUTABLE="./build/bin/macosArm64/releaseExecutable/feishu2html.kexe"
APP_ID="your_app_id"
APP_SECRET="your_app_secret"

while IFS= read -r doc_id; do
    echo "Exporting: $doc_id"
    $EXECUTABLE "$APP_ID" "$APP_SECRET" "$doc_id"
done < document-list.txt
```

## See Also

- [JVM Usage Guide](./jvm-usage.md) - Full-featured JVM platform
- [Node.js Usage Guide](./nodejs-usage.md) - JavaScript/Node.js usage
- [API Documentation](https://yidafu.github.io/feishu2html/)
- [Contributing Guide](../CONTRIBUTING.md)

## Notes

The macOS native executable provides a lightweight, standalone option for running Feishu2HTML without requiring JVM or Node.js runtime. Perfect for integration into native macOS applications or shell scripts.

