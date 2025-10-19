# Windows Native Usage Guide

## Overview

Feishu2HTML provides native Windows executable support, allowing you to run the converter directly on Windows without JVM or Node.js.

## Prerequisites

- **Windows**: 10 or higher (64-bit)
- **MinGW-w64**: For building the executable
- **MSYS2**: Recommended build environment

### Install MSYS2 and MinGW-w64

1. **Download and Install MSYS2**: https://www.msys2.org/

2. **Open MSYS2 MinGW 64-bit terminal**

3. **Install build tools**:
```bash
pacman -Syu
pacman -S mingw-w64-x86_64-gcc mingw-w64-x86_64-curl
```

## Building the Executable

### From MSYS2 Terminal

```bash
# Navigate to project directory
cd /c/path/to/feishu2html

# Build release executable
./gradlew linkReleaseExecutableMingwX64

# The executable will be at:
# build/bin/mingwX64/releaseExecutable/feishu2html.exe
```

### From Windows PowerShell/CMD

```powershell
# Build release executable
.\gradlew.bat linkReleaseExecutableMingwX64

# The executable will be at:
# build\bin\mingwX64\releaseExecutable\feishu2html.exe
```

### Debug Build

```bash
# Build debug executable (with debug symbols)
./gradlew linkDebugExecutableMingwX64

# Located at:
# build/bin/mingwX64/debugExecutable/feishu2html.exe
```

## Running the Application

### Basic Usage

```powershell
# Export a single document
.\build\bin\mingwX64\releaseExecutable\feishu2html.exe <app_id> <app_secret> <document_id>

# Export multiple documents
.\build\bin\mingwX64\releaseExecutable\feishu2html.exe <app_id> <app_secret> <doc_id_1> <doc_id_2> <doc_id_3>
```

### Example

```powershell
.\build\bin\mingwX64\releaseExecutable\feishu2html.exe `
    cli_a1234567890abcde `
    your_app_secret_here `
    doxcnABC123XYZ456
```

### Add to PATH

```powershell
# Add to user PATH (PowerShell)
$env:Path += ";$PWD\build\bin\mingwX64\releaseExecutable"

# Or add permanently via System Properties > Environment Variables
# Add: C:\path\to\feishu2html\build\bin\mingwX64\releaseExecutable

# Then run from anywhere
feishu2html.exe <app_id> <app_secret> <document_id>
```

## Verification

### Test Installation

```powershell
# 1. Build the executable
.\gradlew.bat linkReleaseExecutableMingwX64

# 2. Check the file exists
dir build\bin\mingwX64\releaseExecutable\feishu2html.exe

# 3. Run without arguments (should show help)
.\build\bin\mingwX64\releaseExecutable\feishu2html.exe

# 4. Test with real credentials
.\build\bin\mingwX64\releaseExecutable\feishu2html.exe `
    <your_app_id> `
    <your_app_secret> `
    <your_document_id>
```

### Expected Output

```
Feishu2HTML application started
Received 3 command line arguments
============================================================
Feishu Document to HTML Converter (Windows)
============================================================
App ID: cli_...
Documents to export: 1
============================================================

Initializing Feishu2Html with output directory: .\output
Starting document export process
Exporting single document: doxcn...
...
============================================================
Export completed!
Output directory: .\output
============================================================
Feishu2HTML application terminated
```

## Build Commands Reference

| Command | Description | Output Path |
|---------|-------------|-------------|
| `linkReleaseExecutableMingwX64` | Build release | `build\bin\mingwX64\releaseExecutable\feishu2html.exe` |
| `linkDebugExecutableMingwX64` | Build debug | `build\bin\mingwX64\debugExecutable\feishu2html.exe` |

## File Location

```
project\
└── build\
    └── bin\
        └── mingwX64\
            ├── releaseExecutable\
            │   └── feishu2html.exe    # Windows release binary
            └── debugExecutable\
                └── feishu2html.exe    # Windows debug binary
```

## Troubleshooting

### MinGW Not Found

**Error**: `Could not find MinGW-w64 installation`

**Solution**:
1. Install MSYS2 from https://www.msys2.org/
2. Install MinGW-w64: `pacman -S mingw-w64-x86_64-gcc`
3. Add to PATH: `C:\msys64\mingw64\bin`

### Missing DLLs

**Error**: `The code execution cannot proceed because XXX.dll was not found`

**Solution**: Copy required DLLs from MinGW to executable directory:
```powershell
# Copy MinGW runtime DLLs
copy C:\msys64\mingw64\bin\*.dll build\bin\mingwX64\releaseExecutable\
```

Common required DLLs:
- `libgcc_s_seh-1.dll`
- `libstdc++-6.dll`
- `libwinpthread-1.dll`

### Linker Errors

**Error**: Various linker errors during build

**Solution**:
```bash
# Clean and rebuild
./gradlew clean
./gradlew linkReleaseExecutableMingwX64
```

### Antivirus False Positive

Some antivirus software may flag the executable. Add an exception if needed.

## Performance

Native Windows executables offer:

- **Fast Startup**: Instant startup vs 2-3s for JVM
- **Low Memory**: ~60MB vs 200-300MB for JVM
- **Native Speed**: Compiled to x64 machine code
- **Small Binary**: ~15-20MB executable

## WSL (Windows Subsystem for Linux)

You can also build and run the Linux version in WSL:

```bash
# In WSL terminal
./gradlew linkReleaseExecutableLinuxX64
./build/bin/linuxX64/releaseExecutable/feishu2html.kexe <args>
```

## Distribution

### Portable Application

```powershell
# Create distribution folder
mkdir feishu2html-portable
copy build\bin\mingwX64\releaseExecutable\feishu2html.exe feishu2html-portable\
copy C:\msys64\mingw64\bin\libgcc_s_seh-1.dll feishu2html-portable\
copy C:\msys64\mingw64\bin\libstdc++-6.dll feishu2html-portable\
copy C:\msys64\mingw64\bin\libwinpthread-1.dll feishu2html-portable\

# Distribute the folder
# Users can run feishu2html.exe without installation
```

### Installer

You can create an installer using tools like:
- **Inno Setup**: Free installer creator
- **WiX Toolset**: MSI installer builder
- **NSIS**: Nullsoft Scriptable Install System

## CI/CD Integration

### GitHub Actions

```yaml
- name: Build Windows Executable
  runs-on: windows-latest
  steps:
    - uses: actions/checkout@v3
    - uses: actions/setup-java@v3
      with:
        java-version: '17'
    - name: Setup MSYS2
      uses: msys2/setup-msys2@v2
      with:
        msystem: MINGW64
        update: true
        install: mingw-w64-x86_64-gcc
    - name: Build
      run: .\gradlew.bat linkReleaseExecutableMingwX64
    - name: Upload artifact
      uses: actions/upload-artifact@v3
      with:
        name: feishu2html-windows
        path: build\bin\mingwX64\releaseExecutable\feishu2html.exe
```

## See Also

- [JVM Usage Guide](./jvm-usage.md) - Full-featured JVM platform
- [Node.js Usage Guide](./nodejs-usage.md) - JavaScript/Node.js usage
- [macOS Usage Guide](./macos-usage.md) - macOS native executable
- [Linux Usage Guide](./linux-usage.md) - Linux native executable
- [API Documentation](https://yidafu.github.io/feishu2html/)

## Notes

The Windows native executable provides a lightweight alternative to JVM or Node.js. Ideal for Windows automation, batch scripts, and integration with native Windows applications.

