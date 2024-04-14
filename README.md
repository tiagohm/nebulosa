# Nebulosa

[![Active Development](https://img.shields.io/badge/Maintenance%20Level-Actively%20Developed-brightgreen.svg)](https://gist.github.com/cheerfulstoic/d107229326a01ff0f333a1d3476e068d)
[![CI](https://github.com/tiagohm/nebulosa/actions/workflows/ci.yml/badge.svg)](https://github.com/tiagohm/nebulosa/actions/workflows/ci.yml)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/62f7820784d142dab9feebc222cba4a8)](https://app.codacy.com/gh/tiagohm/nebulosa/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)

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
