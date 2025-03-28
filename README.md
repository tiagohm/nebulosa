# Nebulosa

[![Not Maintained](https://img.shields.io/badge/Maintenance%20Level-Not%20Maintained-yellow.svg)](https://gist.github.com/cheerfulstoic/d107229326a01ff0f333a1d3476e068d)

The complete integrated solution for all of your astronomical imaging needs.

## Building

### Pre-requisites

* Java 17
* Node 20.9.0 or newer

### Steps

1. `./gradlew api:shadowJar`
2. `cd desktop`
3. `npm i`

#### On Linux

4. `npm run electron:build:deb` to build `.deb` package.
5. `npm run electron:build:app` to build `AppImage` package.
6. `npm run electron:build:rpm` to build `RPM` package.
7. `npm run electron:build:pacman` to build `.pacman` package.

> Before build a `RPM` package, run `sudo apt install rpm`.

> Before build a `pacman` package on Ubuntu, run `sudo apt install libarchive-tools`.

#### On Windows

4. `npm run electron:build:portable` to build the portable `.exe`.
5. `npm run electron:build:msi` to build the `.msi` Installer.

#### On Linux ARM (Raspberry PI)

run these commands before:

* `sudo apt install ruby ruby-dev`
* `sudo gem install fpm`

4. `USE_SYSTEM_FPM=true npm run electron:build:deb` to build `.deb` package.

> Look at `release` subdirectory for the generated build.

> Alternatively, you can run `npm run electron:build` for build all package formats.
