'use strict';

const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const { execFileSync } = require('node:child_process');

const {
  PLATFORMS,
  createBundle,
  findLauncher,
  parseArgs
} = require('../scripts/package-bundles.js');

function temporaryDirectory(prefix) {
  return fs.mkdtempSync(path.join(os.tmpdir(), prefix));
}

test('parses packaging arguments', () => {
  const args = parseArgs([
    '--artifacts', 'release-assets',
    '--out', 'dist',
    '--version=4.5.0rc1',
    '--platform', 'linux-x64',
    '--no-pack'
  ]);

  assert.equal(args.artifacts, 'release-assets');
  assert.equal(args.out, 'dist');
  assert.equal(args.version, '4.5.0rc1');
  assert.deepEqual(args.platform, ['linux-x64']);
  assert.equal(args.noPack, true);
});

test('finds the CLI launcher inside an app image', () => {
  const appDir = temporaryDirectory('pmedit-mcp-app-');
  const launcher = path.join(appDir, 'Pdf Metadata Editor', 'bin', 'pmedit-cli');
  fs.mkdirSync(path.dirname(launcher), { recursive: true });
  fs.writeFileSync(launcher, '#!/bin/sh\n', 'utf8');

  const found = findLauncher(appDir);
  assert.equal(found, fs.realpathSync(launcher));

  fs.rmSync(appDir, { recursive: true, force: true });
});

test('creates a Linux bundle from a portable archive', { skip: process.platform === 'win32' }, () => {
  const artifactsDir = temporaryDirectory('pmedit-mcp-artifacts-');
  const sourceDir = temporaryDirectory('pmedit-mcp-source-');
  const outputDir = temporaryDirectory('pmedit-mcp-output-');
  const appDir = path.join(sourceDir, 'Pdf Metadata Editor');
  const launcher = path.join(appDir, 'bin', 'pmedit-cli');
  fs.mkdirSync(path.dirname(launcher), { recursive: true });
  fs.writeFileSync(launcher, '#!/bin/sh\nexit 0\n', 'utf8');

  const mavenVersion = '4.5.0rc1';
  const platform = PLATFORMS.find((entry) => entry.key === 'linux-x64');
  const archive = path.join(artifactsDir, platform.archive(mavenVersion));
  execFileSync('tar', ['-czf', archive, '-C', sourceDir, 'Pdf Metadata Editor']);

  const bundleDir = createBundle(
    platform,
    artifactsDir,
    outputDir,
    mavenVersion,
    '4.5.0-rc.1'
  );

  const manifest = JSON.parse(fs.readFileSync(path.join(bundleDir, 'manifest.json'), 'utf8'));
  assert.equal(manifest.launcher, 'app/Pdf Metadata Editor/bin/pmedit-cli');
  assert.equal(manifest.sourceArtifact, `pdf-metadata-editor-${mavenVersion}-amd64-linux-portable.tar.gz`);
  assert.match(manifest.sha256, /^[0-9a-f]{64}$/);

  const pkg = JSON.parse(fs.readFileSync(path.join(bundleDir, 'package.json'), 'utf8'));
  assert.equal(pkg.name, '@pdf-metadata-editor/mcp-bundle-linux-x64');
  assert.equal(pkg.version, '4.5.0-rc.1');
  assert.equal(pkg.license, 'GPL-3.0');
  assert.deepEqual(pkg.os, ['linux']);
  assert.deepEqual(pkg.cpu, ['x64']);
  assert.ok(fs.existsSync(path.join(bundleDir, 'app', 'Pdf Metadata Editor', 'bin', 'pmedit-cli')));

  const text = `${JSON.stringify(manifest)}\n${JSON.stringify(pkg)}`;
  assert.doesNotMatch(text, /\b(pro|oss|professional|basic)\b/i);

  fs.rmSync(artifactsDir, { recursive: true, force: true });
  fs.rmSync(sourceDir, { recursive: true, force: true });
  fs.rmSync(outputDir, { recursive: true, force: true });
});
