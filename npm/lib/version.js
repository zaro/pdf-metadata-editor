'use strict';

/**
 * Convert the Maven project version to a valid npm semver version.
 *
 * Examples:
 *   4.5.0rc1 -> 4.5.0-rc.1
 *   4.5.0    -> 4.5.0
 */
function toNpmVersion(mavenVersion) {
  const value = String(mavenVersion == null ? '' : mavenVersion).trim();
  const match = /^(\d+\.\d+\.\d+)[-.]?(rc|beta|alpha)[-.]?(\d+)$/i.exec(value);
  if (match) {
    return `${match[1]}-${match[2].toLowerCase()}.${match[3]}`;
  }
  return value;
}

/**
 * Parse the major version from `java -version` output.
 */
function parseJavaVersion(output) {
  const text = String(output == null ? '' : output);
  const match = /version "(\d+)(?:\.(\d+))?/.exec(text);
  if (!match) {
    return null;
  }

  let major = Number(match[1]);
  if (major === 1 && match[2] != null) {
    major = Number(match[2]);
  }
  return major;
}

function platformKey(platform, arch) {
  return `${platform}-${arch}`;
}

function bundlePackageName(platform, arch) {
  return `@pdf-metadata-editor/mcp-bundle-${platformKey(platform, arch)}`;
}

module.exports = {
  toNpmVersion,
  parseJavaVersion,
  platformKey,
  bundlePackageName
};
