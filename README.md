# Nebulosa

[![Active Development](https://img.shields.io/badge/Maintenance%20Level-Actively%20Developed-brightgreen.svg)](https://gist.github.com/cheerfulstoic/d107229326a01ff0f333a1d3476e068d)
[![CI](https://github.com/tiagohm/nebulosa/actions/workflows/ci.yml/badge.svg)](https://github.com/tiagohm/nebulosa/actions/workflows/ci.yml)
[![CodeFactor](https://www.codefactor.io/repository/github/tiagohm/nebulosa/badge/main)](https://www.codefactor.io/repository/github/tiagohm/nebulosa/overview/main)

The complete integrated solution for all of your astronomical imaging needs.

## Building

### Pre-requisites

* Java 17
* Node 20.9.0 or newer

### Steps

* `./gradlew api:bootJar`
* `cd desktop`
* `npm i`
* `npm run electron:build`
