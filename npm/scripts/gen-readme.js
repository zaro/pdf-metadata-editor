#!/usr/bin/env node
'use strict';

const fs = require('node:fs');
const path = require('node:path');

const NPM_ROOT = path.resolve(__dirname, '..');
const REPO_ROOT = path.resolve(NPM_ROOT, '..');

const SOURCE = path.join(REPO_ROOT, 'docs', 'mcp-clients.md');
const RENDERED = path.join(REPO_ROOT, 'docs', 'mcp-clients.npx.md');
const README = path.join(NPM_ROOT, 'README.md');

const BEGIN = '<!-- BEGIN MCP CLIENTS -->';
const END = '<!-- END MCP CLIENTS -->';

const NPX = ['npx', '-y', 'pdf-metadata-editor-mcp@next'];

function jsonArray(values) {
  return `[${values.map((value) => JSON.stringify(value)).join(', ')}]`;
}

const VALUES = {
  '{{COMMAND}}': JSON.stringify(NPX[0]),
  '{{ARGS}}': jsonArray(NPX.slice(1)),
  '{{COMMAND_AND_ARGS}}': jsonArray(NPX),
  '{{COMMAND_LINE}}': NPX.join(' ')
};

function relative(file) {
  return path.relative(REPO_ROOT, file);
}

function readNormalized(file) {
  return fs.readFileSync(file, 'utf8').replace(/\r\n/g, '\n');
}

function renderClients() {
  let text = readNormalized(SOURCE);
  for (const [token, value] of Object.entries(VALUES)) {
    text = text.split(token).join(value);
  }
  if (text.includes('{{')) {
    throw new Error(`${relative(SOURCE)}: unresolved placeholder after rendering`);
  }
  if (!text.endsWith('\n')) {
    text += '\n';
  }
  return text;
}

function splice(readme, block) {
  const start = readme.indexOf(BEGIN);
  const end = readme.indexOf(END);

  if (start === -1 || end === -1) {
    throw new Error(`${relative(README)}: missing ${start === -1 ? BEGIN : END} marker`);
  }
  if (readme.indexOf(BEGIN, start + BEGIN.length) !== -1) {
    throw new Error(`${relative(README)}: duplicate ${BEGIN} marker`);
  }
  if (readme.indexOf(END, end + END.length) !== -1) {
    throw new Error(`${relative(README)}: duplicate ${END} marker`);
  }
  if (end < start) {
    throw new Error(`${relative(README)}: ${END} appears before ${BEGIN}`);
  }

  const before = readme.slice(0, start);
  const after = readme.slice(end + END.length).replace(/^\n+/, '');
  const tail = after === '' ? '' : `\n\n${after}`;
  return `${before}${BEGIN}\n${block}${END}${tail}`;
}

function staleFiles(expectedReadme, expectedClients) {
  const stale = [];

  let clients = null;
  try {
    clients = readNormalized(RENDERED);
  } catch (error) {
    if (error.code !== 'ENOENT') {
      throw error;
    }
  }
  if (clients !== expectedClients) {
    stale.push(relative(RENDERED));
  }
  if (readNormalized(README) !== expectedReadme) {
    stale.push(relative(README));
  }
  return stale;
}

function main() {
  const check = process.argv.includes('--check');

  const clients = renderClients();
  const readme = splice(readNormalized(README), clients);

  if (!check) {
    fs.writeFileSync(README, readme);
    fs.writeFileSync(RENDERED, clients);
    console.log(`Updated ${relative(README)} and ${relative(RENDERED)}.`);
    return;
  }

  const stale = staleFiles(readme, clients);
  if (stale.length > 0) {
    console.error(`Out of date: ${stale.join(', ')}`);
    console.error('Run npm run docs:render and commit the result.');
    process.exit(1);
  }
  console.log('MCP client docs are up to date.');
}

main();
