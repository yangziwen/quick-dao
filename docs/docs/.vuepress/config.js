import { googleAnalyticsPlugin } from '@vuepress/plugin-google-analytics'
import { defaultTheme } from '@vuepress/theme-default'
import { defineUserConfig } from 'vuepress'
import { webpackBundler } from '@vuepress/bundler-webpack'

export default defineUserConfig({
    base: '/quick-dao/',
    lang: 'zh-CN',
    title: 'QuickDAO 项目文档',
    description: '对Spring JDBC, MyBatis, sql2o等orm框架进行封装和抽象，用于快速实现增删改查功能',
    bundler: webpackBundler(),
    theme: defaultTheme({
        navbar: [
            { text: '首页', link: '/' },
            { text: '快速开始', link: '/start/' },
            { text: '使用手册', link: '/manual/' },
            { text: 'GitHub', link: 'https://github.com/yangziwen/quick-dao', target: '_blank' }
        ],
        sidebar: {
            '/start/': [{
                text: '快速开始',
                link: '/start/',
                collapsible: false
            }],
            '/manual/': [{
                text: '使用手册',
                link: '/manual/',
                collapsible: false
            }]
        },
        sidebarDepth: 2
    }),
    plugins: [
        googleAnalyticsPlugin({
            id: 'UA-193733260-2'
        })
    ]
})
