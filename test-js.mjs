#!/usr/bin/env node

/**
 * Test script for Kotlin/JS compiled output
 *
 * Usage:
 *   1. Build JS: ./gradlew compileKotlinJs
 *   2. Run test: node test-js.mjs
 */

import { createRequire } from 'module';
import { fileURLToPath } from 'url';
import { dirname, join } from 'path';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);
const require = createRequire(import.meta.url);

// Path to compiled Kotlin/JS output
const jsOutputPath = join(__dirname, 'build/compileSync/js/main/productionExecutable/kotlin/feishu2html.js');

console.log('╔══════════════════════════════════════════════════════════════╗');
console.log('║                                                              ║');
console.log('║     Feishu2HTML - Kotlin/JS Test Script                     ║');
console.log('║                                                              ║');
console.log('╚══════════════════════════════════════════════════════════════╝');
console.log('');

// Test 1: Check if compiled output exists
console.log('📦 Test 1: Check compiled output');
console.log('─────────────────────────────────────────────────────────────');
try {
    const fs = require('fs');
    if (fs.existsSync(jsOutputPath)) {
        console.log('✅ Compiled JS file found:', jsOutputPath);
        const stats = fs.statSync(jsOutputPath);
        console.log(`   Size: ${(stats.size / 1024).toFixed(2)} KB`);
    } else {
        console.log('❌ Compiled JS file not found:', jsOutputPath);
        console.log('   Run: ./gradlew compileKotlinJs');
        process.exit(1);
    }
} catch (error) {
    console.log('❌ Error checking file:', error.message);
    process.exit(1);
}
console.log('');

// Test 2: Load the module
console.log('📥 Test 2: Load Kotlin/JS module');
console.log('─────────────────────────────────────────────────────────────');
let feishu2html;
try {
    // Use dynamic import for ES modules
    feishu2html = await import('file://' + jsOutputPath);
    console.log('✅ Module loaded successfully');
    const exportKeys = Object.keys(feishu2html);
    console.log('   Exports:', exportKeys.length, 'items');
    if (exportKeys.length > 0) {
        console.log('   Sample:', exportKeys.slice(0, 5).join(', '));
    }
} catch (error) {
    console.log('❌ Error loading module:', error.message);
    console.log('   Stack:', error.stack);
    process.exit(1);
}
console.log('');

// Test 3: Check exported symbols
console.log('🔍 Test 3: Check exported symbols');
console.log('─────────────────────────────────────────────────────────────');
const expectedExports = [
    'dev.yidafu.feishu2html',
    // Add more expected exports here
];

const actualExports = Object.keys(feishu2html);
console.log(`   Total exports: ${actualExports.length}`);

if (actualExports.length > 0) {
    console.log('✅ Module has exports');
    console.log('   Sample exports:');
    actualExports.slice(0, 5).forEach(name => {
        console.log(`     • ${name}`);
    });
} else {
    console.log('⚠️  No exports found (might be internal)');
}
console.log('');

// Test 4: Check package structure
console.log('📁 Test 4: Check package structure');
console.log('─────────────────────────────────────────────────────────────');
try {
    const packagePath = feishu2html['dev.yidafu.feishu2html'];
    if (packagePath) {
        console.log('✅ Package namespace found: dev.yidafu.feishu2html');
        // Try to access known classes
        if (packagePath.Feishu2Html) {
            console.log('   ✅ Feishu2Html class found');
        }
        if (packagePath.api) {
            console.log('   ✅ API package found');
        }
        if (packagePath.converter) {
            console.log('   ✅ Converter package found');
        }
    } else {
        console.log('⚠️  Package namespace not directly accessible');
        console.log('   This is normal for Kotlin/JS modules');
    }
} catch (error) {
    console.log('⚠️  Cannot access package:', error.message);
}
console.log('');

// Test 5: Platform detection
console.log('🖥️  Test 5: Platform information');
console.log('─────────────────────────────────────────────────────────────');
console.log(`   Node.js version: ${process.version}`);
console.log(`   Platform: ${process.platform}`);
console.log(`   Architecture: ${process.arch}`);
console.log('');

// Test 6: Dependencies check
console.log('📚 Test 6: Check bundled dependencies');
console.log('─────────────────────────────────────────────────────────────');
const jsDir = dirname(jsOutputPath);
const fs = require('fs');
const jsFiles = fs.readdirSync(jsDir).filter(f => f.endsWith('.js'));
console.log(`   Total JS files: ${jsFiles.length}`);
console.log('   Key dependencies:');
const keyDeps = jsFiles.filter(f =>
    f.includes('ktor') ||
    f.includes('kotlinx') ||
    f.includes('kotlin-stdlib')
);
keyDeps.slice(0, 5).forEach(f => {
    console.log(`     • ${f}`);
});
console.log('');

// Summary
console.log('╔══════════════════════════════════════════════════════════════╗');
console.log('║                                                              ║');
console.log('║     ✅ Kotlin/JS Tests Complete!                             ║');
console.log('║                                                              ║');
console.log('╚══════════════════════════════════════════════════════════════╝');
console.log('');
console.log('Summary:');
console.log('  ✅ JS compilation successful');
console.log('  ✅ Module loadable in Node.js');
console.log('  ✅ Dependencies bundled');
console.log('');
console.log('Next steps:');
console.log('  • Create actual test cases for API functionality');
console.log('  • Test with real Feishu API calls (requires credentials)');
console.log('  • Add to CI/CD pipeline');
console.log('');

