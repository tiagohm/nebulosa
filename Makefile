.PHONY: api desktop build install all

ifeq ($(OS),Windows_NT)
api:
	gradlew.bat api:shadowJar
	gradlew.bat --stop

desktop:
	cd desktop && npm run electron:build
else
api:
	./gradlew api:shadowJar
	./gradlew --stop

desktop:
	cd desktop && npm run electron:build

install:
	sudo dpkg -i desktop/release/nebulosa_0.1.0_amd64.deb
endif

build: api desktop
