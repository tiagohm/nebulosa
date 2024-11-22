.PHONY: api desktop build install all

ifeq ($(OS),Windows_NT)
api:
	gradlew.bat api:shadowJar
	gradlew.bat api:dependencyGraph -Dorg.gradle.configuration-cache=false
	gradlew.bat --stop

desktop:
	cd desktop && npm run electron:build
else
api:
	./gradlew api:shadowJar
	./gradlew api:dependencyGraph -Dorg.gradle.configuration-cache=false
	./gradlew --stop

desktop:
	cd desktop && npm run electron:build
endif

build: api desktop
