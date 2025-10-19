# Linux Native Usage Guide

## Overview

Feishu2HTML provides native Linux executable support, allowing you to run the converter directly on Linux without JVM or Node.js.

## Prerequisites

- **Linux**: x86_64 architecture (64-bit)
- **GCC/Clang**: C/C++ compiler toolchain
- **curl**: HTTP client library (usually pre-installed)

### Install Build Tools

**Ubuntu/Debian:**
```bash
sudo apt-get update
sudo apt-get install build-essential libcurl4-openssl-dev
```

**Fedora/RHEL/CentOS:**
```bash
sudo dnf groupinstall "Development Tools"
sudo dnf install libcurl-devel
```

**Arch Linux:**
```bash
sudo pacman -S base-devel curl
```

## Building the Executable

```bash
# Build release executable
./gradlew linkReleaseExecutableLinuxX64

# The executable will be at:
# build/bin/linuxX64/releaseExecutable/feishu2html.kexe
```

### Debug Build

```bash
# Build debug executable (with debug symbols)
./gradlew linkDebugExecutableLinuxX64

# Located at:
# build/bin/linuxX64/debugExecutable/feishu2html.kexe
```

## Running the Application

### Basic Usage

```bash
# Export a single document
./build/bin/linuxX64/releaseExecutable/feishu2html.kexe <app_id> <app_secret> <document_id>

# Export multiple documents
./build/bin/linuxX64/releaseExecutable/feishu2html.kexe <app_id> <app_secret> <doc_id_1> <doc_id_2> <doc_id_3>
```

### Example

```bash
./build/bin/linuxX64/releaseExecutable/feishu2html.kexe \
    cli_a1234567890abcde \
    your_app_secret_here \
    doxcnABC123XYZ456
```

### Install System-wide

```bash
# Copy to /usr/local/bin
sudo cp build/bin/linuxX64/releaseExecutable/feishu2html.kexe /usr/local/bin/feishu2html

# Or create symlink
sudo ln -s "$(pwd)/build/bin/linuxX64/releaseExecutable/feishu2html.kexe" /usr/local/bin/feishu2html

# Now you can run from anywhere
feishu2html <app_id> <app_secret> <document_id>
```

## Verification

### Test Installation

```bash
# 1. Build the executable
./gradlew linkReleaseExecutableLinuxX64

# 2. Check the file exists
ls -lh build/bin/linuxX64/releaseExecutable/feishu2html.kexe

# 3. Make it executable (should already be)
chmod +x build/bin/linuxX64/releaseExecutable/feishu2html.kexe

# 4. Run without arguments (should show help)
./build/bin/linuxX64/releaseExecutable/feishu2html.kexe

# 5. Test with real credentials
./build/bin/linuxX64/releaseExecutable/feishu2html.kexe \
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

## Build Commands Reference

| Command | Description | Output Path |
|---------|-------------|-------------|
| `linkReleaseExecutableLinuxX64` | Build release | `build/bin/linuxX64/releaseExecutable/feishu2html.kexe` |
| `linkDebugExecutableLinuxX64` | Build debug | `build/bin/linuxX64/debugExecutable/feishu2html.kexe` |

## File Location

```
project/
└── build/
    └── bin/
        └── linuxX64/
            ├── releaseExecutable/
            │   └── feishu2html.kexe    # Linux release binary
            └── debugExecutable/
                └── feishu2html.kexe    # Linux debug binary
```

## Troubleshooting

### Missing libcurl

**Error**: `libcurl.so.4: cannot open shared object file`

**Solution**:
```bash
# Ubuntu/Debian
sudo apt-get install libcurl4

# Fedora/RHEL
sudo dnf install libcurl

# Arch
sudo pacman -S curl
```

### Permission Denied

**Error**: `Permission denied` when running the executable

**Solution**:
```bash
chmod +x build/bin/linuxX64/releaseExecutable/feishu2html.kexe
```

### Linker Errors

**Error**: Various linker errors during build

**Solution**:
```bash
# Install build tools
sudo apt-get install build-essential

# Clean and rebuild
./gradlew clean
./gradlew linkReleaseExecutableLinuxX64
```

### GLIBC Version Issues

**Error**: `version 'GLIBC_X.XX' not found`

**Solution**: The executable is compiled for your current system's GLIBC version. To run on older systems, build on a machine with an older GLIBC version.

## Performance

Native Linux executables offer:

- **Fast Startup**: No JVM startup overhead (~50ms vs 2-3s)
- **Low Memory**: ~50MB vs 200-300MB for JVM
- **Native Speed**: Compiled to x64 machine code
- **Small Binary**: ~15-25MB standalone executable

## Docker Usage

### Build in Docker

```dockerfile
FROM ubuntu:22.04

RUN apt-get update && apt-get install -y \
    openjdk-17-jdk \
    build-essential \
    libcurl4-openssl-dev

WORKDIR /app
COPY . .

RUN ./gradlew linkReleaseExecutableLinuxX64

# Extract executable
RUN cp build/bin/linuxX64/releaseExecutable/feishu2html.kexe /usr/local/bin/feishu2html

ENTRYPOINT ["feishu2html"]
```

### Run in Container

```bash
docker build -t feishu2html-linux .
docker run feishu2html-linux <app_id> <app_secret> <document_id>
```

## Distribution

### Single Binary

```bash
# Build
./gradlew linkReleaseExecutableLinuxX64

# Distribute the single file
cp build/bin/linuxX64/releaseExecutable/feishu2html.kexe ~/Desktop/

# Users can run it directly
./feishu2html.kexe <args>
```

### Package as DEB/RPM

You can package the executable for easier distribution:

**DEB package (Debian/Ubuntu):**
```bash
# Create package structure
mkdir -p feishu2html_1.0.0/usr/local/bin
cp build/bin/linuxX64/releaseExecutable/feishu2html.kexe feishu2html_1.0.0/usr/local/bin/feishu2html

# Create control file and build
dpkg-deb --build feishu2html_1.0.0
```

## CI/CD Integration

### GitHub Actions

```yaml
- name: Build Linux Executable
  runs-on: ubuntu-latest
  steps:
    - uses: actions/checkout@v3
    - uses: actions/setup-java@v3
      with:
        java-version: '17'
    - name: Build
      run: ./gradlew linkReleaseExecutableLinuxX64
    - name: Upload artifact
      uses: actions/upload-artifact@v3
      with:
        name: feishu2html-linux
        path: build/bin/linuxX64/releaseExecutable/feishu2html.kexe
```

## See Also

- [JVM Usage Guide](./jvm-usage.md) - Full-featured JVM platform
- [Node.js Usage Guide](./nodejs-usage.md) - JavaScript/Node.js usage
- [macOS Usage Guide](./macos-usage.md) - macOS native executable
- [Windows Usage Guide](./windows-usage.md) - Windows native executable
- [API Documentation](https://yidafu.github.io/feishu2html/)

## Notes

The Linux native executable provides a lightweight, standalone option for running Feishu2HTML without JVM or Node.js runtime. Ideal for servers, containers, and embedded Linux systems.

