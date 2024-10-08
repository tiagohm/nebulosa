.PHONY: api desktop build install

ifeq ($(OS),Windows_NT)
api:
	gradlew.bat api:shadowJar

desktop:
	cd desktop && npm run electron:build
else
api:
	./gradlew api:shadowJar

desktop:
	cd desktop && npm run electron:build:deb

install:
	sudo dpkg -i desktop/release/nebulosa_0.1.0_amd64.deb
endif

build: api desktop
