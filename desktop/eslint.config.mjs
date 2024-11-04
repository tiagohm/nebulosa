import eslint from '@eslint/js'
import tseslint from 'typescript-eslint'

export default tseslint.config(
	{
		ignores: ['**/*.mjs', '**/*.js', '**/.angular', '**/node_modules'],
	},
	{
		files: ['**/*.ts'],
		...eslint.configs.recommended,
	},
	...tseslint.configs.strictTypeChecked.map((config) => {
		return {
			files: ['**/*.ts'],
			...config,
		}
	}),
	...tseslint.configs.stylisticTypeCheckedOnly.map((config) => {
		return {
			files: ['**/*.ts'],
			...config,
		}
	}),
	{
		languageOptions: {
			parserOptions: {
				ecmaVersion: 2022,
				parser: '@typescript-eslint/parser',
				project: './tsconfig.json',
				tsconfigRootDir: import.meta.dirname,
			},
		},
	},
	{
		files: ['**/*.ts'],
		rules: {
			'no-unused-vars': 'off',
			'no-loss-of-precision': 'off',
			'no-extra-semi': 'warn',
			'@typescript-eslint/no-unused-vars': 'warn',
			'@typescript-eslint/no-loss-of-precision': 'off',
			'@typescript-eslint/restrict-template-expressions': 'off',
			'@typescript-eslint/no-unsafe-argument': 'off',
			'@typescript-eslint/no-misused-promises': 'off',
			'@typescript-eslint/no-unsafe-return': 'off',
			'@typescript-eslint/no-extraneous-class': 'off',
			'@typescript-eslint/no-non-null-assertion': 'off',
			'@typescript-eslint/consistent-type-imports': 'error',
			'@typescript-eslint/no-empty-interface': 'error',
			'@typescript-eslint/consistent-return': 'error',
			'@typescript-eslint/consistent-indexed-object-style': 'error',
			'@typescript-eslint/prefer-readonly': 'error',
			'@typescript-eslint/consistent-type-assertions': 'error',
			'@typescript-eslint/consistent-type-definitions': 'error',
			'@typescript-eslint/prefer-nullish-coalescing': [
				'error',
				{
					ignorePrimitives: true,
				},
			],
			'@typescript-eslint/no-unused-expressions': [
				'error',
				{
					allowShortCircuit: true,
					allowTernary: true,
				},
			],
		},
	},
)
