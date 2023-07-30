const fs = require('fs')
const { copyFiles } = require('./package.json')

for (const file of copyFiles) {
    fs.copyFile(file.from, file.to, () => null)
}
