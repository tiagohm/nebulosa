/// <reference types="vitest" />

import analog from '@analogjs/platform'
import { replaceFiles } from '@nx/vite/plugins/rollup-replace-files.plugin'
import { defineConfig } from 'vite'
import { nodePolyfills } from 'vite-plugin-node-polyfills'
import viteTsConfigPaths from 'vite-tsconfig-paths'

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
	return {
		root: __dirname,
		cacheDir: './node_modules/.vite',
		build: {
			outDir: './dist/./client',
			reportCompressedSize: true,
			target: ['es2022'],
		},
		plugins: [
			analog({
				ssr: false,
				static: true,
				prerender: {
					routes: [],
				},
			}),
			viteTsConfigPaths(),
			nodePolyfills({
				include: ['path'],
			}),
			mode === 'production' &&
				replaceFiles([
					{
						replace: 'src/environments/environment.ts',
						with: 'src/environments/environment.prod.ts',
					},
				]),
		],
		server: {
			fs: {
				allow: ['.'],
			},
		},
		test: {
			globals: true,
			environment: 'jsdom',
			setupFiles: ['src/test-setup.ts'],
			include: ['**/*.spec.ts'],
			reporters: ['default'],
		},
	}
})
