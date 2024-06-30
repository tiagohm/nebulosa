# Nebulosa

[![Active Development](https://img.shields.io/badge/Maintenance%20Level-Actively%20Developed-brightgreen.svg)](https://gist.github.com/cheerfulstoic/d107229326a01ff0f333a1d3476e068d)
[![CI](https://github.com/tiagohm/nebulosa/actions/workflows/ci.yml/badge.svg)](https://github.com/tiagohm/nebulosa/actions/workflows/ci.yml)

The complete integrated solution for all of your astronomical imaging needs.

## Building

### Pre-requisites

* Java 17
* Node 20.9.0 or newer

### Steps

1. `./gradlew api:bootJar`
2. `cd desktop`
3. `npm i`

#### On Linux

4. `npm run electron:build:deb` to build `.deb` package.
5. `npm run electron:build:app` to build `AppImage`.
6. `npm run electron:build:rpm` to build `RPM` package.

Before build a `RPM` package, run `sudo apt install rpm`.

#### On Windows

4. `npm run electron:build` to build the `.exe`.

#### On Linux ARM (Raspberry PI)

run these commands before:

* `sudo apt install ruby ruby-dev`
* `sudo gem install fpm`

4. `USE_SYSTEM_FPM=true npm run electron:build:deb` to build `.deb` package.

> Look at `release` subdirectory for the generated build.
