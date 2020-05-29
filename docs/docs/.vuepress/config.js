module.exports = {
    title: 'Quick-DAO',
    description: '对Spring JDBC, MyBatis, sql2o等orm框架进行封装和抽象，用于快速实现增删改查功能',
    base: '/quick-dao/',
    themeConfig: {
        nav: [
            { text: '首页', link: '/' },
            { text: '快速开始', link: '/start/index.html' },
            { text: 'Github', link: 'https://github.com/yangziwen/quick-dao', target: '_blank' }
        ],
        sidebar: [{
            title: '快速开始',
            path: '/start/'
        }],
        sidebarDepth: 2
    }
}