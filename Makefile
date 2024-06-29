.PHONY: api desktop bootRun build install

ifeq ($(OS),Windows_NT)
api:
	gradlew.bat api:bootJar

desktop:
	cd desktop && npm run electron:build

bootRun:
	gradlew.bat api:bootRun --args='--server.port=7000'
else
api:
	./gradlew api:bootJar

desktop:
	cd desktop && npm run electron:build:deb

bootRun:
	./gradlew api:bootRun --args='--server.port=7000'

install:
	sudo dpkg -i desktop/release/nebulosa_0.1.0_amd64.deb
endif

build: api desktop
