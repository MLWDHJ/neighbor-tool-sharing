package com.neighbor.tool.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 文件服务
 * 负责图片上传和删除
 * TODO: 集成腾讯云 COS
 */
@Slf4j
@Service
public class FileService {
    
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final int MAX_IMAGE_COUNT = 3;
    
    /**
     * 上传图片
     */
    public List<String> uploadImages(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new RuntimeException("请选择要上传的图片");
        }
        
        if (files.size() > MAX_IMAGE_COUNT) {
            throw new RuntimeException("图片数量不能超过" + MAX_IMAGE_COUNT + "张");
        }
        
        List<String> imageUrls = new ArrayList<>();
        
        for (MultipartFile file : files) {
            // 验证文件大小
            if (file.getSize() > MAX_FILE_SIZE) {
                throw new RuntimeException("图片大小不能超过5MB");
            }
            
            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new RuntimeException("只能上传图片文件");
            }
            
            // TODO: 上传到腾讯云 COS
            // 目前返回模拟的 URL
            String fileName = UUID.randomUUID().toString() + getFileExtension(file.getOriginalFilename());
            String imageUrl = "https://example.com/images/" + fileName;
            imageUrls.add(imageUrl);
            
            log.info("图片上传成功: {}", imageUrl);
        }
        
        return imageUrls;
    }
    
    /**
     * 删除图片
     */
    public void deleteImage(String imageUrl) {
        // TODO: 从腾讯云 COS 删除
        log.info("图片删除成功: {}", imageUrl);
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
