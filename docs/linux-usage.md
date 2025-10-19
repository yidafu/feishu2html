# Linux Native Usage Guide

## Overview

Feishu2HTML provides native Linux executable support, allowing you to run the converter directly on Linux without JVM or Node.js.

## Prerequisites

- **Linux**: x86_64 architecture (64-bit)
- No additional dependencies required (standalone binary)

## Installation

### Download Pre-built Binary

Download the Linux x64 binary from the [latest release](https://github.com/yidafu/feishu2html/releases/latest):

```bash
# Download Linux x64 binary
curl -LO https://github.com/yidafu/feishu2html/releases/latest/download/feishu2html-1.0.1-linuxX64.tar.gz

# Extract
tar -xzf feishu2html-1.0.1-linuxX64.tar.gz

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

### Example

```bash
# Using local binary
./feishu2html.kexe \
    cli_a1234567890abcde \
    your_app_secret_here \
    doxcnABC123XYZ456

# Using system-installed binary
feishu2html \
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
Feishu Document to HTML Converter (Linux)
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

### GLIBC Version Issues

**Error**: `version 'GLIBC_X.XX' not found`

**Solution**: The executable requires a minimum GLIBC version. If you encounter this error:
- Update your system: `sudo apt-get update && sudo apt-get upgrade`
- Or use the JVM version instead, which works on all Linux systems

## Performance

Native Linux executables offer:

- **Fast Startup**: No JVM startup overhead (~50ms vs 2-3s)
- **Low Memory**: ~50MB vs 200-300MB for JVM
- **Native Speed**: Compiled to x64 machine code
- **Small Binary**: ~15-25MB standalone executable

## Docker Usage

You can easily run Feishu2HTML in a Docker container:

```dockerfile
FROM ubuntu:22.04

# Download pre-built binary
RUN apt-get update && apt-get install -y curl && \
    curl -LO https://github.com/yidafu/feishu2html/releases/latest/download/feishu2html-1.0.1-linuxX64.tar.gz && \
    tar -xzf feishu2html-1.0.1-linuxX64.tar.gz && \
    mv feishu2html.kexe /usr/local/bin/feishu2html && \
    chmod +x /usr/local/bin/feishu2html && \
    rm feishu2html-1.0.1-linuxX64.tar.gz

ENTRYPOINT ["feishu2html"]
```

```bash
docker build -t feishu2html-linux .
docker run feishu2html-linux <app_id> <app_secret> <document_id>
```

## See Also

- [JVM Usage Guide](./jvm-usage.md) - Full-featured JVM platform
- [Node.js Usage Guide](./nodejs-usage.md) - JavaScript/Node.js usage
- [macOS Usage Guide](./macos-usage.md) - macOS native executable
- [Windows Usage Guide](./windows-usage.md) - Windows native executable
- [API Documentation](https://yidafu.github.io/feishu2html/)

## Notes

The Linux native executable provides a lightweight, standalone option for running Feishu2HTML without JVM or Node.js runtime. Ideal for servers, containers, and embedded Linux systems.

