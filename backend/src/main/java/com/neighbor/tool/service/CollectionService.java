package com.neighbor.tool.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neighbor.tool.model.entity.Collection;
import com.neighbor.tool.model.entity.Tool;
import com.neighbor.tool.model.vo.ToolVO;
import com.neighbor.tool.repository.CollectionRepository;
import com.neighbor.tool.repository.ToolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollectionService {
    
    private final CollectionRepository collectionRepository;
    private final ToolRepository toolRepository;
    
    @Transactional
    public void toggleCollection(Long userId, Long toolId) {
        LambdaQueryWrapper<Collection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Collection::getUserId, userId).eq(Collection::getToolId, toolId);
        Collection existing = collectionRepository.selectOne(wrapper);
        
        if (existing != null) {
            collectionRepository.deleteById(existing.getId());
            log.info("取消收藏: userId={}, toolId={}", userId, toolId);
        } else {
            Collection collection = new Collection();
            collection.setUserId(userId);
            collection.setToolId(toolId);
            collectionRepository.insert(collection);
            log.info("添加收藏: userId={}, toolId={}", userId, toolId);
        }
    }
    
    /**
     * 获取用户收藏列表
     */
    public List<ToolVO> getCollections(Long userId) {
        LambdaQueryWrapper<Collection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Collection::getUserId, userId)
               .orderByDesc(Collection::getCreatedAt);
        
        List<Collection> collections = collectionRepository.selectList(wrapper);
        List<ToolVO> tools = new ArrayList<>();
        
        for (Collection collection : collections) {
            Tool tool = toolRepository.selectById(collection.getToolId());
            if (tool != null) {
                ToolVO vo = new ToolVO();
                vo.setId(tool.getId());
                vo.setName(tool.getName());
                vo.setCategory(tool.getCategory());
                vo.setImages(parseImagesJson(tool.getImages()));
                vo.setPrice(tool.getPrice());
                vo.setCondition(tool.getCondition());
                vo.setIsFree(tool.getIsFree());
                vo.setRentFee(tool.getRentFee());
                vo.setDeposit(tool.getDeposit());
                vo.setMaxDays(tool.getMaxDays());
                vo.setDescription(tool.getDescription());
                vo.setStatus(tool.getStatus());
                vo.setUserId(tool.getUserId());
                vo.setCreatedAt(tool.getCreatedAt());
                tools.add(vo);
            }
        }
        
        return tools;
    }
    
    /**
     * 解析images JSON字符串为List
     */
    private List<String> parseImagesJson(String imagesJson) {
        if (imagesJson == null || imagesJson.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return new ObjectMapper().readValue(imagesJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.error("解析images JSON失败: {}", imagesJson, e);
            return new ArrayList<>();
        }
    }
}
