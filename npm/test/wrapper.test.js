'use strict';

const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');

const {
  bundlePackageName,
  parseJavaVersion,
  platformKey,
  toNpmVersion
} = require('../lib/version.js');

const {
  buildFallbackArgs,
  helpText,
  normalizeArgs,
  readManifest,
  rootFromArgs
} = require('../bin/pdf-metadata-editor-mcp.js');

test('converts Maven prerelease versions to npm semver', () => {
  assert.equal(toNpmVersion('4.5.0rc1'), '4.5.0-rc.1');
  assert.equal(toNpmVersion('4.5.0-rc1'), '4.5.0-rc.1');
  assert.equal(toNpmVersion('4.5.0-rc.1'), '4.5.0-rc.1');
  assert.equal(toNpmVersion('4.5.0'), '4.5.0');
  assert.equal(toNpmVersion('4.4.2'), '4.4.2');
});

test('parses Java versions', () => {
  assert.equal(parseJavaVersion('openjdk version "17.0.9" 2023-10-17'), 17);
  assert.equal(parseJavaVersion('java version "21.0.2" 2024-01-16'), 21);
  assert.equal(parseJavaVersion('java version "1.8.0_402"'), 8);
  assert.equal(parseJavaVersion('not java'), null);
});

test('maps platforms to bundle package names', () => {
  assert.equal(platformKey('linux', 'x64'), 'linux-x64');
  assert.equal(platformKey('win32', 'arm64'), 'win32-arm64');
  assert.equal(bundlePackageName('darwin', 'arm64'), '@pdf-metadata-editor/mcp-bundle-darwin-arm64');
});

test('normalizes forwarded arguments', () => {
  assert.deepEqual(normalizeArgs(['--mcp', '--mcp-root', '/x']), ['--mcp-root', '/x']);
});

test('builds fallback JVM arguments', () => {
  const args = buildFallbackArgs('/tmp/app.jar', ['--mcp-root', '/tmp/pdfs']);

  assert.ok(args.includes('-DnoGui'));
  assert.ok(args.includes('-Djava.awt.headless=true'));
  assert.ok(args.includes('--enable-native-access=ALL-UNNAMED'));
  assert.deepEqual(args.slice(-3), ['--mcp', '--mcp-root', '/tmp/pdfs']);
  assert.equal(args.filter((arg) => arg === '--mcp').length, 1);
});

test('parses the root argument', () => {
  assert.equal(rootFromArgs(['--mcp-root', '/tmp/pdfs']), path.resolve('/tmp/pdfs'));
  assert.equal(rootFromArgs(['--mcp-root=/tmp/pdfs']), path.resolve('/tmp/pdfs'));
  assert.equal(rootFromArgs([]), process.cwd());
});

test('help text is edition-neutral', () => {
  assert.doesNotMatch(helpText(), /\b(pro|oss|professional|basic)\b/i);
});

test('reads a bundle manifest', () => {
  const directory = fs.mkdtempSync(path.join(os.tmpdir(), 'pmedit-mcp-manifest-'));
  const launcher = path.join(directory, 'bin', 'pmedit-cli');
  fs.mkdirSync(path.dirname(launcher), { recursive: true });
  fs.writeFileSync(launcher, '#!/bin/sh\n', 'utf8');
  fs.writeFileSync(
    path.join(directory, 'manifest.json'),
    JSON.stringify({ launcher: 'bin/pmedit-cli' }),
    'utf8'
  );

  const result = readManifest(directory);
  assert.equal(result.launcher, launcher);

  fs.rmSync(directory, { recursive: true, force: true });
});
