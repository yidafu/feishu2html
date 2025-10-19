# Node.js / JavaScript Usage Guide

## Overview

Feishu2HTML supports Node.js through **ES Module** format, providing a modern JavaScript development experience.

## Quick Start

### Installation

```bash
# Clone the repository
git clone https://github.com/yidafu/feishu2html.git
cd feishu2html

# Install dependencies
npm install

# Build the project
./gradlew compileKotlinJs jsProductionExecutableCompileSync
```

### Basic Usage

```bash
# Display help
./bin/feishu2html.mjs

# Export a single document
./bin/feishu2html.mjs <app_id> <app_secret> <document_id>

# Export multiple documents
./bin/feishu2html.mjs <app_id> <app_secret> <doc_id_1> <doc_id_2> <doc_id_3>
```

### Example

```bash
./bin/feishu2html.mjs cli_a1234567890abcde your_app_secret_here doxcnABC123XYZ456
```

## Configuration

### ES Module Format

The project uses ES Module (.mjs) format for modern JavaScript compatibility:

**build.gradle.kts:**
```kotlin
js(IR) {
    nodejs()
    binaries.executable()
    
    // ES Module configuration
    useEsModules()
    
    compilations.all {
        compileTaskProvider.configure {
            compilerOptions {
                moduleKind.set(org.jetbrains.kotlin.gradle.dsl.JsModuleKind.MODULE_ES)
            }
        }
    }
}
```

**package.json:**
```json
{
  "type": "module",
  "main": "./build/js/packages/feishu2html/kotlin/feishu2html.mjs",
  "bin": {
    "feishu2html": "./bin/feishu2html.mjs"
  },
  "dependencies": {
    "@js-joda/core": "^5.6.5",
    "abort-controller": "^3.0.0"
  }
}
```

## Project Structure

```
project/
├── package.json                      # type="module"
├── bin/
│   └── feishu2html.mjs              # Launch script (ES Module)
├── node_modules/
│   ├── @js-joda/                    # Kotlin DateTime dependency
│   └── abort-controller/            # Ktor dependency
└── build/
    └── js/packages/feishu2html/kotlin/
        ├── feishu2html.mjs          # Main file (ES Module)
        ├── kotlin-kotlin-stdlib.mjs # Kotlin stdlib
        ├── ktor-ktor-client-core.mjs # Ktor HTTP client
        └── ...                      # Other dependencies

✅ All files are in ES Module format (.mjs)
```

## Build Commands

```bash
# Full build
./gradlew clean compileKotlinJs jsProductionExecutableCompileSync

# Quick compilation
./gradlew compileKotlinJs

# Using npm
npm run build
```

## Platform-Specific Implementation

The Node.js file system operations use ES Module imports:

**src/jsMain/kotlin/dev/yidafu/feishu2html/platform/FileSystem.kt:**
```kotlin
@JsModule("fs")
@JsNonModule
external object NodeFs {
    fun mkdirSync(path: String, options: dynamic): dynamic
    fun existsSync(path: String): Boolean
    fun writeFileSync(path: String, data: dynamic, encoding: String? = definedExternally): Unit
}

@JsModule("path")
@JsNonModule
external object NodePath {
    fun dirname(path: String): String
}
```

## Dependencies

Required npm packages:

- **@js-joda/core**: Kotlin DateTime library dependency
- **abort-controller**: Ktor HTTP client dependency

Install with:
```bash
npm install
```

## Troubleshooting

### Error: `Cannot find module '@js-joda/core'`

**Solution:**
```bash
npm install
```

### Error: `Cannot find module 'abort-controller'`

**Solution:**
```bash
npm install abort-controller
```

### Build Issues

If you encounter build issues, try:
```bash
./gradlew clean
./gradlew compileKotlinJs jsProductionExecutableCompileSync
npm install
```

## Advanced Usage

### Using as npm Package

If you want to use feishu2html as an npm package in your project:

1. Install the package (when published):
```bash
npm install feishu2html
```

2. Use in your code:
```javascript
import { Feishu2Html } from 'feishu2html';

// Your code here
```

### Custom Build Configuration

You can customize the build by modifying `build.gradle.kts`:

```kotlin
js(IR) {
    nodejs()
    binaries.executable()
    
    // Custom configuration
    useEsModules()
    
    compilations.all {
        compileTaskProvider.configure {
            compilerOptions {
                moduleKind.set(org.jetbrains.kotlin.gradle.dsl.JsModuleKind.MODULE_ES)
                // Add more options as needed
            }
        }
    }
}
```

## Features

✅ **ES Module Format**: Modern JavaScript module system  
✅ **Type Safety**: Kotlin compile-time type checking  
✅ **Node.js 16+**: Native ES Module support  
✅ **Production Ready**: Tested and verified  
✅ **Cross-Platform**: >95% code shared with other platforms

## Limitations

- **Node.js Only**: Browser environment is not supported
- **Node.js 16+**: Requires Node.js version 16 or higher
- **Unix-like Systems**: Optimized for Linux/macOS (Windows support via WSL)

## See Also

- [JVM Usage Guide](./jvm-usage.md)
- [API Documentation](https://yidafu.github.io/feishu2html/)
- [Contributing Guide](../CONTRIBUTING.md)

