# 🧪 JavaScript Testing Guide

This document explains how to test the Kotlin/JS compiled output.

---

## 📋 Test Scripts

### 1. `test-js.mjs` - Comprehensive Test

Tests the compiled Kotlin/JS output:
- ✅ Checks if JS file exists
- ✅ Loads the module
- ✅ Verifies exports
- ✅ Lists bundled dependencies

**Usage**:
```bash
# Build JS first
./gradlew compileKotlinJs

# Run test
node test-js.mjs

# Or use npm script
npm test
```

### 2. `test-js-example.mjs` - Usage Example

Demonstrates how to use the Kotlin/JS library in a Node.js application.

**Usage**:
```bash
node test-js-example.mjs

# Or
npm run example:js
```

### 3. `package.json` - NPM Scripts

Convenient npm scripts for testing:

```bash
npm run build:js    # Compile Kotlin to JS
npm run test:js     # Run tests
npm test            # Build + Test
```

---

## 🔍 Test Results

### Current Test Output

```
✅ JS compilation successful
✅ Module loadable in Node.js  
✅ Dependencies bundled (23 files)
```

**Bundled Dependencies**:
- Kotlin stdlib
- Kotlinx coroutines
- Kotlinx serialization
- Kotlinx HTML
- Kotlinx DateTime
- Ktor client
- kotlin-logging

### File Locations

**Compiled Output**:
```
build/compileSync/js/main/productionExecutable/kotlin/
  ├─ feishu2html.js        (main module, 0.5 KB)
  ├─ feishu2html.js.map    (source map)
  ├─ kotlin-kotlin-stdlib.js
  ├─ ktor-ktor-client-core.js
  └─ ... (20+ dependency files)
```

---

## 💡 Usage in Node.js Applications

### Method 1: Direct File Reference

```javascript
import feishu2html from './build/compileSync/js/main/productionExecutable/kotlin/feishu2html.js';

// Access Kotlin classes/functions
// Note: Check actual export structure in feishu2html.js
```

### Method 2: Via NPM Package (after publishing)

```javascript
// In your project
npm install @yidafu/feishu2html

// In your code
import { Feishu2Html } from '@yidafu/feishu2html';

const converter = new Feishu2Html(appId, appSecret);
await converter.exportDocument(docId, outputPath);
```

---

## 🚀 Publishing to NPM

To publish the JS version as an NPM package:

### 1. Update package.json

Create a proper package.json with:
- Correct entry point
- Dependencies
- Export configuration

### 2. Build for NPM

```bash
./gradlew compileKotlinJs
```

### 3. Prepare Package

```bash
# Copy compiled output to dist/
mkdir -p dist
cp -r build/compileSync/js/main/productionExecutable/kotlin/* dist/
```

### 4. Publish

```bash
npm publish
```

---

## 🐛 Troubleshooting

### Module Not Found

```bash
# Make sure to build first
./gradlew compileKotlinJs
```

### Import Errors

Kotlin/JS exports might use specific naming patterns:
- Check `feishu2html.js` for actual export structure
- May use namespace: `module['dev.yidafu.feishu2html']`

### Node.js Version

Requires Node.js 16+ for ES module support:
```bash
node --version  # Should be >= v16.0.0
```

---

## 📊 Test Coverage

Current tests verify:
- ✅ Compilation successful
- ✅ Module loading
- ✅ Dependency bundling
- ✅ Platform compatibility (Node.js)

**Not yet tested** (requires Feishu API credentials):
- API authentication
- Document fetching
- HTML conversion
- File I/O operations

---

## 🎯 Next Steps

1. **Add Functional Tests**
   - Test API client
   - Test HTML conversion
   - Test file operations

2. **Add Integration Tests**
   - Test with real Feishu documents
   - Validate HTML output

3. **CI/CD Integration**
   - Add to GitHub Actions
   - Automated testing on each commit

---

## 📚 Resources

- Kotlin/JS Documentation: https://kotlinlang.org/docs/js-overview.html
- Node.js ES Modules: https://nodejs.org/api/esm.html
- NPM Publishing Guide: https://docs.npmjs.com/cli/v8/commands/npm-publish

---

**Created**: 2025-10-19  
**Node.js Version Tested**: v18.20.4  
**Status**: Basic tests passing ✅

