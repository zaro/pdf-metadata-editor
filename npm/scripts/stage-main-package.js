#!/usr/bin/env node
'use strict';

const fs = require('node:fs');
const path = require('node:path');
const { execFileSync } = require('node:child_process');

const { toNpmVersion } = require('../lib/version.js');

const NPM_ROOT = path.resolve(__dirname, '..');

function parseArgs(argv) {
  const result = {};
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
    result[key] = value;
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

function findJar(artifactsDir) {
  const candidates = fs.readdirSync(artifactsDir, { withFileTypes: true })
    .filter((entry) => entry.isFile())
    .map((entry) => path.join(artifactsDir, entry.name))
    .filter((file) => {
      const name = path.basename(file);
      return name.startsWith('pdf-metadata-editor-') && name.endsWith('.jar') && !name.startsWith('original-');
    });

  if (candidates.length === 0) {
    throw new Error(`No application JAR found in ${artifactsDir}`);
  }
  return candidates[0];
}

function readBundleDependencies(bundlesDir) {
  const dependencies = {};
  if (!fs.existsSync(bundlesDir)) {
    return dependencies;
  }

  for (const entry of fs.readdirSync(bundlesDir, { withFileTypes: true })) {
    if (!entry.isDirectory()) {
      continue;
    }
    const packageFile = path.join(bundlesDir, entry.name, 'package.json');
    if (!fs.existsSync(packageFile)) {
      continue;
    }
    const pkg = JSON.parse(fs.readFileSync(packageFile, 'utf8'));
    if (typeof pkg.name === 'string' && pkg.name.startsWith('@pdf-metadata-editor/mcp-bundle-')) {
      dependencies[pkg.name] = pkg.version;
    }
  }

  return dependencies;
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
  const bundlesDir = path.resolve(args.bundles || path.join(outputDir, 'bundles'));
  const mavenVersion = args.version || readMavenVersion();
  const npmVersion = args['npm-version'] || toNpmVersion(mavenVersion);
  const mainDir = path.join(outputDir, 'main');

  fs.rmSync(mainDir, { recursive: true, force: true });
  fs.mkdirSync(path.join(mainDir, 'lib'), { recursive: true });

  fs.cpSync(path.join(NPM_ROOT, 'bin'), path.join(mainDir, 'bin'), { recursive: true });
  fs.copyFileSync(path.join(NPM_ROOT, 'lib', 'version.js'), path.join(mainDir, 'lib', 'version.js'));
  fs.copyFileSync(findJar(artifactsDir), path.join(mainDir, 'lib', 'pdf-metadata-editor.jar'));
  fs.copyFileSync(path.join(NPM_ROOT, 'README.md'), path.join(mainDir, 'README.md'));
  fs.copyFileSync(path.join(NPM_ROOT, 'LICENSE'), path.join(mainDir, 'LICENSE'));
  fs.copyFileSync(path.join(NPM_ROOT, 'NOTICE'), path.join(mainDir, 'NOTICE'));

  const serverJson = JSON.parse(fs.readFileSync(path.join(NPM_ROOT, 'server.json'), 'utf8'));
  serverJson.version = npmVersion;
  if (Array.isArray(serverJson.packages) && serverJson.packages.length > 0) {
    serverJson.packages[0].version = npmVersion;
  }
  fs.writeFileSync(
    path.join(mainDir, 'server.json'),
    `${JSON.stringify(serverJson, null, 2)}\n`,
    'utf8'
  );

  if (process.platform !== 'win32') {
    fs.chmodSync(path.join(mainDir, 'bin', 'pdf-metadata-editor-mcp.js'), 0o755);
  }

  const pkg = JSON.parse(fs.readFileSync(path.join(NPM_ROOT, 'package.json'), 'utf8'));
  pkg.version = npmVersion;
  pkg.optionalDependencies = readBundleDependencies(bundlesDir);

  fs.writeFileSync(
    path.join(mainDir, 'package.json'),
    `${JSON.stringify(pkg, null, 2)}\n`,
    'utf8'
  );

  process.stdout.write(`Created ${mainDir}\n`);
  if (!args.noPack) {
    packDirectory(mainDir, outputDir);
  }
}

if (require.main === module) {
  try {
    main();
  } catch (error) {
    process.stderr.write(`stage-main-package: ${error.message}\n`);
    process.exit(1);
  }
}

module.exports = {
  findJar,
  packDirectory,
  parseArgs,
  readBundleDependencies,
  readMavenVersion
};
