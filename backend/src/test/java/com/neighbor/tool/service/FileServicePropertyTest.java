package com.neighbor.tool.service;

import net.jqwik.api.*;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 文件服务属性测试
 * Feature: neighbor-tool-sharing
 */
@SpringBootTest
class FileServicePropertyTest {
    
    @Autowired
    private FileService fileService;
    
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final int MAX_IMAGE_COUNT = 3;
    
    /**
     * Property 5: 工具图片上传验证
     * Feature: neighbor-tool-sharing, Property 5: 工具图片上传验证
     * Validates: Requirements 3.1
     * 
     * 对于任何工具发布请求，如果图片数量不在1-3张之间或任何图片大小超过5MB，
     * 系统应该拒绝该请求并抛出异常
     */
    @Property(tries = 100)
    void imageUploadValidation_invalidImageCount(@ForAll("invalidImageCount") int imageCount) {
        // 生成无效数量的图片（0张或超过3张）
        List<MultipartFile> files = createMockImages(imageCount, 1024 * 1024); // 1MB each
        
        // 验证系统拒绝无效数量的图片
        assertThatThrownBy(() -> fileService.uploadImages(files))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("图片");
    }
    
    /**
     * Property 5: 工具图片上传验证 - 文件大小验证
     * Feature: neighbor-tool-sharing, Property 5: 工具图片上传验证
     * Validates: Requirements 3.1
     * 
     * 对于任何图片，如果大小超过5MB，系统应该拒绝上传
     */
    @Property(tries = 100)
    void imageUploadValidation_oversizedImage(
            @ForAll("validImageCount") int imageCount,
            @ForAll("oversizedFileSize") long fileSize) {
        // 生成包含超大图片的列表
        List<MultipartFile> files = createMockImages(imageCount, fileSize);
        
        // 验证系统拒绝超大图片
        assertThatThrownBy(() -> fileService.uploadImages(files))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("5MB");
    }
    
    /**
     * Property 5: 工具图片上传验证 - 有效图片上传成功
     * Feature: neighbor-tool-sharing, Property 5: 工具图片上传验证
     * Validates: Requirements 3.1
     * 
     * 对于任何有效的图片上传请求（1-3张，每张不超过5MB），系统应该成功上传并返回URL列表
     */
    @Property(tries = 100)
    void imageUploadValidation_validImages(
            @ForAll("validImageCount") int imageCount,
            @ForAll("validFileSize") long fileSize) {
        // 生成有效的图片列表
        List<MultipartFile> files = createMockImages(imageCount, fileSize);
        
        // 上传图片
        List<String> imageUrls = fileService.uploadImages(files);
        
        // 验证返回的URL数量正确
        assertThat(imageUrls).hasSize(imageCount);
        
        // 验证每个URL都不为空
        assertThat(imageUrls).allMatch(url -> url != null && !url.isEmpty());
    }
    
    // ========== Helper Methods ==========
    
    /**
     * 创建模拟图片文件列表
     */
    private List<MultipartFile> createMockImages(int count, long fileSize) {
        if (count <= 0) {
            return new ArrayList<>();
        }
        
        List<MultipartFile> files = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            byte[] content = new byte[(int) fileSize];
            MockMultipartFile file = new MockMultipartFile(
                    "file" + i,
                    "image" + i + ".jpg",
                    "image/jpeg",
                    content
            );
            files.add(file);
        }
        return files;
    }
    
    // ========== Arbitraries ==========
    
    @Provide
    Arbitrary<Integer> invalidImageCount() {
        // 生成无效的图片数量：0张或超过3张
        return Arbitraries.oneOf(
                Arbitraries.just(0),
                Arbitraries.integers().between(4, 10)
        );
    }
    
    @Provide
    Arbitrary<Integer> validImageCount() {
        // 生成有效的图片数量：1-3张
        return Arbitraries.integers().between(1, MAX_IMAGE_COUNT);
    }
    
    @Provide
    Arbitrary<Long> oversizedFileSize() {
        // 生成超过5MB的文件大小
        return Arbitraries.longs().between(MAX_FILE_SIZE + 1, MAX_FILE_SIZE * 2);
    }
    
    @Provide
    Arbitrary<Long> validFileSize() {
        // 生成有效的文件大小：1KB到5MB
        return Arbitraries.longs().between(1024, MAX_FILE_SIZE);
    }
}
