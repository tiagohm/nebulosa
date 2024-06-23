import eslint from '@eslint/js'
import tseslint from 'typescript-eslint'

export default tseslint.config(
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
			'@typescript-eslint/no-unused-vars': 'warn',
			'@typescript-eslint/no-loss-of-precision': 'off',
			'@typescript-eslint/restrict-template-expressions': 'off',
			'@typescript-eslint/no-unsafe-argument': 'off',
			'@typescript-eslint/no-misused-promises': 'off',
			'@typescript-eslint/no-unsafe-return': 'off',
			'@typescript-eslint/no-extraneous-class': 'off',
			'@typescript-eslint/no-non-null-assertion': 'off',
		},
	},
)
