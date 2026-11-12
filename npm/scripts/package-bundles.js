#!/usr/bin/env node
'use strict';

const crypto = require('node:crypto');
const fs = require('node:fs');
const path = require('node:path');
const { execFileSync } = require('node:child_process');

const { toNpmVersion } = require('../lib/version.js');

const NPM_ROOT = path.resolve(__dirname, '..');

const PLATFORMS = [
  {
    key: 'linux-x64',
    os: 'linux',
    cpu: 'x64',
    archive: (version) => `pdf-metadata-editor-${version}-amd64-linux-portable.tar.gz`
  },
  {
    key: 'linux-arm64',
    os: 'linux',
    cpu: 'arm64',
    archive: (version) => `pdf-metadata-editor-${version}-arm64-linux-portable.tar.gz`
  },
  {
    key: 'darwin-arm64',
    os: 'darwin',
    cpu: 'arm64',
    archive: (version) => `pdf-metadata-editor-${version}-arm64-macos-portable.zip`
  },
  {
    key: 'win32-x64',
    os: 'win32',
    cpu: 'x64',
    archive: (version) => `pdf-metadata-editor-${version}-amd64-windows-portable.zip`
  },
  {
    key: 'win32-arm64',
    os: 'win32',
    cpu: 'arm64',
    archive: (version) => `pdf-metadata-editor-${version}-arm64-windows-portable.zip`
  }
];

function parseArgs(argv) {
  const result = { platform: [] };

  for (let i = 0; i < argv.length; i += 1) {
    const arg = argv[i];
    if (arg === '--no-pack') {
      result.noPack = true;
      continue;
    }
    if (!arg.startsWith('--')) {
      throw new Error(`Unexpected argument: ${arg}`);
    }

    const equals = arg.indexOf('=');
    let key;
    let value;
    if (equals >= 0) {
      key = arg.substring(2, equals);
      value = arg.substring(equals + 1);
    } else {
      key = arg.substring(2);
      i += 1;
      if (i >= argv.length) {
        throw new Error(`Missing value for --${key}`);
      }
      value = argv[i];
    }

    if (key === 'platform') {
      result.platform.push(value);
    } else {
      result[key] = value;
    }
  }

  return result;
}

function readMavenVersion() {
  const pom = fs.readFileSync(path.join(NPM_ROOT, '..', 'pom.xml'), 'utf8');
  const match = /<version>([^<]+)<\/version>/.exec(pom);
  if (!match) {
    throw new Error('Unable to read the project version from pom.xml');
  }
  return match[1].trim();
}

function walkFiles(directory, result = []) {
  for (const entry of fs.readdirSync(directory, { withFileTypes: true })) {
    const fullPath = path.join(directory, entry.name);
    if (entry.isDirectory()) {
      walkFiles(fullPath, result);
    } else {
      result.push(fullPath);
    }
  }
  return result;
}

function findArtifact(artifactsDir, fileName) {
  const match = walkFiles(artifactsDir).find((file) => path.basename(file) === fileName);
  if (!match) {
    throw new Error(`Missing release artifact: ${fileName} (looked in ${artifactsDir})`);
  }
  return match;
}

function extractArchive(archive, destination) {
  fs.mkdirSync(destination, { recursive: true });
  if (archive.endsWith('.tar.gz')) {
    execFileSync('tar', ['-xzf', archive, '-C', destination], { stdio: 'inherit' });
  } else if (archive.endsWith('.zip')) {
    execFileSync('unzip', ['-q', archive, '-d', destination], { stdio: 'inherit' });
  } else {
    throw new Error(`Unsupported archive format: ${archive}`);
  }
}

function findLauncher(appDir) {
  const launchers = walkFiles(appDir).filter((file) => {
    const name = path.basename(file);
    return name === 'pmedit-cli' || name === 'pmedit-cli.exe';
  });

  if (launchers.length === 0) {
    throw new Error(`No pmedit-cli launcher found under ${appDir}`);
  }

  const requiredSegment = `${path.sep}bin${path.sep}`;
  const macSegment = `${path.sep}Contents${path.sep}MacOS${path.sep}`;
  const selected =
    launchers.find((file) => file.includes(requiredSegment)) ||
    launchers.find((file) => file.includes(macSegment)) ||
    launchers[0];

  const real = fs.realpathSync(selected);
  const realAppDir = fs.realpathSync(appDir);
  if (!real.startsWith(realAppDir)) {
    throw new Error(`Launcher resolves outside the bundle: ${real}`);
  }

  if (process.platform !== 'win32') {
    fs.chmodSync(real, 0o755);
  }

  return real;
}

function sha256(file) {
  return crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex');
}

function writeJson(file, value) {
  fs.writeFileSync(file, `${JSON.stringify(value, null, 2)}\n`, 'utf8');
}

function createBundle(platform, artifactsDir, outputDir, mavenVersion, npmVersion) {
  const archiveName = platform.archive(mavenVersion);
  const archive = findArtifact(artifactsDir, archiveName);
  const bundleDir = path.join(outputDir, 'bundles', platform.key);
  const appDir = path.join(bundleDir, 'app');

  fs.rmSync(bundleDir, { recursive: true, force: true });
  fs.mkdirSync(bundleDir, { recursive: true });

  extractArchive(archive, appDir);
  const launcher = findLauncher(appDir);
  const launcherRelative = path
    .relative(bundleDir, launcher)
    .split(path.sep)
    .join('/');

  writeJson(path.join(bundleDir, 'manifest.json'), {
    launcher: launcherRelative,
    sourceArtifact: archiveName,
    sha256: sha256(archive)
  });

  writeJson(path.join(bundleDir, 'package.json'), {
    name: `@pdf-metadata-editor/mcp-bundle-${platform.key}`,
    version: npmVersion,
    description: 'Platform bundle for the PDF Metadata Editor MCP server.',
    license: 'GPL-3.0',
    repository: {
      type: 'git',
      url: 'git+https://github.com/zaro/pdf-metadata-editor.git'
    },
    os: [platform.os],
    cpu: [platform.cpu],
    files: ['app/', 'manifest.json', 'LICENSE', 'NOTICE']
  });

  fs.copyFileSync(path.join(NPM_ROOT, 'LICENSE'), path.join(bundleDir, 'LICENSE'));
  fs.copyFileSync(path.join(NPM_ROOT, 'NOTICE'), path.join(bundleDir, 'NOTICE'));

  return bundleDir;
}

function packDirectory(directory, destination) {
  const npm = process.platform === 'win32' ? 'npm.cmd' : 'npm';
  execFileSync(npm, ['pack', '--pack-destination', destination], {
    cwd: directory,
    stdio: 'inherit'
  });
}

function main() {
  const args = parseArgs(process.argv.slice(2));
  const artifactsDir = path.resolve(args.artifacts || 'release-assets');
  const outputDir = path.resolve(args.out || 'dist');
  const mavenVersion = args.version || readMavenVersion();
  const npmVersion = args['npm-version'] || toNpmVersion(mavenVersion);

  const selected = args.platform.length > 0
    ? PLATFORMS.filter((platform) => args.platform.includes(platform.key))
    : PLATFORMS;

  if (selected.length === 0) {
    throw new Error(`No matching platforms. Valid values: ${PLATFORMS.map((p) => p.key).join(', ')}`);
  }

  fs.mkdirSync(outputDir, { recursive: true });

  for (const platform of selected) {
    const bundleDir = createBundle(platform, artifactsDir, outputDir, mavenVersion, npmVersion);
    process.stdout.write(`Created ${bundleDir}\n`);
    if (!args.noPack) {
      packDirectory(bundleDir, outputDir);
    }
  }
}

if (require.main === module) {
  try {
    main();
  } catch (error) {
    process.stderr.write(`package-bundles: ${error.message}\n`);
    process.exit(1);
  }
}

module.exports = {
  PLATFORMS,
  createBundle,
  extractArchive,
  findArtifact,
  findLauncher,
  parseArgs,
  readMavenVersion,
  walkFiles
};
