.PHONY: build install

build:
	./gradlew api:bootJar
	cd desktop && npm run electron:build:deb

install:
	sudo dpkg -i desktop/release/nebulosa_0.1.0_amd64.deb
