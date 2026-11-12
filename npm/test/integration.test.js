'use strict';

const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const { spawn, spawnSync } = require('node:child_process');

const NPM_ROOT = path.resolve(__dirname, '..');
const WRAPPER = path.join(NPM_ROOT, 'bin', 'pdf-metadata-editor-mcp.js');
const FIXTURE = path.join(__dirname, 'fixtures', 'sample.pdf');

function builtJar() {
  const directory = path.resolve(NPM_ROOT, '..', 'modules', 'editor-gui', 'target', 'jar');
  if (!fs.existsSync(directory)) {
    return null;
  }
  const file = fs.readdirSync(directory).find((name) => {
    return name.startsWith('pdf-metadata-editor-') && name.endsWith('.jar') && !name.startsWith('original-');
  });
  return file ? path.join(directory, file) : null;
}

function javaAvailable(java) {
  const result = spawnSync(java, ['-version'], { encoding: 'utf8' });
  return !result.error && result.status === 0;
}

function runWrapper(args, environment, input) {
  return new Promise((resolve, reject) => {
    const child = spawn(process.execPath, [WRAPPER, ...args], {
      env: environment,
      stdio: ['pipe', 'pipe', 'pipe']
    });

    let stdout = '';
    let stderr = '';
    child.stdout.setEncoding('utf8');
    child.stderr.setEncoding('utf8');
    child.stdout.on('data', (chunk) => { stdout += chunk; });
    child.stderr.on('data', (chunk) => { stderr += chunk; });
    child.on('error', reject);
    child.on('close', (code) => resolve({ code, stdout, stderr }));

    if (input) {
      child.stdin.end(input);
    } else {
      child.stdin.end();
    }
  });
}

const jar = process.env.PDF_METADATA_EDITOR_JAR || builtJar();
if (jar && !fs.existsSync(jar)) {
  throw new Error(`PDF_METADATA_EDITOR_JAR does not exist: ${jar}`);
}
const java = process.env.PDF_METADATA_EDITOR_JAVA || 'java';
const skip =
  !jar ? 'application JAR is not built' :
  !javaAvailable(java) ? 'Java is not available' :
  !fs.existsSync(FIXTURE) ? 'sample PDF fixture is missing' :
  false;

test('serves JSON-RPC over stdio using the system Java fallback', { skip, timeout: 30000 }, async () => {
  const root = fs.mkdtempSync(path.join(os.tmpdir(), 'pmedit-mcp-root-'));
  fs.copyFileSync(FIXTURE, path.join(root, 'sample.pdf'));

  const environment = {
    ...process.env,
    PDF_METADATA_EDITOR_BUNDLE_DIR: '',
    PDF_METADATA_EDITOR_JAR: jar,
    PDF_METADATA_EDITOR_JAVA: java
  };

  const messages = [
    {
      jsonrpc: '2.0',
      id: 1,
      method: 'initialize',
      params: {
        protocolVersion: '2025-11-25',
        capabilities: {},
        clientInfo: { name: 'npm-test', version: '1' }
      }
    },
    { jsonrpc: '2.0', method: 'notifications/initialized' },
    { jsonrpc: '2.0', id: 2, method: 'tools/list' },
    {
      jsonrpc: '2.0',
      id: 3,
      method: 'tools/call',
      params: {
        name: 'pdf_read_metadata',
        arguments: { path: 'sample.pdf' }
      }
    }
  ];

  const result = await runWrapper(
    ['--mcp', '--mcp-root', root],
    environment,
    `${messages.map((message) => JSON.stringify(message)).join('\n')}\n`
  );

  assert.equal(result.code, 0, result.stderr);
  const lines = result.stdout.split('\n').filter((line) => line.length > 0);
  assert.equal(lines.length, 3, result.stdout);

  const responses = lines.map((line) => {
    const response = JSON.parse(line);
    assert.equal(response.jsonrpc, '2.0');
    return response;
  });

  assert.equal(responses[0].id, 1);
  assert.equal(responses[0].result.protocolVersion, '2025-11-25');
  assert.equal(responses[1].id, 2);
  assert.ok(Array.isArray(responses[1].result.tools));
  assert.equal(responses[2].id, 3);
  assert.equal(responses[2].result.isError, false);
  assert.equal(responses[2].result.structuredContent.path, 'sample.pdf');
  assert.equal(typeof responses[2].result.structuredContent.fields, 'object');

  fs.rmSync(root, { recursive: true, force: true });
});

test('doctor output is edition-neutral', { skip, timeout: 15000 }, () => {
  const result = spawnSync(process.execPath, [WRAPPER, '--doctor'], {
    encoding: 'utf8',
    env: {
      ...process.env,
      PDF_METADATA_EDITOR_BUNDLE_DIR: '',
      PDF_METADATA_EDITOR_JAR: jar,
      PDF_METADATA_EDITOR_JAVA: java
    }
  });

  assert.equal(result.status, 0, result.stderr);
  assert.match(result.stdout, /package: 4\.5\.0-rc\.1/);
  assert.match(result.stdout, /mode: system Java/);
  assert.doesNotMatch(result.stdout, /\b(pro|oss|professional|basic)\b/i);
});
