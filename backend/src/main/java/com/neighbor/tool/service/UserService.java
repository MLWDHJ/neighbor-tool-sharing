package com.neighbor.tool.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neighbor.tool.model.entity.User;
import com.neighbor.tool.model.vo.UserVO;
import com.neighbor.tool.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 用户服务
 * 负责用户信息的查询和更新
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    
    /**
     * 根据用户ID查询用户信息
     */
    public UserVO getUserById(Long userId) {
        User user = userRepository.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return convertToVO(user);
    }
    
    /**
     * 更新用户信息
     */
    @Transactional
    public UserVO updateUserProfile(Long userId, String nickname, String avatar, 
                                    String location, BigDecimal latitude, BigDecimal longitude) {
        User user = userRepository.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 更新用户信息
        if (nickname != null && !nickname.trim().isEmpty()) {
            user.setNickname(nickname);
        }
        if (avatar != null) {
            user.setAvatar(avatar);
        }
        if (location != null) {
            user.setLocation(location);
        }
        if (latitude != null && longitude != null) {
            user.setLatitude(latitude);
            user.setLongitude(longitude);
        }
        
        userRepository.updateById(user);
        log.info("用户信息更新成功: userId={}", userId);
        
        return convertToVO(user);
    }
    
    /**
     * 实名认证
     */
    @Transactional
    public void verifyIdentity(Long userId, String idCardFront, String idCardBack) {
        User user = userRepository.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        user.setIdCardFront(idCardFront);
        user.setIdCardBack(idCardBack);
        user.setIsVerified(true);
        
        userRepository.updateById(user);
        log.info("用户实名认证成功: userId={}", userId);
    }
    
    /**
     * 转换为 VO
     */
    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
}
