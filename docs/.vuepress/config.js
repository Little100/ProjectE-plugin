import { viteBundler } from '@vuepress/bundler-vite'
import { defaultTheme } from '@vuepress/theme-default'
import { defineUserConfig } from 'vuepress'
import { searchPlugin } from '@vuepress/plugin-search'

export default defineUserConfig({
  bundler: viteBundler(),
  
  // 多语言配置
  locales: {
    '/': {
      lang: 'zh-CN',
      title: 'ProjectE 插件',
      description: 'Minecraft Spigot 等价交换插件文档'
    },
    '/en/': {
      lang: 'en-US',
      title: 'ProjectE Plugin',
      description: 'Minecraft Spigot Equivalent Exchange Plugin Documentation'
    }
  },
  
  base: '/ProjectE-plugin/',
  
  theme: defaultTheme({
    logo: '/images/Philosopher_Stone.png',
    
    locales: {
      '/': {
        selectLanguageName: '简体中文',
        navbar: [
          { text: '首页', link: '/' },
          { text: '指南', link: '/guide/' },
          { text: 'GitHub', link: 'https://github.com/Little100/ProjectE-plugin' }
        ],
        sidebar: {
          '/guide/': [
            {
              text: '指南',
              children: [
                '/guide/README.md',
                '/guide/installation.md',
                '/guide/commands.md',
                '/guide/permissions.md'
              ]
            }
          ]
        },
        editLinkText: '在 GitHub 上编辑此页',
        lastUpdatedText: '上次更新',
        contributorsText: '贡献者'
      },
      '/en/': {
        selectLanguageName: 'English',
        navbar: [
          { text: 'Home', link: '/en/' },
          { text: 'Guide', link: '/en/guide/' },
          { text: 'GitHub', link: 'https://github.com/Little100/ProjectE-plugin' }
        ],
        sidebar: {
          '/en/guide/': [
            {
              text: 'Guide',
              children: [
                '/en/guide/README.md',
                '/en/guide/installation.md',
                '/en/guide/commands.md',
                '/en/guide/permissions.md'
              ]
            }
          ]
        },
        editLinkText: 'Edit this page on GitHub',
        lastUpdatedText: 'Last Updated',
        contributorsText: 'Contributors'
      }
    },
    
    repo: 'Little100/ProjectE-plugin',
    docsDir: 'docs',
    editLink: true,
    lastUpdated: true,
    contributors: true
  }),
  
  plugins: [
    searchPlugin({
      locales: {
        '/': {
          placeholder: '搜索',
        },
        '/en/': {
          placeholder: 'Search',
        }
      }
    })
  ]
})