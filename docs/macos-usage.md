# macOS Native Usage Guide

> ✅ **Platform Status: Verified**  
> Successfully tested and verified on macOS (both Intel x64 and Apple Silicon ARM64)

## Overview

Feishu2HTML provides native macOS executable support, allowing you to run the converter directly on macOS without JVM or Node.js.

## Prerequisites

- **macOS**: 10.15 (Catalina) or higher
- **Architecture**: Supports both Intel (x64) and Apple Silicon (ARM64)

## Installation

### Download Pre-built Binary

Download the appropriate binary for your Mac from the [latest release](https://github.com/yidafu/feishu2html/releases/latest):

#### For Apple Silicon (M1/M2/M3/M4)
```bash
# Download macOS ARM64 binary
curl -LO https://github.com/yidafu/feishu2html/releases/latest/download/feishu2html-1.0.2-macosArm64.tar.gz

# Extract
tar -xzf feishu2html-1.0.2-macosArm64.tar.gz

# Make executable
chmod +x feishu2html.kexe
```

#### For Intel Macs
```bash
# Download macOS x64 binary
curl -LO https://github.com/yidafu/feishu2html/releases/latest/download/feishu2html-1.0.2-macosX64.tar.gz

# Extract
tar -xzf feishu2html-1.0.2-macosX64.tar.gz

# Make executable
chmod +x feishu2html.kexe
```

### Optional: Install to System Path

```bash
# Move to /usr/local/bin for system-wide access
sudo mv feishu2html.kexe /usr/local/bin/feishu2html

# Now you can run from anywhere
feishu2html <app_id> <app_secret> <document_id>
```

## Running the Application

### Basic Usage

```bash
# Export a single document
./feishu2html.kexe <app_id> <app_secret> <document_id>

# Export multiple documents
./feishu2html.kexe <app_id> <app_secret> <doc_id_1> <doc_id_2> <doc_id_3>

# If installed to system path
feishu2html <app_id> <app_secret> <document_id>
```

### Advanced Usage with Options

```bash
# Standalone HTML (embedded images and CSS)
./feishu2html.kexe --inline-images --inline-css <app_id> <app_secret> <document_id>

# Clean output (hide unsupported block warnings)
./feishu2html.kexe --hide-unsupported <app_id> <app_secret> <document_id>

# Custom HTML template
./feishu2html.kexe -t fragment <app_id> <app_secret> <document_id>

# All options combined
./feishu2html.kexe -t fragment --inline-images --inline-css --hide-unsupported <app_id> <app_secret> <document_id>
```

### CLI Options

```
Options:
  -t, --template <mode>   HTML template mode: default | fragment | full
                          default:  Standard Feishu template (default)
                          fragment: Minimal template with custom body wrapper
                          full:     Minimal template with basic HTML structure
  --inline-images         Embed images as base64 data URLs
  --inline-css            Embed CSS styles inline in <style> tag
  --hide-unsupported      Hide unsupported block type warnings
  -h, --help              Show help message
```

### Example

```bash
# Basic export
./feishu2html.kexe \
    cli_a1234567890abcde \
    your_app_secret_here \
    doxcnABC123XYZ456

# Standalone HTML for sharing
./feishu2html.kexe \
    --inline-images \
    --inline-css \
    --hide-unsupported \
    cli_a1234567890abcde \
    your_app_secret_here \
    doxcnABC123XYZ456
```

## Verification

### Test Installation

```bash
# 1. Check the file exists
ls -lh feishu2html.kexe

# 2. Run without arguments (should show help)
./feishu2html.kexe

# 3. Test with real credentials
./feishu2html.kexe \
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

## Troubleshooting

### Permission Denied

**Error**: `Permission denied` when running the executable

**Solution**:
```bash
chmod +x feishu2html.kexe
```

### Wrong Architecture

**Error**: `Bad CPU type in executable`

**Solution**: Download the correct binary for your Mac:
- Apple Silicon (M1/M2/M3/M4): Download `macosArm64` version
- Intel: Download `macosX64` version

Check your architecture:
```bash
uname -m
# arm64 = Apple Silicon
# x86_64 = Intel
```

### Gatekeeper Warning

**Error**: macOS blocks the executable with "unidentified developer" warning

**Solution**:
```bash
# Remove quarantine attribute
xattr -d com.apple.quarantine feishu2html.kexe

# Or allow in System Preferences
# System Preferences > Security & Privacy > General > Allow
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

