#!/usr/bin/env node

/**
 * Feishu2HTML Node.js launcher script (ES Module)
 *
 * This script loads the compiled Kotlin/JS application
 * Note: The build process automatically patches eval('require') to require
 */

import { fileURLToPath } from 'url';
import { dirname, join } from 'path';
import { createRequire } from 'module';

// Get __dirname equivalent in ES modules
const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

// Create require function for ES Module context
// This is needed because the patched code uses require() in ES Module
globalThis.require = createRequire(import.meta.url);

// Get the absolute path to the Kotlin build output
const mainModule = join(__dirname, '../build/js/packages/feishu2html/kotlin/feishu2html.mjs');

// Import and run the main application module
await import(mainModule);

// The main function is automatically called by the Kotlin/JS runtime

