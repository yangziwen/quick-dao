module.exports = {
    title: 'QuickDAO 项目文档',
    description: '对Spring JDBC, MyBatis, sql2o等orm框架进行封装和抽象，用于快速实现增删改查功能',
    base: '/quick-dao/',
    themeConfig: {
        nav: [
            { text: '首页', link: '/' },
            { text: '快速开始', link: '/start/index.html' },
            { text: '使用手册', link: '/manual/index.html' },
            { text: 'GitHub', link: 'https://github.com/yangziwen/quick-dao', target: '_blank' }
        ],
        displayAllHeaders: true,
        sidebar: [{
            title: '快速开始',
            path: '/start/',
            collapsable: false
        }, {
            title: '使用手册',
            path: '/manual/',
            collapsable: false
        }],
        sidebarDepth: 2,
        smoothScroll: true
    }
}
