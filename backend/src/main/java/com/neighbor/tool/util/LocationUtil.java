package com.neighbor.tool.util;

import java.math.BigDecimal;

/**
 * 位置计算工具类
 * 使用 Haversine 公式计算两点之间的距离
 */
public class LocationUtil {
    
    private static final double EARTH_RADIUS_KM = 6371.0; // 地球半径（公里）
    
    /**
     * 计算两个坐标点之间的距离（单位：米）
     * 使用 Haversine 公式
     * 
     * @param lat1 第一个点的纬度
     * @param lon1 第一个点的经度
     * @param lat2 第二个点的纬度
     * @param lon2 第二个点的经度
     * @return 距离（米）
     */
    public static double calculateDistance(BigDecimal lat1, BigDecimal lon1, 
                                          BigDecimal lat2, BigDecimal lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return 0.0;
        }
        
        double dLat = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double dLon = Math.toRadians(lon2.doubleValue() - lon1.doubleValue());
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1.doubleValue())) * 
                   Math.cos(Math.toRadians(lat2.doubleValue())) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        // 返回距离（米）
        return EARTH_RADIUS_KM * c * 1000;
    }
    
    /**
     * 计算两个坐标点之间的距离（单位：公里）
     */
    public static double calculateDistanceInKm(BigDecimal lat1, BigDecimal lon1, 
                                               BigDecimal lat2, BigDecimal lon2) {
        return calculateDistance(lat1, lon1, lat2, lon2) / 1000.0;
    }
}
