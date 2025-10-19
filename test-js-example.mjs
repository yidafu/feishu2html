#!/usr/bin/env node

/**
 * Example: How to use Feishu2HTML Kotlin/JS library
 *
 * This demonstrates how to import and use the compiled Kotlin/JS module
 * in a Node.js application.
 *
 * Prerequisites:
 *   1. Build JS: ./gradlew compileKotlinJs
 *   2. Install Node.js (v16+)
 *
 * Usage:
 *   node test-js-example.mjs
 */

import { createRequire } from 'module';
import { fileURLToPath } from 'url';
import { dirname, join } from 'path';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);
const require = createRequire(import.meta.url);

console.log('Feishu2HTML Kotlin/JS Example\n');

// Load the compiled Kotlin/JS module
const jsOutputPath = join(__dirname, 'build/compileSync/js/main/productionExecutable/kotlin/feishu2html.js');

try {
    // Import the module
    const feishu2html = require(jsOutputPath);

    console.log('✅ Module loaded successfully\n');

    // Example 1: Check module structure
    console.log('📦 Module structure:');
    console.log('   Exports:', Object.keys(feishu2html).length, 'items');

    // Example 2: Access Kotlin package
    // Note: Kotlin/JS exports follow a specific pattern
    // Usually: module['package.name']

    // Example 3: Usage patterns
    console.log('\n📝 Usage patterns:');
    console.log('');
    console.log('// In your Node.js application:');
    console.log('const feishu2html = require("./path/to/feishu2html.js");');
    console.log('');
    console.log('// Access Kotlin classes/functions:');
    console.log('// const Feishu2Html = feishu2html["dev.yidafu.feishu2html"].Feishu2Html;');
    console.log('');
    console.log('// Or if publishing as npm package:');
    console.log('// npm install @yidafu/feishu2html');
    console.log('// import { Feishu2Html } from "@yidafu/feishu2html";');

    console.log('\n✅ Example complete!');
    console.log('\n📚 For actual API usage:');
    console.log('   1. Set up Feishu API credentials');
    console.log('   2. Initialize Feishu2Html with app_id and app_secret');
    console.log('   3. Call exportDocument(docId, outputPath)');

} catch (error) {
    console.error('❌ Error:', error.message);
    console.error('\n💡 Make sure to run: ./gradlew compileKotlinJs');
    process.exit(1);
}

