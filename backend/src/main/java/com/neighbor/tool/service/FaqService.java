package com.neighbor.tool.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.neighbor.tool.model.entity.Faq;
import com.neighbor.tool.repository.FaqRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * FAQ Service
 */
@Service
@RequiredArgsConstructor
public class FaqService {
    
    private final FaqRepository faqRepository;
    
    /**
     * 查询所有FAQ，按分类和排序号排序
     */
    public List<Faq> getAllFaqs() {
        return faqRepository.selectList(
            new QueryWrapper<Faq>()
                .orderByAsc("category", "sort_order")
        );
    }
    
    /**
     * 按分类查询FAQ
     */
    public List<Faq> getFaqsByCategory(String category) {
        return faqRepository.selectList(
            new QueryWrapper<Faq>()
                .eq("category", category)
                .orderByAsc("sort_order")
        );
    }
}
