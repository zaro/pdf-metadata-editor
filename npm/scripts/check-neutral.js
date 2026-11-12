#!/usr/bin/env node
'use strict';

const fs = require('node:fs');
const path = require('node:path');
const { spawnSync } = require('node:child_process');

const NPM_ROOT = path.resolve(__dirname, '..');

const FORBIDDEN = /\b(pro|oss|professional|basic)\b/i;

const FILES = [
  'package.json',
  'README.md',
  'NOTICE',
  'server.json',
  'bin/pdf-metadata-editor-mcp.js',
  'lib/version.js'
];

function checkText(label, text) {
  const lines = String(text).split('\n');
  for (let i = 0; i < lines.length; i += 1) {
    if (FORBIDDEN.test(lines[i])) {
      throw new Error(`${label}:${i + 1}: ${lines[i].trim()}`);
    }
  }
}

function checkFile(file) {
  checkText(file, fs.readFileSync(path.join(NPM_ROOT, file), 'utf8'));
}

function checkCommand(args) {
  const result = spawnSync(process.execPath, [path.join(NPM_ROOT, 'bin', 'pdf-metadata-editor-mcp.js'), ...args], {
    encoding: 'utf8'
  });
  if (result.error) {
    throw new Error(`Failed to run ${args.join(' ')}: ${result.error.message}`);
  }
  checkText(`wrapper ${args.join(' ')}`, `${result.stdout || ''}\n${result.stderr || ''}`);
}

function main() {
  for (const file of FILES) {
    checkFile(file);
  }

  checkCommand(['--help']);
  checkCommand(['--version']);

  process.stdout.write('Edition-neutral wording check passed.\n');
}

if (require.main === module) {
  try {
    main();
  } catch (error) {
    process.stderr.write(`check-neutral: ${error.message}\n`);
    process.exit(1);
  }
}

module.exports = { FILES, FORBIDDEN, checkCommand, checkFile, checkText };
