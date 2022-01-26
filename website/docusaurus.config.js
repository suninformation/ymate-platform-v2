const lightCodeTheme = require('prism-react-renderer/themes/github');
const darkCodeTheme = require('prism-react-renderer/themes/dracula');

const versions = require('./versions.json');
const VersionsArchived = require('./versionsArchived.json');
const ArchivedVersionsDropdownItems = Object.entries(VersionsArchived).splice(
    0,
    5,
);

// With JSDoc @type annotations, IDEs can provide config autocompletion
/** @type {import('@docusaurus/types').DocusaurusConfig} */
(module.exports = {
    title: 'YMP - 轻量级、组件化、简单、高效的 Java 应用开发框架',
    tagline: '轻量级、组件化、简单、高效的 Java 应用开发框架',
    url: 'https://ymate.net',
    baseUrl: '/',
    onBrokenLinks: 'warn',
    onBrokenMarkdownLinks: 'warn',
    favicon: 'img/favicon.ico',
    organizationName: '',
    projectName: '',
    customFields: {
        logoBigUrl: 'img/logo_big.png',
    },
    scripts: [
        {src: 'https://hm.baidu.com/hm.js?d732d09a2ccea77b26ad0581cd9bd91c', async: true},
        {src: 'https://s4.cnzz.com/z_stat.php?id=1254908110&web_id=1254908110', async: true},
    ],
    i18n: {
        defaultLocale: 'zh-CN',
        locales: ['en', 'zh-CN'],
        localeConfigs: {
            'en': {
                label: "English",
            },
            'zh-CN': {
                label: "简体中文",
            },
        },
    },

    presets: [
        [
            '@docusaurus/preset-classic',
            /** @type {import('@docusaurus/preset-classic').Options} */
            ({
                docs: {
                    path: 'guide',
                    routeBasePath: 'guide',
                    sidebarPath: require.resolve('./sidebars.js'),
                    editUrl: 'https://github.com/suninformation/ymatenet-platform-v2/website/edit/',
                    editLocalizedFiles: true,
                    disableVersioning: false,
                    lastVersion: "current",
                    versions: {
                        current: {
                            label: "2.1.x"
                        }
                    },
                    includeCurrentVersion: true,
                    onlyIncludeVersions: (() => ['current', ...versions.slice(0, 2)])(),
                },
                blog: {
                    blogSidebarCount: 'ALL',
                    postsPerPage: 10,
                    showReadingTime: true,
                    editUrl: 'https://github.com/suninformation/ymatenet-platform-v2/website/edit/',
                    editLocalizedFiles: true,
                },
                theme: {
                    customCss: require.resolve('./src/css/custom.css'),
                },
            }),
        ],
    ],

    plugins: [
        [
            '@docusaurus/plugin-client-redirects',
            {
                // Redirect for: /api.html => /api
                fromExtensions: ['html'],
            },
        ],
        // [
        //     '@docusaurus/plugin-content-docs',
        //     {
        //         id: 'modules',
        //         path: 'modules',
        //         routeBasePath: 'modules',
        //         sidebarPath: require.resolve('./sidebars.js'),
        //         editUrl: 'https://github.com/suninformation/ymatenet-platform-v2/website/edit/',
        //         editLocalizedFiles: true,
        //     },
        // ],
        [
            require.resolve('@easyops-cn/docusaurus-search-local'),
            {
                hashed: true,
                language: ['en', 'zh'],
                docsRouteBasePath: ['/guide', '/modules'],
                blogRouteBasePath: ['/blog'],
                docsDir: ['guide', 'modules'],
                blogDir: ['blog'],
                translations: {
                    'search_placeholder': '搜索',
                    'see_all_results': '查看全部结果',
                    'no_results': '无结果',
                    'search_results_for': '搜索结果： \'{{ keyword }}\'',
                    'search_the_documentation': '搜索此文档',
                    'count_documents_found': '找到 {{ count }} 个相关内容',
                    'count_documents_found_plural': '找到 {{ count }} 个相文内容',
                    'no_documents_were_found': '未找到相关内容'
                }
            },
        ],
    ],

    themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
        ({
            image: 'img/logo_big.png',
            metadata: [
                {name: 'author', content: 'suninformation'}
            ],
            hideableSidebar: true,
            navbar: {
                title: 'YMP',
                logo: {
                    alt: 'YMP Logo',
                    src: 'img/logo.png',
                },
                items: [
                    {
                        to: '/quickstart',
                        label: '快速上手',
                        position: 'right'
                    },
                    {
                        type: 'doc',
                        docId: 'intro',
                        label: '开发指南',
                        position: 'right',
                    },
                    {
                        to: '/modules',
                        label: '模块',
                        position: 'right'
                    },
                    // {
                    //     type: 'doc',
                    //     docId: 'intro',
                    //     docsPluginId: 'modules',
                    //     label: '模块',
                    //     position: 'right',
                    // },
                    {to: '/blog', label: '博客', position: 'right'},
                    {
                        to: '/support',
                        label: '支持 & 捐赠',
                        position: 'right'
                    },
                    {
                        type: 'dropdown',
                        label: '下载源码',
                        position: 'right',
                        items: [
                            {
                                href: 'https://github.com/suninformation/ymate-platform-v2',
                                label: 'GitHub',
                            },
                            {
                                href: 'https://gitee.com/suninformation/ymate-platform-v2',
                                label: 'Gitee',
                            },
                        ],
                    },
                    {
                        type: "docsVersionDropdown",
                        position: "right",
                        dropdownActiveClassDisabled: true,
                        dropdownItemsAfter: [
                            ...ArchivedVersionsDropdownItems.map(
                                ([versionName, versionUrl]) => ({
                                    label: versionName,
                                    href: versionUrl,
                                }),
                            ),
                            {
                                href: 'https://gitee.com/suninformation/ymate-platform-v2/blob/dev_2.0.x/README.md',
                                label: '2.0.x',
                            }
                        ],
                    },
                    {
                        type: "localeDropdown",
                        position: "right",
                    },
                ],
            },
            footer: {
                style: 'dark',
                links: [
                    {
                        title: '文档',
                        items: [
                            {
                                label: '快速上手',
                                to: '/quickstart',
                            },
                            {
                                label: '开发指南',
                                to: '/guide',
                            },
                            {
                                label: '模块',
                                to: '/modules',
                            },
                        ],
                    },
                    {
                        title: '社群',
                        items: [
                            {
                                label: 'Github Issues',
                                href: 'https://github.com/suninformation/ymate-platform-v2/issues',
                            },
                            {
                                label: 'Gitee Issues',
                                href: 'https://gitee.com/suninformation/ymate-platform-v2/issues',
                            },
                            {
                                label: 'QQ群：480374360',
                                href: 'https://qm.qq.com/cgi-bin/qm/qr?k=3KSXbRoridGeFxTVA8HZzyhwU_btZQJ2',
                            },
                            // {
                            //     html: `<a href="https://ymate.net" target="_blank" rel="" aria-label="yMateNet"><img src="https://ymate.net/img/logo.png" alt="yMateNet" /></a>`,
                            // },
                        ],
                    },
                    {
                        title: '更多',
                        items: [
                            {
                                label: '博客',
                                to: '/blog',
                            },
                            {
                                label: '支持 & 捐赠',
                                to: '/support',
                            },
                            {
                                label: 'GitHub',
                                href: 'https://github.com/suninformation/ymate-platform-v2',
                            },
                            {
                                label: 'Gitee',
                                href: 'https://gitee.com/suninformation/ymate-platform-v2',
                            },
                        ],
                    },
                ],
                copyright: `Copyright © 2015-${new Date().getFullYear()} yMate.Net. All Rights Reserved. Built with Docusaurus.<br/>Apache License Version 2.0 | <a href="https://beian.miit.gov.cn/" target="_blank">辽ICP备14001643号</a>`,
            },
            prism: {
                theme: lightCodeTheme,
                darkTheme: darkCodeTheme,
                additionalLanguages: ['java', 'properties'],
            },
        }),
});
