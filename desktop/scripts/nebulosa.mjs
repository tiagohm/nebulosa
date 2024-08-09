import { execSync } from 'child_process'
import fs from 'fs'
import mainPackageJson from '../app/package.json' with { type: 'json' }
import rendererPackageJson from '../package.json' with { type: 'json' }

const dependencies = []

if (rendererPackageJson.dependencies) {
	for (const [name, version] of Object.entries(rendererPackageJson.dependencies).filter((e) => !e[1].includes('#'))) {
		dependencies.push({ name, version })
	}
}

if (mainPackageJson.dependencies) {
	for (const [name, version] of Object.entries(mainPackageJson.dependencies).filter((e) => !e[1].includes('#'))) {
		dependencies.push({ name, version })
	}
}

if (rendererPackageJson.devDependencies) {
	for (const [name, version] of Object.entries(rendererPackageJson.devDependencies).filter((e) => !e[1].includes('#'))) {
		dependencies.push({ name, version })
	}
}

if (mainPackageJson.devDependencies) {
	for (const [name, version] of Object.entries(mainPackageJson.devDependencies).filter((e) => !e[1].includes('#'))) {
		dependencies.push({ name, version })
	}
}

dependencies.sort((a, b) => a.name.localeCompare(b.name))

const data = {
	name: rendererPackageJson.name,
	codename: rendererPackageJson.codename,
	version: rendererPackageJson.version,
	description: rendererPackageJson.description,
	author: rendererPackageJson.author,
	build: {
		commit: execSync('git rev-parse HEAD').toString().trim(),
		date: new Date().toISOString(),
	},
	dependencies,
}

fs.writeFileSync('src/assets/data/nebulosa.json', JSON.stringify(data))
