-- 性能优化索引脚本
-- 在已有索引基础上添加复合索引，优化常用查询

USE neighbor_tool_sharing;

-- 工具表复合索引优化
-- 优化按状态+分类+位置的查询
ALTER TABLE tools ADD INDEX idx_status_category (status, category);

-- 优化按状态+是否免费的查询
ALTER TABLE tools ADD INDEX idx_status_free (status, is_free);

-- 优化按用户+状态的查询（我的工具列表）
ALTER TABLE tools ADD INDEX idx_user_status (user_id, status);

-- 优化按创建时间排序的查询
ALTER TABLE tools ADD INDEX idx_created_at (created_at DESC);

-- 借用记录表复合索引优化
-- 优化按借用人+状态的查询（我的借用列表）
ALTER TABLE borrows ADD INDEX idx_borrower_status (borrower_id, status);

-- 优化按出借人+状态的查询（我的出借列表）
ALTER TABLE borrows ADD INDEX idx_lender_status (lender_id, status);

-- 优化按工具+状态的查询
ALTER TABLE borrows ADD INDEX idx_tool_status (tool_id, status);

-- 优化逾期查询（状态+结束日期）
ALTER TABLE borrows ADD INDEX idx_status_enddate (status, end_date);

-- 消息表复合索引优化
-- 优化按用户+已读状态+时间的查询
ALTER TABLE messages ADD INDEX idx_user_read_time (user_id, is_read, created_at DESC);

-- 评价表复合索引优化
-- 优化按被评价人+时间的查询
ALTER TABLE reviews ADD INDEX idx_reviewee_time (reviewee_id, created_at DESC);

-- 收藏表复合索引优化
-- 优化按用户+时间的查询
ALTER TABLE collections ADD INDEX idx_user_time (user_id, created_at DESC);

-- 搜索历史表复合索引优化
-- 优化按用户+时间的查询
ALTER TABLE search_history ADD INDEX idx_user_keyword_time (user_id, created_at DESC);

-- 全文索引（用于工具搜索）
ALTER TABLE tools ADD FULLTEXT INDEX ft_name_desc (name, description);
