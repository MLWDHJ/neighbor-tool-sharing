/**
 * 本地缓存工具
 * 提供带过期时间的缓存功能
 */

const CACHE_PREFIX = 'nts_cache_';

/**
 * 设置缓存
 * @param {string} key 缓存键
 * @param {any} data 缓存数据
 * @param {number} expireMinutes 过期时间（分钟），默认30分钟
 */
const set = (key, data, expireMinutes = 30) => {
  const cacheKey = CACHE_PREFIX + key;
  const expireTime = Date.now() + expireMinutes * 60 * 1000;
  
  try {
    wx.setStorageSync(cacheKey, {
      data,
      expireTime
    });
  } catch (e) {
    console.error('缓存设置失败:', e);
  }
};

/**
 * 获取缓存
 * @param {string} key 缓存键
 * @returns {any} 缓存数据，过期或不存在返回null
 */
const get = (key) => {
  const cacheKey = CACHE_PREFIX + key;
  
  try {
    const cache = wx.getStorageSync(cacheKey);
    if (!cache) return null;
    
    // 检查是否过期
    if (Date.now() > cache.expireTime) {
      wx.removeStorageSync(cacheKey);
      return null;
    }
    
    return cache.data;
  } catch (e) {
    console.error('缓存获取失败:', e);
    return null;
  }
};

/**
 * 删除缓存
 * @param {string} key 缓存键
 */
const remove = (key) => {
  const cacheKey = CACHE_PREFIX + key;
  try {
    wx.removeStorageSync(cacheKey);
  } catch (e) {
    console.error('缓存删除失败:', e);
  }
};

/**
 * 清除所有应用缓存
 */
const clear = () => {
  try {
    const keys = wx.getStorageInfoSync().keys;
    keys.forEach(key => {
      if (key.startsWith(CACHE_PREFIX)) {
        wx.removeStorageSync(key);
      }
    });
  } catch (e) {
    console.error('缓存清除失败:', e);
  }
};

/**
 * 清除过期缓存
 */
const clearExpired = () => {
  try {
    const keys = wx.getStorageInfoSync().keys;
    const now = Date.now();
    
    keys.forEach(key => {
      if (key.startsWith(CACHE_PREFIX)) {
        const cache = wx.getStorageSync(key);
        if (cache && cache.expireTime && now > cache.expireTime) {
          wx.removeStorageSync(key);
        }
      }
    });
  } catch (e) {
    console.error('清除过期缓存失败:', e);
  }
};

// 缓存键常量
const CACHE_KEYS = {
  TOOL_LIST: 'tool_list',
  TOOL_DETAIL: 'tool_detail_',
  USER_INFO: 'user_info',
  USER_STATS: 'user_stats',
  HOT_SEARCH: 'hot_search',
  SEARCH_HISTORY: 'search_history',
  CATEGORIES: 'categories'
};

module.exports = {
  set,
  get,
  remove,
  clear,
  clearExpired,
  CACHE_KEYS
};
