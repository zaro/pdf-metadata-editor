'use strict';

const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const { execFileSync } = require('node:child_process');

const NPM_ROOT = path.resolve(__dirname, '..');

test('stages the main package with the shared JAR and bundle dependencies', () => {
  const artifactsDir = fs.mkdtempSync(path.join(os.tmpdir(), 'pmedit-mcp-jar-'));
  const bundlesDir = fs.mkdtempSync(path.join(os.tmpdir(), 'pmedit-mcp-bundles-'));
  const outputDir = fs.mkdtempSync(path.join(os.tmpdir(), 'pmedit-mcp-stage-'));

  fs.writeFileSync(path.join(artifactsDir, 'pdf-metadata-editor-4.5.0rc1.jar'), 'jar');
  fs.writeFileSync(path.join(artifactsDir, 'original-pdf-metadata-editor-4.5.0rc1.jar'), 'original');

  for (const [key, name] of [
    ['linux-x64', '@pdf-metadata-editor/mcp-bundle-linux-x64'],
    ['darwin-arm64', '@pdf-metadata-editor/mcp-bundle-darwin-arm64']
  ]) {
    const directory = path.join(bundlesDir, key);
    fs.mkdirSync(directory, { recursive: true });
    fs.writeFileSync(
      path.join(directory, 'package.json'),
      JSON.stringify({ name, version: '4.5.0-rc.1' })
    );
  }

  execFileSync(process.execPath, [
    path.join(NPM_ROOT, 'scripts', 'stage-main-package.js'),
    '--artifacts', artifactsDir,
    '--bundles', bundlesDir,
    '--out', outputDir,
    '--npm-version', '4.5.0-rc.1',
    '--no-pack'
  ], { stdio: 'inherit' });

  const mainDir = path.join(outputDir, 'main');
  const pkg = JSON.parse(fs.readFileSync(path.join(mainDir, 'package.json'), 'utf8'));

  assert.equal(pkg.version, '4.5.0-rc.1');
  assert.deepEqual(pkg.optionalDependencies, {
    '@pdf-metadata-editor/mcp-bundle-darwin-arm64': '4.5.0-rc.1',
    '@pdf-metadata-editor/mcp-bundle-linux-x64': '4.5.0-rc.1'
  });
  assert.equal(fs.readFileSync(path.join(mainDir, 'lib', 'pdf-metadata-editor.jar'), 'utf8'), 'jar');
  assert.ok(fs.existsSync(path.join(mainDir, 'lib', 'version.js')));
  assert.ok(fs.existsSync(path.join(mainDir, 'bin', 'pdf-metadata-editor-mcp.js')));
  assert.ok(fs.existsSync(path.join(mainDir, 'LICENSE')));
  assert.ok(fs.existsSync(path.join(mainDir, 'NOTICE')));
  assert.ok(fs.existsSync(path.join(mainDir, 'server.json')));

  const serverJson = JSON.parse(fs.readFileSync(path.join(mainDir, 'server.json'), 'utf8'));
  assert.equal(serverJson.version, '4.5.0-rc.1');
  assert.equal(serverJson.packages[0].version, '4.5.0-rc.1');

  const pluginFiles = fs.readdirSync(path.join(mainDir, 'lib')).filter((file) => file.startsWith('pme-'));
  assert.deepEqual(pluginFiles, []);

  const text = fs.readFileSync(path.join(mainDir, 'package.json'), 'utf8');
  assert.doesNotMatch(text, /\b(pro|oss|professional|basic)\b/i);

  fs.rmSync(artifactsDir, { recursive: true, force: true });
  fs.rmSync(bundlesDir, { recursive: true, force: true });
  fs.rmSync(outputDir, { recursive: true, force: true });
});
