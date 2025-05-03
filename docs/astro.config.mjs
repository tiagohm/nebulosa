import { defineConfig } from 'astro/config'
import starlight from '@astrojs/starlight'

// https://astro.build/config
export default defineConfig({
    site: 'https://tiagohm.github.io',
    base: '/nebulosa',
    integrations: [
        starlight({
            title: 'Nebulosa',
            favicon: 'public/favicon.png',
            social: [
                { icon: 'github', label: 'GitHub', href: 'https://github.com/tiagohm/nebulosa' },
            ],
            sidebar: [
                {
                    label: 'Guides',
                    items: [
                        // Each item here is one entry in the navigation menu.
                        { label: 'Example Guide', slug: 'guides/example' },
                    ],
                },
            ],
        }),
    ],
});
