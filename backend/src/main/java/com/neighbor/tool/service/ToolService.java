package com.neighbor.tool.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neighbor.tool.model.entity.Tool;
import com.neighbor.tool.model.entity.User;
import com.neighbor.tool.model.entity.Borrow;
import com.neighbor.tool.model.entity.Review;
import com.neighbor.tool.model.entity.Collection;
import com.neighbor.tool.model.vo.ToolVO;
import com.neighbor.tool.model.vo.UserVO;
import com.neighbor.tool.model.vo.BorrowHistoryVO;
import com.neighbor.tool.repository.ToolRepository;
import com.neighbor.tool.repository.UserRepository;
import com.neighbor.tool.repository.BorrowRepository;
import com.neighbor.tool.repository.ReviewRepository;
import com.neighbor.tool.repository.CollectionRepository;
import com.neighbor.tool.util.LocationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 工具服务
 * 负责工具的发布、查询、编辑和删除
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ToolService {
    
    private final ToolRepository toolRepository;
    private final UserRepository userRepository;
    private final BorrowRepository borrowRepository;
    private final ReviewRepository reviewRepository;
    private final CollectionRepository collectionRepository;
    private final ObjectMapper objectMapper;
    
    /**
     * 发布工具
     */
    @Transactional
    public ToolVO publishTool(Long userId, String name, String category, List<String> images,
                             BigDecimal price, String condition, Boolean isFree, BigDecimal rentFee,
                             BigDecimal deposit, Integer maxDays, String description) {
        
        // 验证收费工具必须提供租金
        if (!isFree && (rentFee == null || rentFee.compareTo(BigDecimal.ZERO) <= 0)) {
            throw new RuntimeException("收费工具必须设置租金");
        }
        
        // 获取用户位置信息
        User user = userRepository.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 创建工具
        Tool tool = new Tool();
        tool.setUserId(userId);
        tool.setName(name);
        tool.setCategory(category);
        tool.setImages(convertImagesToJson(images));
        tool.setPrice(price);
        tool.setCondition(condition);
        tool.setIsFree(isFree);
        tool.setRentFee(rentFee != null ? rentFee : BigDecimal.ZERO);
        tool.setDeposit(deposit);
        tool.setMaxDays(maxDays);
        tool.setDescription(description);
        tool.setStatus("available"); // 初始状态为"可借用"
        tool.setLatitude(user.getLatitude());
        tool.setLongitude(user.getLongitude());
        tool.setBorrowCount(0);
        
        toolRepository.insert(tool);
        log.info("工具发布成功: toolId={}, userId={}", tool.getId(), userId);
        
        return convertToVO(tool, user, 0.0);
    }
    
    /**
     * 查询工具列表
     */
    public List<ToolVO> getToolList(BigDecimal userLat, BigDecimal userLon, 
                                    Double maxDistance, String category, 
                                    Boolean isFree, String status, String keyword,
                                    int page, int size) {
        
        // 构建查询条件
        LambdaQueryWrapper<Tool> wrapper = new LambdaQueryWrapper<>();
        
        if (category != null && !category.isEmpty()) {
            wrapper.eq(Tool::getCategory, category);
        }
        if (isFree != null) {
            wrapper.eq(Tool::getIsFree, isFree);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Tool::getStatus, status);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Tool::getName, keyword).or().like(Tool::getDescription, keyword));
        }
        
        // 分页查询
        Page<Tool> pageParam = new Page<>(page, size);
        IPage<Tool> toolPage = toolRepository.selectPage(pageParam, wrapper);
        
        List<Tool> tools = toolPage.getRecords();
        if (tools.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 批量查询出借人信息，避免N+1查询
        List<Long> userIds = tools.stream()
                .map(Tool::getUserId)
                .distinct()
                .collect(Collectors.toList());
        List<User> users = userRepository.selectBatchIds(userIds);
        java.util.Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        
        // 转换为 VO 并计算距离
        List<ToolVO> toolVOs = new ArrayList<>();
        for (Tool tool : tools) {
            User lender = userMap.get(tool.getUserId());
            double distance = 0.0;
            
            if (userLat != null && userLon != null && tool.getLatitude() != null && tool.getLongitude() != null) {
                distance = LocationUtil.calculateDistance(userLat, userLon, tool.getLatitude(), tool.getLongitude());
            }
            
            // 距离筛选
            if (maxDistance != null && distance > maxDistance) {
                continue;
            }
            
            toolVOs.add(convertToVO(tool, lender, distance));
        }
        
        // 按距离排序
        toolVOs.sort(Comparator.comparingDouble(ToolVO::getDistance));
        
        return toolVOs;
    }
    
    /**
     * 查询工具详情
     */
    public ToolVO getToolDetail(Long toolId, BigDecimal userLat, BigDecimal userLon) {
        Tool tool = toolRepository.selectById(toolId);
        if (tool == null) {
            throw new RuntimeException("工具不存在");
        }
        
        User lender = userRepository.selectById(tool.getUserId());
        double distance = 0.0;
        
        if (userLat != null && userLon != null && tool.getLatitude() != null && tool.getLongitude() != null) {
            distance = LocationUtil.calculateDistance(userLat, userLon, tool.getLatitude(), tool.getLongitude());
        }
        
        ToolVO toolVO = convertToVO(tool, lender, distance);
        
        // 查询借用历史（最近3条已完成的借用记录）
        LambdaQueryWrapper<Borrow> borrowWrapper = new LambdaQueryWrapper<>();
        borrowWrapper.eq(Borrow::getToolId, toolId);
        borrowWrapper.eq(Borrow::getStatus, "returned");
        borrowWrapper.orderByDesc(Borrow::getActualReturnDate);
        borrowWrapper.last("LIMIT 3");
        
        List<Borrow> borrows = borrowRepository.selectList(borrowWrapper);
        List<BorrowHistoryVO> borrowHistory = new ArrayList<>();
        
        for (Borrow borrow : borrows) {
            BorrowHistoryVO historyVO = new BorrowHistoryVO();
            historyVO.setId(borrow.getId());
            
            // 查询借用人信息
            User borrower = userRepository.selectById(borrow.getBorrowerId());
            if (borrower != null) {
                historyVO.setBorrowerNickname(borrower.getNickname());
                historyVO.setBorrowerAvatar(borrower.getAvatar());
            }
            
            historyVO.setStartDate(borrow.getStartDate());
            historyVO.setEndDate(borrow.getEndDate());
            
            // 查询评价信息
            LambdaQueryWrapper<Review> reviewWrapper = new LambdaQueryWrapper<>();
            reviewWrapper.eq(Review::getBorrowId, borrow.getId());
            Review review = reviewRepository.selectOne(reviewWrapper);
            
            if (review != null) {
                historyVO.setRating(review.getRating());
                historyVO.setComment(review.getComment());
            }
            
            borrowHistory.add(historyVO);
        }
        
        toolVO.setBorrowHistory(borrowHistory);
        
        // 如果工具已借出，查询预计归还日期
        if ("borrowed".equals(tool.getStatus())) {
            LambdaQueryWrapper<Borrow> currentBorrowWrapper = new LambdaQueryWrapper<>();
            currentBorrowWrapper.eq(Borrow::getToolId, toolId);
            currentBorrowWrapper.eq(Borrow::getStatus, "in_use");
            currentBorrowWrapper.orderByDesc(Borrow::getCreatedAt);
            currentBorrowWrapper.last("LIMIT 1");
            
            Borrow currentBorrow = borrowRepository.selectOne(currentBorrowWrapper);
            if (currentBorrow != null && currentBorrow.getEndDate() != null) {
                // 将LocalDate转换为LocalDateTime（设置为当天结束时间）
                toolVO.setExpectedReturnDate(currentBorrow.getEndDate().atTime(23, 59, 59));
            }
        }
        
        return toolVO;
    }
    
    /**
     * 查询我的工具列表
     */
    public List<ToolVO> getMyTools(Long userId) {
        LambdaQueryWrapper<Tool> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tool::getUserId, userId);
        wrapper.orderByDesc(Tool::getCreatedAt);
        
        List<Tool> tools = toolRepository.selectList(wrapper);
        User user = userRepository.selectById(userId);
        
        return tools.stream()
                .map(tool -> convertToVO(tool, user, 0.0))
                .collect(Collectors.toList());
    }
    
    /**
     * 编辑工具
     */
    @Transactional
    public ToolVO updateTool(Long toolId, Long userId, String name, String category,
                            java.util.List<String> images, BigDecimal price, String condition,
                            Boolean isFree, BigDecimal rentFee, BigDecimal deposit,
                            Integer maxDays, String description) {
        Tool tool = toolRepository.selectById(toolId);
        if (tool == null) {
            throw new RuntimeException("工具不存在");
        }
        
        if (!tool.getUserId().equals(userId)) {
            throw new RuntimeException("无权编辑此工具");
        }
        
        // 借用中的工具不允许编辑价格和押金
        if ("borrowed".equals(tool.getStatus())) {
            if (rentFee != null || deposit != null) {
                throw new RuntimeException("借用中的工具不允许编辑价格和押金");
            }
        }
        
        // 更新工具信息
        if (name != null && !name.trim().isEmpty()) {
            tool.setName(name);
        }
        if (category != null) {
            tool.setCategory(category);
        }
        if (images != null && !images.isEmpty()) {
            tool.setImages(convertImagesToJson(images));
        }
        if (price != null) {
            tool.setPrice(price);
        }
        if (condition != null) {
            tool.setCondition(condition);
        }
        if (isFree != null) {
            tool.setIsFree(isFree);
        }
        if (description != null) {
            tool.setDescription(description);
        }
        if (maxDays != null) {
            tool.setMaxDays(maxDays);
        }
        if (rentFee != null && !"borrowed".equals(tool.getStatus())) {
            tool.setRentFee(rentFee);
        }
        if (deposit != null && !"borrowed".equals(tool.getStatus())) {
            tool.setDeposit(deposit);
        }
        
        toolRepository.updateById(tool);
        log.info("工具编辑成功: toolId={}", toolId);
        
        User user = userRepository.selectById(userId);
        return convertToVO(tool, user, 0.0);
    }
    
    /**
     * 工具上下架
     */
    @Transactional
    public void updateToolStatus(Long toolId, Long userId, String status) {
        Tool tool = toolRepository.selectById(toolId);
        if (tool == null) {
            throw new RuntimeException("工具不存在");
        }
        
        if (!tool.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此工具");
        }
        
        if ("borrowed".equals(tool.getStatus())) {
            throw new RuntimeException("借用中的工具不能下架");
        }
        
        tool.setStatus(status);
        toolRepository.updateById(tool);
        log.info("工具状态更新成功: toolId={}, status={}", toolId, status);
    }
    
    /**
     * 删除工具
     */
    @Transactional
    public void deleteTool(Long toolId, Long userId) {
        Tool tool = toolRepository.selectById(toolId);
        if (tool == null) {
            throw new RuntimeException("工具不存在");
        }
        
        if (!tool.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此工具");
        }
        
        if ("borrowed".equals(tool.getStatus())) {
            throw new RuntimeException("借用中的工具不能删除");
        }
        
        // 级联删除收藏记录
        LambdaQueryWrapper<Collection> collectionWrapper = new LambdaQueryWrapper<>();
        collectionWrapper.eq(Collection::getToolId, toolId);
        collectionRepository.delete(collectionWrapper);
        
        toolRepository.deleteById(toolId);
        log.info("工具删除成功: toolId={}", toolId);
    }
    
    /**
     * 转换为 VO
     */
    private ToolVO convertToVO(Tool tool, User lender, double distance) {
        ToolVO vo = new ToolVO();
        BeanUtils.copyProperties(tool, vo);
        vo.setImages(convertJsonToImages(tool.getImages()));
        vo.setDistance(distance);
        
        if (lender != null) {
            UserVO lenderVO = new UserVO();
            BeanUtils.copyProperties(lender, lenderVO);
            vo.setLender(lenderVO);
        }
        
        return vo;
    }
    
    /**
     * 图片列表转 JSON
     */
    private String convertImagesToJson(List<String> images) {
        try {
            return objectMapper.writeValueAsString(images);
        } catch (JsonProcessingException e) {
            log.error("图片列表转JSON失败", e);
            return "[]";
        }
    }
    
    /**
     * JSON 转图片列表
     * 自动清理旧数据中存储的http://localhost前缀，统一返回相对路径
     */
    private List<String> convertJsonToImages(String json) {
        try {
            List<String> images = objectMapper.readValue(json, new TypeReference<List<String>>() {});
            return images.stream().map(url -> {
                // 清理旧数据中的 http://localhost:xxxx 前缀
                if (url != null && url.startsWith("http://localhost")) {
                    int idx = url.indexOf("/uploads/");
                    if (idx >= 0) {
                        return url.substring(idx);
                    }
                }
                return url;
            }).collect(Collectors.toList());
        } catch (JsonProcessingException e) {
            log.error("JSON转图片列表失败", e);
            return new ArrayList<>();
        }
    }
}
