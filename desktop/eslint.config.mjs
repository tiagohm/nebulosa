import eslint from '@eslint/js'
import angular from 'angular-eslint'
import tseslint from 'typescript-eslint'

export default tseslint.config(
	{
		ignores: ['**/*.mjs', '**/*.js', '**/.angular', '**/node_modules'],
	},
	{
		files: ['**/*.ts'],
		extends: [eslint.configs.recommended, ...tseslint.configs.strictTypeChecked, ...tseslint.configs.stylisticTypeCheckedOnly, ...angular.configs.tsRecommended],
		languageOptions: {
			parserOptions: {
				ecmaVersion: 2022,
				parser: '@typescript-eslint/parser',
				project: './tsconfig.json',
				tsconfigRootDir: import.meta.dirname,
			},
		},
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
			'@angular-eslint/prefer-standalone': 'off',
			'@angular-eslint/directive-selector': 'off',
			'@angular-eslint/component-selector': [
				'error',
				{
					type: 'element',
					prefix: 'neb',
					style: 'kebab-case',
				},
			],
		},
	},
	{
		files: ['**/*.html'],
		extends: [...angular.configs.templateRecommended, ...angular.configs.templateAccessibility],
		rules: {
			'@angular-eslint/template/label-has-associated-control': 'off',
			'@angular-eslint/template/click-events-have-key-events': 'off',
			'@angular-eslint/template/interactive-supports-focus': 'off',
			'@angular-eslint/template/alt-text': 'off',
			'@angular-eslint/template/elements-content': [
				'error',
				{
					allowList: ['pButton'],
				},
			],
		},
	},
)
