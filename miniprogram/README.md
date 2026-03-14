# 邻里工具共享小程序

## 项目结构

```
miniprogram/
├── app.js                 # 小程序入口文件
├── app.json              # 小程序配置
├── app.wxss              # 全局样式
├── components/           # 组件目录
│   └── tool-card/       # 工具卡片组件
├── pages/               # 页面目录
│   ├── auth/           # 认证相关页面
│   │   ├── login/      # 登录页
│   │   └── complete-profile/  # 完善信息页
│   ├── home/           # 首页
│   ├── tool/           # 工具相关页面
│   │   ├── detail/     # 工具详情
│   │   └── publish/    # 发布工具
│   ├── borrow/         # 借用相关页面
│   │   └── apply/      # 借用申请
│   ├── message/        # 消息中心
│   └── user/           # 用户中心
├── utils/              # 工具函数
│   ├── util.js        # 通用工具函数
│   └── request.js     # 网络请求封装
└── assets/            # 静态资源
    ├── icons/         # 图标
    └── images/        # 图片
```

## 已完成功能

### 基础架构
- ✅ 小程序项目结构
- ✅ 全局配置（app.json）
- ✅ 全局样式（app.wxss）
- ✅ 底部导航栏配置

### 工具函数
- ✅ 网络请求封装（request.js）
- ✅ 通用工具函数（util.js）
  - 时间格式化
  - 距离格式化
  - 金额格式化
  - 防抖/节流
  - 手机号验证

### 组件
- ✅ tool-card 工具卡片组件
  - 工具信息展示
  - 距离计算
  - 状态显示

### 页面
- ✅ 登录页（pages/auth/login）
  - 手机号验证
  - 验证码发送
  - 登录功能
  - 倒计时功能
- ✅ 完善信息页（pages/auth/complete-profile）
  - 头像上传
  - 昵称输入
  - 位置选择
- ✅ 首页（pages/home/index）
  - 工具列表展示
  - 下拉刷新
  - 上拉加载更多
  - 搜索入口
  - 筛选入口
  - 发布按钮
- ✅ 工具详情页（pages/tool/detail）
  - 图片轮播
  - 工具信息展示
  - 出借人信息
  - 借用记录
  - 收藏功能
- ✅ 发布工具页（pages/tool/publish）
  - 图片上传（1-3张）
  - 基本信息表单
  - 借用设置
  - 工具描述
  - 完整验证
- ✅ 借用申请页（pages/borrow/apply）
  - 工具信息卡片
  - 时间选择
  - 费用计算
  - 借用说明
  - 申请提交
- ✅ 消息中心页（pages/message/index）
  - 消息列表
  - 未读标记
  - 全部已读
  - 消息删除
  - 消息跳转
- ✅ 用户中心页（pages/user/index）
  - 用户信息卡片
  - 统计数据展示
  - 功能菜单
  - 退出登录

## 待完成功能

- ⏳ 搜索页（pages/home/search）
- ⏳ 筛选页（pages/home/filter）
- ⏳ 地图视图（pages/home/map）
- ⏳ 借用记录列表（pages/borrow/list）
- ⏳ 借用详情页（pages/borrow/detail）
- ⏳ 评价页（pages/review/index）
- ⏳ 我的工具页（pages/user/tools）
- ⏳ 我的收藏页（pages/user/collection）
- ⏳ 信用详情页（pages/user/credit）
- ⏳ 编辑资料页（pages/user/edit-profile）
- ⏳ 帮助中心页（pages/help/index）
- ⏳ 意见反馈页（pages/feedback/index）

## 开发说明

### 环境要求
- 微信开发者工具
- Node.js（可选，用于npm包管理）

### 配置说明
1. 修改 `project.config.json` 中的 `appid` 为你的小程序AppID
2. 修改 `app.js` 中的 `baseUrl` 为后端API地址

### 运行步骤
1. 使用微信开发者工具打开 `miniprogram` 目录
2. 编译运行

## 注意事项
- 需要配置服务器域名白名单
- 需要申请位置权限
- 图标和图片资源需要自行准备
