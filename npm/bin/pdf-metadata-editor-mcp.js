#!/usr/bin/env node
'use strict';

const { spawn, spawnSync } = require('node:child_process');
const fs = require('node:fs');
const path = require('node:path');

const {
  bundlePackageName,
  parseJavaVersion,
  platformKey
} = require('../lib/version.js');

const PACKAGE_ROOT = path.resolve(__dirname, '..');
const DEFAULT_JAR = path.join(PACKAGE_ROOT, 'lib', 'pdf-metadata-editor.jar');

const JVM_OPTIONS = [
  '-DnoGui',
  '-Djava.awt.headless=true',
  '--add-opens=java.base/java.nio=ALL-UNNAMED',
  '--add-opens=java.base/jdk.internal.ref=ALL-UNNAMED',
  '--enable-native-access=ALL-UNNAMED'
];

function readPackageVersion() {
  try {
    return require('../package.json').version;
  } catch (e) {
    return '0.0.0';
  }
}

function isWindows() {
  return process.platform === 'win32';
}

function normalizeArgs(args) {
  return args.filter((arg) => arg !== '--mcp');
}

function buildFallbackArgs(jarPath, forwardedArgs) {
  return [...JVM_OPTIONS, '-jar', jarPath, '--mcp', ...normalizeArgs(forwardedArgs)];
}

function rootFromArgs(args) {
  for (let i = 0; i < args.length; i += 1) {
    const arg = args[i];
    if (arg === '--mcp-root' && i + 1 < args.length) {
      return path.resolve(args[i + 1]);
    }
    if (arg.startsWith('--mcp-root=')) {
      return path.resolve(arg.substring('--mcp-root='.length));
    }
  }
  return process.cwd();
}

function resolveBundleDir() {
  const override = process.env.PDF_METADATA_EDITOR_BUNDLE_DIR;
  if (override) {
    return path.resolve(override);
  }

  const packageName = bundlePackageName(process.platform, process.arch);
  try {
    const manifestPath = require.resolve(`${packageName}/manifest.json`, {
      paths: [PACKAGE_ROOT]
    });
    return path.dirname(manifestPath);
  } catch (e) {
    return null;
  }
}

function readManifest(bundleDir) {
  const manifestPath = path.join(bundleDir, 'manifest.json');
  const manifest = JSON.parse(fs.readFileSync(manifestPath, 'utf8'));

  if (!manifest || typeof manifest.launcher !== 'string' || manifest.launcher.length === 0) {
    throw new Error(`Invalid bundle manifest: ${manifestPath}`);
  }

  const launcher = path.resolve(bundleDir, manifest.launcher);
  if (!fs.existsSync(launcher)) {
    throw new Error(`Bundle launcher not found: ${launcher}`);
  }

  return { launcher, manifest };
}

function ensureExecutable(file) {
  if (isWindows()) {
    return;
  }
  try {
    fs.accessSync(file, fs.constants.X_OK);
  } catch (e) {
    fs.chmodSync(file, 0o755);
  }
}

function resolveBundled() {
  const bundleDir = resolveBundleDir();
  if (!bundleDir) {
    return null;
  }

  try {
    const { launcher, manifest } = readManifest(bundleDir);
    ensureExecutable(launcher);
    return { bundleDir, launcher, manifest };
  } catch (e) {
    process.stderr.write(`pdf-metadata-editor-mcp: ${e.message}\n`);
    return null;
  }
}

function findJava() {
  const configured = process.env.PDF_METADATA_EDITOR_JAVA;
  if (configured) {
    return configured;
  }

  if (process.env.JAVA_HOME) {
    const candidate = path.join(process.env.JAVA_HOME, 'bin', isWindows() ? 'java.exe' : 'java');
    if (fs.existsSync(candidate)) {
      return candidate;
    }
  }

  return isWindows() ? 'java.exe' : 'java';
}

function findJar() {
  const configured = process.env.PDF_METADATA_EDITOR_JAR;
  if (configured) {
    return path.resolve(configured);
  }
  if (fs.existsSync(DEFAULT_JAR)) {
    return DEFAULT_JAR;
  }
  return null;
}

function javaVersionOf(java) {
  const result = spawnSync(java, ['-version'], { encoding: 'utf8' });
  if (result.error || result.status !== 0) {
    return null;
  }
  return parseJavaVersion(`${result.stdout || ''}\n${result.stderr || ''}`);
}

function helpText() {
  return [
    'Usage:',
    '  pdf-metadata-editor-mcp [--mcp-root DIR] [--mcp-read-only]',
    '  pdf-metadata-editor-mcp --doctor',
    '  pdf-metadata-editor-mcp --version',
    '  pdf-metadata-editor-mcp --help',
    '',
    'Starts the PDF Metadata Editor MCP server on stdin/stdout.',
    '',
    'Options:',
    '  --mcp-root DIR     Restrict file access to DIR (default: current directory)',
    '  --mcp-read-only    Disable mutating tools',
    '  --doctor           Show runtime and configuration details',
    '  --version          Print the package version',
    '  --help             Show this help',
    '',
    'Some write operations may require a license.',
    ''
  ].join('\n');
}

function doctorText(args) {
  const bundled = resolveBundled();
  const jar = findJar();
  const lines = [
    `package: ${readPackageVersion()}`,
    `platform: ${platformKey(process.platform, process.arch)}`,
    `mode: ${bundled ? 'bundled' : 'system Java'}`,
    `root: ${rootFromArgs(args)}`,
    `write mode: ${args.includes('--mcp-read-only') ? 'read-only' : 'read-write'}`
  ];

  if (bundled) {
    lines.push(`launcher: ${bundled.launcher}`);
  } else {
    const java = findJava();
    const major = javaVersionOf(java);
    lines.push(`java: ${java}${major == null ? '' : ` (Java ${major})`}`);
    lines.push(`jar: ${jar || 'not found'}`);
  }

  return `${lines.join('\n')}\n`;
}

function main() {
  const args = process.argv.slice(2);

  if (args.includes('--help') || args.includes('-h')) {
    process.stdout.write(helpText());
    return;
  }

  if (args.includes('--version') || args.includes('-v')) {
    process.stdout.write(`${readPackageVersion()}\n`);
    return;
  }

  if (args.includes('--doctor')) {
    process.stdout.write(doctorText(args));
    return;
  }

  const bundled = resolveBundled();
  let child;

  if (bundled) {
    child = spawn(bundled.launcher, ['--mcp', ...normalizeArgs(args)], {
      stdio: 'inherit'
    });
  } else {
    const jar = findJar();
    if (!jar) {
      process.stderr.write(
        'pdf-metadata-editor-mcp: the application JAR was not found. ' +
          'Reinstall the package with the platform bundle or set PDF_METADATA_EDITOR_JAR.\n'
      );
      process.exitCode = 1;
      return;
    }

    const java = findJava();
    const major = javaVersionOf(java);
    if (major == null || major < 17) {
      process.stderr.write(
        'pdf-metadata-editor-mcp: no bundled runtime is available for this platform, ' +
          'and Java 17 or newer was not found.\n' +
          'Install Java 17+ or set PDF_METADATA_EDITOR_JAVA to a Java executable.\n'
      );
      process.exitCode = 1;
      return;
    }

    child = spawn(java, buildFallbackArgs(jar, args), {
      stdio: 'inherit'
    });
  }

  child.on('error', (error) => {
    process.stderr.write(`pdf-metadata-editor-mcp: failed to start the server: ${error.message}\n`);
    process.exitCode = 1;
  });

  child.on('exit', (code, signal) => {
    if (typeof code === 'number') {
      process.exitCode = code;
    } else {
      process.exitCode = signal ? 1 : 0;
    }
  });

  for (const signal of ['SIGINT', 'SIGTERM', 'SIGHUP']) {
    process.on(signal, () => {
      try {
        child.kill(signal);
      } catch (e) {
        // Best effort; the child may already be gone.
      }
    });
  }
}

if (require.main === module) {
  main();
}

module.exports = {
  buildFallbackArgs,
  doctorText,
  findJava,
  findJar,
  helpText,
  normalizeArgs,
  readManifest,
  resolveBundleDir,
  rootFromArgs
};
