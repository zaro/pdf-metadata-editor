#!/usr/bin/env node
'use strict';

const { toNpmVersion } = require('../lib/version.js');

const value = process.argv[2];
if (!value) {
  process.stderr.write('Usage: node scripts/convert-version.js <maven-version>\n');
  process.exit(2);
}

process.stdout.write(toNpmVersion(value));
