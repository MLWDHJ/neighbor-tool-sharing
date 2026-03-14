-- 初始化测试数据

USE neighbor_tool_sharing;

-- 插入测试用户
INSERT INTO users (phone, nickname, avatar, location, latitude, longitude, credit_score, is_verified, status) VALUES
('13800138001', '张三', 'https://api.dicebear.com/7.x/avataaars/png?seed=zhangsan', '阳光小区 1栋', 23.1291, 113.2644, 85, TRUE, 'active'),
('13800138002', '李四', 'https://api.dicebear.com/7.x/avataaars/png?seed=lisi', '阳光小区 2栋', 23.1295, 113.2648, 90, TRUE, 'active'),
('13800138003', '王五', 'https://api.dicebear.com/7.x/avataaars/png?seed=wangwu', '阳光小区 3栋', 23.1298, 113.2652, 80, FALSE, 'active');

-- 插入测试工具
INSERT INTO tools (user_id, name, category, images, price, `condition`, is_free, rent_fee, deposit, max_days, description, status, latitude, longitude) VALUES
(1, '博世电钻', 'electric', '["https://placehold.co/400x300/4CAF50/FFFFFF?text=电钻"]', 300.00, 'like_new', FALSE, 10.00, 150.00, 3, '博世电钻，功率强劲，适合家装打孔。配有多种钻头，使用方便。', 'available', 23.1291, 113.2644),
(1, '户外帐篷', 'outdoor', '["https://placehold.co/400x300/FF9800/FFFFFF?text=帐篷"]', 500.00, 'good', TRUE, 0.00, 200.00, 7, '4人户外帐篷，防水防风，适合露营使用。', 'available', 23.1291, 113.2644),
(2, '行李箱', 'daily', '["https://placehold.co/400x300/2196F3/FFFFFF?text=行李箱"]', 200.00, 'new', FALSE, 5.00, 100.00, 7, '28寸行李箱，全新未使用，适合旅行搬家。', 'available', 23.1295, 113.2648),
(2, '电动螺丝刀', 'electric', '["https://placehold.co/400x300/9C27B0/FFFFFF?text=螺丝刀"]', 150.00, 'like_new', FALSE, 5.00, 80.00, 5, '小米电动螺丝刀，轻便好用，适合日常维修。', 'available', 23.1295, 113.2648),
(3, '投影仪', 'digital', '["https://placehold.co/400x300/F44336/FFFFFF?text=投影仪"]', 800.00, 'good', FALSE, 20.00, 300.00, 3, '极米投影仪，1080P高清，适合家庭影院和会议使用。', 'available', 23.1298, 113.2652),
(3, '折叠梯', 'manual', '["https://placehold.co/400x300/607D8B/FFFFFF?text=折叠梯"]', 200.00, 'like_new', TRUE, 0.00, 100.00, 7, '铝合金折叠梯，高度2米，稳固安全，适合家用。', 'available', 23.1298, 113.2652);

-- 插入测试借用记录
INSERT INTO borrows (tool_id, borrower_id, lender_id, start_date, end_date, days, note, status, deposit_amount, rent_amount) VALUES
(1, 2, 1, '2024-03-01', '2024-03-03', 3, '周末挂画使用', 'returned', 150.00, 30.00),
(2, 3, 1, '2024-03-05', '2024-03-10', 5, '周末露营', 'returned', 200.00, 0.00),
(3, 1, 2, '2024-03-15', '2024-03-20', 5, '出差使用', 'in_use', 100.00, 25.00),
(4, 3, 2, '2024-03-18', '2024-03-22', 4, '组装家具', 'pending', 80.00, 20.00);

-- 插入测试评价
INSERT INTO reviews (borrow_id, reviewer_id, reviewee_id, rating, tags, comment) VALUES
(1, 2, 1, 5, '["工具好用", "沟通顺畅"]', '工具很好用，出借人很友好，推荐！'),
(2, 3, 1, 5, '["准时交付", "物品完好"]', '帐篷质量很好，张三人也很热情！'),
(2, 1, 3, 4, '["按时归还", "爱护物品"]', '王五很守时，帐篷归还时很干净。');

-- 插入测试消息
INSERT INTO messages (user_id, type, title, content, related_id, is_read) VALUES
(1, 'system', '欢迎使用', '欢迎使用邻里工具共享平台！', NULL, FALSE),
(1, 'borrow_request', '借用申请', '李四申请借用您的博世电钻', 1, TRUE),
(1, 'return_reminder', '归还提醒', '李四已归还博世电钻，请确认', 1, FALSE),
(2, 'system', '欢迎使用', '欢迎使用邻里工具共享平台！', NULL, TRUE),
(2, 'request_approved', '申请通过', '张三已同意您借用博世电钻', 1, TRUE),
(3, 'system', '欢迎使用', '欢迎使用邻里工具共享平台！', NULL, FALSE);

-- 插入FAQ数据
INSERT INTO faqs (category, question, answer, sort_order) VALUES
('basic', '如何发布工具？', '在首页点击"发布工具"按钮，填写工具信息、上传图片、设置借用规则即可发布。', 1),
('basic', '如何借用工具？', '在工具列表中找到需要的工具，点击进入详情页，选择借用时间后提交申请，等待出借人同意即可。', 2),
('basic', '押金如何退还？', '归还工具并经出借人确认后，押金会自动退还到您的账户。如有损坏，出借人可申请扣除部分押金。', 3),
('credit', '信用评分如何计算？', '新用户初始80分。按时归还+2分，逾期每天-5分，工具损坏-10分，收到好评+1分，收到差评-3分。', 4),
('credit', '信用评分有什么用？', '信用评分低于60分将无法借用工具。信用评分95分以上可获得高信用徽章，更容易获得出借人信任。', 5),
('credit', '如何提高信用评分？', '按时归还工具、爱护工具、积极评价他人、保持良好的借用记录都可以提高信用评分。', 6),
('borrow', '借用天数有限制吗？', '每个工具的最长借用天数由出借人设置，一般为3-30天。请在借用前查看工具详情。', 7),
('borrow', '可以提前归还吗？', '可以提前归还。租金按实际借用天数计算，多余租金会退还。', 8),
('borrow', '逾期会怎样？', '逾期会收到提醒通知，每天扣除5分信用评分。逾期超过3天会影响后续借用，超过7天可能被限制使用。', 9),
('safety', '如何保证交易安全？', '平台采用实名认证、信用评分、押金保护等多重机制保障交易安全。建议选择高信用用户进行交易。', 10),
('safety', '工具损坏怎么办？', '如工具损坏，出借人可在确认归还时标记损坏并说明情况，平台会根据情况扣除相应押金。', 11),
('safety', '遇到纠纷怎么办？', '如遇到纠纷，可通过平台反馈功能联系客服，我们会及时介入处理。', 12);
