-- 邻里工具共享平台数据库表结构

-- 创建数据库
CREATE DATABASE IF NOT EXISTS neighbor_tool_sharing DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE neighbor_tool_sharing;

-- 1. 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    phone VARCHAR(11) UNIQUE NOT NULL COMMENT '手机号',
    nickname VARCHAR(50) NOT NULL COMMENT '昵称',
    avatar VARCHAR(255) COMMENT '头像URL',
    location VARCHAR(100) COMMENT '位置描述',
    latitude DECIMAL(10, 7) COMMENT '纬度',
    longitude DECIMAL(10, 7) COMMENT '经度',
    credit_score INT DEFAULT 80 COMMENT '信用评分',
    is_verified BOOLEAN DEFAULT FALSE COMMENT '是否实名认证',
    id_card_front VARCHAR(255) COMMENT '身份证正面',
    id_card_back VARCHAR(255) COMMENT '身份证反面',
    status ENUM('active', 'banned') DEFAULT 'active' COMMENT '账号状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_phone (phone),
    INDEX idx_location (latitude, longitude),
    INDEX idx_credit_score (credit_score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 2. 工具表
CREATE TABLE IF NOT EXISTS tools (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '工具ID',
    user_id BIGINT NOT NULL COMMENT '出借人ID',
    name VARCHAR(100) NOT NULL COMMENT '工具名称',
    category ENUM('electric', 'manual', 'outdoor', 'digital', 'daily') NOT NULL COMMENT '分类',
    images JSON NOT NULL COMMENT '图片URL数组',
    price DECIMAL(10, 2) NOT NULL COMMENT '购买价格',
    `condition` ENUM('new', 'like_new', 'good', 'fair') NOT NULL COMMENT '新旧程度',
    is_free BOOLEAN DEFAULT TRUE COMMENT '是否免费',
    rent_fee DECIMAL(10, 2) DEFAULT 0 COMMENT '租金(元/天)',
    deposit DECIMAL(10, 2) NOT NULL COMMENT '押金',
    max_days INT NOT NULL COMMENT '最长借用天数',
    description TEXT COMMENT '工具描述',
    status ENUM('available', 'borrowed', 'offline') DEFAULT 'available' COMMENT '状态',
    latitude DECIMAL(10, 7) NOT NULL COMMENT '纬度',
    longitude DECIMAL(10, 7) NOT NULL COMMENT '经度',
    borrow_count INT DEFAULT 0 COMMENT '被借用次数',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_location (latitude, longitude),
    INDEX idx_category (category),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工具表';


-- 3. 借用记录表
CREATE TABLE IF NOT EXISTS borrows (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '借用记录ID',
    tool_id BIGINT NOT NULL COMMENT '工具ID',
    borrower_id BIGINT NOT NULL COMMENT '借用人ID',
    lender_id BIGINT NOT NULL COMMENT '出借人ID',
    start_date DATE NOT NULL COMMENT '开始日期',
    end_date DATE NOT NULL COMMENT '归还日期',
    actual_return_date DATE COMMENT '实际归还日期',
    days INT NOT NULL COMMENT '借用天数',
    note TEXT COMMENT '借用说明',
    status ENUM('pending', 'approved', 'rejected', 'in_use', 'returned', 'cancelled') DEFAULT 'pending' COMMENT '状态',
    deposit_amount DECIMAL(10, 2) NOT NULL COMMENT '押金金额',
    rent_amount DECIMAL(10, 2) DEFAULT 0 COMMENT '租金金额',
    deduct_amount DECIMAL(10, 2) DEFAULT 0 COMMENT '扣除金额',
    is_damaged BOOLEAN DEFAULT FALSE COMMENT '是否损坏',
    damage_note TEXT COMMENT '损坏说明',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (tool_id) REFERENCES tools(id) ON DELETE CASCADE,
    FOREIGN KEY (borrower_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (lender_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_tool_id (tool_id),
    INDEX idx_borrower_id (borrower_id),
    INDEX idx_lender_id (lender_id),
    INDEX idx_status (status),
    INDEX idx_end_date (end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='借用记录表';

-- 4. 评价表
CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评价ID',
    borrow_id BIGINT NOT NULL COMMENT '借用记录ID',
    reviewer_id BIGINT NOT NULL COMMENT '评价人ID',
    reviewee_id BIGINT NOT NULL COMMENT '被评价人ID',
    rating INT NOT NULL COMMENT '评分(1-5)',
    tags JSON COMMENT '标签数组',
    comment TEXT COMMENT '评价内容',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (borrow_id) REFERENCES borrows(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewer_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewee_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_borrow_id (borrow_id),
    INDEX idx_reviewee_id (reviewee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价表';

-- 5. 消息表
CREATE TABLE IF NOT EXISTS messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    user_id BIGINT NOT NULL COMMENT '接收用户ID',
    type ENUM('borrow_request', 'request_approved', 'request_rejected', 'return_reminder', 'overdue_reminder', 'return_confirmed', 'review_received', 'system') NOT NULL COMMENT '消息类型',
    title VARCHAR(100) NOT NULL COMMENT '标题',
    content TEXT NOT NULL COMMENT '内容',
    related_id BIGINT COMMENT '关联ID(如借用记录ID)',
    is_read BOOLEAN DEFAULT FALSE COMMENT '是否已读',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_is_read (is_read),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';


-- 6. 信用记录表
CREATE TABLE IF NOT EXISTS credit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '信用记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    change_amount INT NOT NULL COMMENT '变化分数',
    reason VARCHAR(100) NOT NULL COMMENT '原因',
    related_id BIGINT COMMENT '关联ID',
    before_score INT NOT NULL COMMENT '变化前分数',
    after_score INT NOT NULL COMMENT '变化后分数',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='信用记录表';

-- 7. 收藏表
CREATE TABLE IF NOT EXISTS collections (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '收藏ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    tool_id BIGINT NOT NULL COMMENT '工具ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (tool_id) REFERENCES tools(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_tool (user_id, tool_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';

-- 8. 验证码表
CREATE TABLE IF NOT EXISTS verification_codes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '验证码ID',
    phone VARCHAR(11) NOT NULL COMMENT '手机号',
    code VARCHAR(6) NOT NULL COMMENT '验证码',
    expires_at TIMESTAMP NOT NULL COMMENT '过期时间',
    is_used BOOLEAN DEFAULT FALSE COMMENT '是否已使用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_phone (phone),
    INDEX idx_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='验证码表';

-- 9. 搜索历史表
CREATE TABLE IF NOT EXISTS search_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '搜索历史ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    keyword VARCHAR(100) NOT NULL COMMENT '搜索关键词',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='搜索历史表';

-- 10. FAQ表
CREATE TABLE IF NOT EXISTS faqs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'FAQ ID',
    category VARCHAR(50) NOT NULL COMMENT '分类',
    question VARCHAR(200) NOT NULL COMMENT '问题',
    answer TEXT NOT NULL COMMENT '答案',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_category (category),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='FAQ表';

-- 11. 反馈表
CREATE TABLE IF NOT EXISTS feedbacks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '反馈ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    type VARCHAR(50) NOT NULL COMMENT '反馈类型',
    content TEXT NOT NULL COMMENT '反馈内容',
    images VARCHAR(500) COMMENT '截图URL',
    contact VARCHAR(100) COMMENT '联系方式',
    status ENUM('pending', 'processing', 'resolved', 'closed') DEFAULT 'pending' COMMENT '处理状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='反馈表';
