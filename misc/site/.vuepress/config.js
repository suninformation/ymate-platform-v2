module.exports = {
    title: 'YMP',
    lang: 'zh-CN',
    description: '一个轻量级、模块化、简单而强大的Java应用程序开发框架。',
    head: [
        ['link', {rel: 'icon', href: '/logo.png'}],
        ['meta', {name: 'theme-color', content: '#3eaf7c'}]
    ],
    themeConfig: {
        title: null,
        logo: '/logo.png',
        repoLabel: '查看源码',
        smoothScroll: true,
        editLinks: false,
        nav: require('./nav'),
        sidebar: {
            '/guide/': buildGuideSidebar('指南')
        }
    },
    plugins: [
        ['@vuepress/back-to-top', true],
        ['@vuepress/medium-zoom', true],
        require('./plugin-stat/stat')
    ]
};

function buildGuideSidebar(title) {
    return [
        {
            title: title,
            collapsable: false,
            children: [
                '',
                'core',
                'configuration',
                'log',
                'persistence/',
                'persistence/jdbc',
                'persistence/mongodb',
                'persistence/redis',
                'plugin',
                'serv',
                'validation',
                'cache',
                'webmvc'
            ]
        }
    ]
}