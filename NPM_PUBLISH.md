# NPM Publishing Guide

This document explains how to publish the Feishu2HTML JavaScript/CLI package to npm.

---

## Prerequisites

1. **npm account**: Create at https://www.npmjs.com/
2. **npm CLI**: Install Node.js (v16+) which includes npm
3. **npm login**: Run `npm login` and authenticate

---

## Building for NPM

### 1. Build the Kotlin/JS Module

```bash
# Compile Kotlin to JavaScript
./gradlew compileKotlinJs

# Or use npm script
npm run build
```

This generates:
- `build/compileSync/js/main/productionExecutable/kotlin/feishu2html.js` - Main module
- `build/compileSync/js/main/productionExecutable/kotlin/*.js` - Dependencies

### 2. Verify Package Contents

```bash
# Check what files will be published
npm pack --dry-run

# This should include:
# - bin/feishu2html (CLI wrapper)
# - build/compileSync/js/main/productionExecutable/kotlin/ (all JS files)
# - README.md
# - LICENSE
# - package.json
```

---

## Publishing to NPM

### Publish Process

```bash
# 1. Ensure you're logged in
npm whoami

# 2. Build the package
npm run build

# 3. Test locally before publishing
npm pack
# This creates feishu2html-1.0.0.tgz

# 4. Test installation from tarball
npm install -g ./feishu2html-1.0.0.tgz
feishu2html --help

# 5. Publish to npm
npm publish

# For first-time publishing
npm publish --access public
```

### Version Management

```bash
# Bump version before publishing
npm version patch  # 1.0.0 -> 1.0.1
npm version minor  # 1.0.0 -> 1.1.0
npm version major  # 1.0.0 -> 2.0.0

# This updates package.json and creates a git tag
# Then publish
npm publish
```

---

## Installation and Usage

### Global Installation

```bash
# Install globally
npm install -g feishu2html

# Use CLI anywhere
feishu2html <app_id> <app_secret> <document_id>
```

### Local Installation (as dependency)

```bash
# In your Node.js project
npm install feishu2html

# Use in code
import { Feishu2Html, Feishu2HtmlOptions } from 'feishu2html';

const options = {
    appId: 'your_app_id',
    appSecret: 'your_app_secret',
    outputDir: './output'
};

const converter = new Feishu2Html(options);
await converter.export('document_id');
```

### CLI Usage

```bash
# Export single document
feishu2html cli_a1234567890abcde cli_secret1234567890abcdef doxcnABCDEFGHIJK

# Export multiple documents
feishu2html <app_id> <app_secret> doc1 doc2 doc3

# Arguments:
#   app_id       - Feishu App ID (starts with cli_)
#   app_secret   - Feishu App Secret
#   document_id  - Document ID(s) to export (can specify multiple)
```

---

## Package Structure

```
feishu2html/
├── bin/
│   └── feishu2html              # CLI wrapper script
├── build/compileSync/js/main/productionExecutable/kotlin/
│   ├── feishu2html.js           # Main module
│   ├── kotlin-kotlin-stdlib.js  # Kotlin stdlib
│   ├── ktor-*.js                # Ktor HTTP client
│   └── kotlinx-*.js             # Kotlinx libraries
├── package.json
├── README.md
└── LICENSE
```

---

## NPM Scripts

```bash
npm run build         # Compile Kotlin/JS
npm run prepublishOnly # Auto-run before npm publish
npm test              # Run tests
```

---

## Publishing Checklist

### Before First Publish

- [ ] npm account created
- [ ] Package name available: `feishu2html`
- [ ] Build successful: `npm run build`
- [ ] CLI works locally: `node bin/feishu2html`
- [ ] README.md updated
- [ ] LICENSE file present
- [ ] .npmignore configured

### Every Publish

- [ ] Update version in package.json
- [ ] Run tests: `npm test`
- [ ] Build: `npm run build`
- [ ] Test pack: `npm pack --dry-run`
- [ ] Git commit and tag version
- [ ] Publish: `npm publish`
- [ ] Verify on npm: https://www.npmjs.com/package/feishu2html

---

## Troubleshooting

### "Module not found" after npm install

Make sure the `files` array in package.json includes all necessary JS output.

### CLI command not found after global install

Check that:
- `bin/feishu2html` has executable permissions
- `bin` entry in package.json is correct
- Shebang `#!/usr/bin/env node` is present

Fix permissions:
```bash
chmod +x bin/feishu2html
```

### Build fails

```bash
# Clean and rebuild
./gradlew clean
./gradlew compileKotlinJs
```

---

## NPM Registry URLs

After publishing, your package will be available at:
- **Package page**: https://www.npmjs.com/package/feishu2html
- **Install**: `npm install feishu2html`
- **CDN (unpkg)**: https://unpkg.com/feishu2html
- **CDN (jsDelivr)**: https://cdn.jsdelivr.net/npm/feishu2html

---

## Related Publishing

This package publishes the **JS/Node.js version** to npm.

For other platforms:
- **JVM/Kotlin/Java**: Published to Maven Central via NMCP
- **Native (macOS/Linux/Windows/iOS)**: Published to Maven Central

See `PUBLISHING.md` (if exists) for Maven Central publishing.

---

## Resources

- npm CLI documentation: https://docs.npmjs.com/cli/
- npm publishing guide: https://docs.npmjs.com/packages-and-modules/contributing-packages-to-the-registry
- Kotlin/JS documentation: https://kotlinlang.org/docs/js-project-setup.html

---

**Last Updated**: 2025-10-19  
**Package Name**: `feishu2html`  
**Registry**: npm (https://www.npmjs.com/)

