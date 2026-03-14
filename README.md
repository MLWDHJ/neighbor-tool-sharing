# 邻里工具共享小程序

面向社区工具共享场景的小程序，支持工具发布、搜索、借用与评价等功能，促进邻里间工具资源的共享与互助。

## 项目简介

本项目是一个基于微信小程序和Spring Boot的邻里工具共享平台，旨在解决社区内工具闲置与需求不匹配的问题，通过共享经济模式提高工具利用率，降低使用成本，增进邻里关系。

## 技术栈

### 后端
- **框架**: Spring Boot 3.2.0
- **语言**: Java 17
- **数据库**: MySQL 8.0
- **缓存**: Redis
- **ORM**: MyBatis Plus 3.5.7
- **认证**: JWT (jsonwebtoken 0.12.3)
- **测试**: JUnit 5 + jqwik (属性测试)

### 前端
- **平台**: 微信小程序
- **开发工具**: 微信开发者工具
- **组件**: 自定义组件（工具卡片、骨架屏、懒加载图片等）

## 项目结构

```
neighbor-tool-sharing/
├── backend/                    # 后端服务
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/neighbor/tool/
│   │   │   │   ├── config/          # 配置类
│   │   │   │   ├── controller/      # 控制器
│   │   │   │   ├── model/
│   │   │   │   │   ├── dto/         # 数据传输对象
│   │   │   │   │   ├── entity/      # 实体类
│   │   │   │   │   └── vo/          # 视图对象
│   │   │   │   ├── repository/      # 数据访问层
│   │   │   │   ├── service/         # 业务逻辑层
│   │   │   │   ├── task/            # 定时任务
│   │   │   │   ├── util/            # 工具类
│   │   │   │   └── filter/          # 过滤器
│   │   │   └── resources/
│   │   │       ├── application.yml  # 配置文件
│   │   │       └── db/              # 数据库脚本
│   │   │           ├── schema.sql   # 表结构
│   │   │           ├── data.sql     # 初始数据
│   │   │           └── performance_indexes.sql  # 性能索引
│   │   └── test/                    # 测试代码
│   └── pom.xml                      # Maven配置
│
└── miniprogram/                 # 小程序前端
    ├── pages/                    # 页面
    │   ├── auth/                # 认证相关
    │   │   ├── login/          # 登录
    │   │   └── complete-profile/# 完善信息
    │   ├── home/                # 首页
    │   │   ├── index/           # 首页列表
    │   │   ├── search/          # 搜索
    │   │   └── map/             # 地图视图
    │   ├── tool/                # 工具相关
    │   │   ├── detail/          # 工具详情
    │   │   ├── publish/         # 发布工具
    │   │   └── edit/            # 编辑工具
    │   ├── borrow/              # 借用相关
    │   │   ├── apply/           # 借用申请
    │   │   ├── detail/          # 借用详情
    │   │   ├── list/            # 借用记录
    │   │   └── review/          # 评价
    │   ├── message/             # 消息中心
    │   ├── user/                # 用户中心
    │   ├── userSub/             # 用户子页面
    │   │   ├── profile/         # 个人资料
    │   │   ├── credit/          # 信用详情
    │   │   ├── tools/           # 我的工具
    │   │   ├── collection/      # 我的收藏
    │   │   ├── verify/          # 实名认证
    │   │   └── reviews/         # 我的评价
    │   └── help/                # 帮助中心
    ├── components/              # 自定义组件
    │   ├── custom-tab-bar/      # 自定义导航栏
    │   ├── tool-card/           # 工具卡片
    │   ├── skeleton/            # 骨架屏
    │   └── lazy-image/          # 懒加载图片
    ├── utils/                   # 工具函数
    │   ├── request.js           # 网络请求
    │   ├── cache.js             # 缓存管理
    │   └── util.js              # 通用工具
    ├── assets/                  # 静态资源
    │   ├── icons/               # 图标
    │   └── images/              # 图片
    ├── app.js                   # 小程序入口
    ├── app.json                 # 全局配置
    └── app.wxss                 # 全局样式
```

## 核心功能

### 用户模块
- 手机号注册/登录
- 短信验证码验证
- 个人信息管理
- 实名认证（身份证上传）
- 信用评分系统
- 信用记录查询

### 工具模块
- 工具发布（图片上传、信息填写）
- 工具编辑/下架
- 工具搜索（关键词、分类、距离）
- 工具详情查看
- 工具收藏
- 地图展示附近工具

### 借用模块
- 借用申请（选择时间、计算费用）
- 借用审批（出借人同意/拒绝）
- 借用归还
- 逾期自动处理
- 借用评价

### 消息模块
- 系统通知
- 借用申请通知
- 借用状态更新通知
- 消息已读/未读管理

### 统计模块
- 用户统计（借用次数、发布工具数）
- 工具统计（浏览量、借用次数）
- 平台数据统计

## 数据库设计

### 核心表结构

| 表名 | 说明 | 主要字段 |
|------|------|----------|
| users | 用户表 | id, phone, nickname, avatar, location, credit_score, is_verified |
| tools | 工具表 | id, user_id, name, category, images, price, status, latitude, longitude |
| borrows | 借用记录表 | id, tool_id, borrower_id, status, start_date, end_date, rent_fee |
| reviews | 评价表 | id, borrow_id, rating, content, created_at |
| messages | 消息表 | id, user_id, type, title, content, is_read |
| collections | 收藏表 | id, user_id, tool_id, created_at |
| credit_logs | 信用记录表 | id, user_id, score_change, reason, created_at |
| search_history | 搜索历史表 | id, user_id, keyword, created_at |
| faq | 常见问题表 | id, question, answer, sort_order |
| feedback | 反馈表 | id, user_id, content, contact_info, created_at |
| verification_codes | 验证码表 | id, phone, code, type, expires_at |

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+
- 微信开发者工具

### 后端部署

1. **克隆项目**
```bash
git clone https://github.com/MLWDHJ/neighbor-tool-sharing.git
cd neighbor-tool-sharing/backend
```

2. **配置数据库**
```bash
# 创建数据库
mysql -u root -p
CREATE DATABASE neighbor_tool_sharing CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

3. **导入数据库**
```bash
mysql -u root -p neighbor_tool_sharing < src/main/resources/db/schema.sql
mysql -u root -p neighbor_tool_sharing < src/main/resources/db/data.sql
```

4. **修改配置**
编辑 `src/main/resources/application.yml`：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/neighbor_tool_sharing
    username: your_username
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379
```

5. **启动服务**
```bash
mvn clean install
mvn spring-boot:run
```

服务将在 `http://localhost:8080` 启动

### 小程序部署

1. **安装微信开发者工具**
   下载地址：https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html

2. **导入项目**
   - 打开微信开发者工具
   - 选择"导入项目"
   - 选择 `miniprogram` 目录
   - 填写AppID（测试号可使用测试AppID）

3. **配置API地址**
   编辑 `miniprogram/utils/request.js`，修改 `baseUrl` 为你的后端地址

4. **编译运行**
   - 点击"编译"按钮
   - 在模拟器中预览

## API文档

### 认证相关
- `POST /api/auth/send-code` - 发送验证码
- `POST /api/auth/login` - 登录
- `POST /api/auth/complete-profile` - 完善信息
- `POST /api/auth/verify` - 实名认证

### 工具相关
- `GET /api/tools` - 获取工具列表
- `GET /api/tools/{id}` - 获取工具详情
- `POST /api/tools` - 发布工具
- `PUT /api/tools/{id}` - 编辑工具
- `DELETE /api/tools/{id}` - 删除工具
- `POST /api/tools/{id}/collect` - 收藏/取消收藏

### 借用相关
- `POST /api/borrows` - 申请借用
- `GET /api/borrows` - 获取借用记录
- `PUT /api/borrows/{id}/approve` - 审批借用
- `PUT /api/borrows/{id}/return` - 归还工具
- `POST /api/borrows/{id}/review` - 评价

### 用户相关
- `GET /api/users/profile` - 获取个人信息
- `PUT /api/users/profile` - 更新个人信息
- `GET /api/users/credit` - 获取信用详情
- `GET /api/users/tools` - 获取我的工具
- `GET /api/users/collections` - 获取我的收藏

### 消息相关
- `GET /api/messages` - 获取消息列表
- `PUT /api/messages/{id}/read` - 标记已读
- `PUT /api/messages/read-all` - 全部已读

### 统计相关
- `GET /api/statistics/overview` - 获取平台概览数据
- `GET /api/statistics/user/{id}` - 获取用户统计数据

## 测试

### 后端测试
```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=BorrowServiceTest

# 运行属性测试
mvn test -Dtest=*PropertyTest
```

### 小程序测试
- 使用微信开发者工具的调试功能
- 使用真机调试进行实际测试

## 部署

### Docker部署（后端）
```bash
# 构建镜像
docker build -t neighbor-tool-sharing:latest .

# 运行容器
docker run -d \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/neighbor_tool_sharing \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=password \
  --name neighbor-tool-sharing \
  neighbor-tool-sharing:latest
```

### 小程序发布
1. 在微信开发者工具中点击"上传"
2. 登录微信公众平台
3. 提交审核
4. 审核通过后发布

## 注意事项

1. **服务器域名配置**
   - 小程序需要配置合法域名白名单
   - 包括：request域名、uploadFile域名、downloadFile域名

2. **位置权限**
   - 需要在 `app.json` 中配置位置权限
   - 用户首次使用时需要授权

3. **文件上传**
   - 图片上传大小限制：单张5MB
   - 支持格式：jpg、png

4. **短信服务**
   - 需要配置短信服务商（如阿里云、腾讯云）
   - 修改 `SmsService` 实现类

5. **安全性**
   - 生产环境请修改JWT密钥
   - 配置HTTPS
   - 敏感信息加密存储

## 贡献指南

欢迎贡献代码！请遵循以下步骤：

1. Fork本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 联系方式

- 作者：MLWDHJ
- GitHub：https://github.com/MLWDHJ
- 项目地址：https://github.com/MLWDHJ/neighbor-tool-sharing

## 致谢

感谢所有为本项目做出贡献的开发者！

---

**注意**：本项目仅供学习交流使用，请勿用于商业用途。
