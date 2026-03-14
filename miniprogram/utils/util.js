// 工具函数

// 格式化时间
const formatTime = date => {
  const year = date.getFullYear();
  const month = date.getMonth() + 1;
  const day = date.getDate();
  const hour = date.getHours();
  const minute = date.getMinutes();
  const second = date.getSeconds();

  return `${[year, month, day].map(formatNumber).join('-')} ${[hour, minute, second].map(formatNumber).join(':')}`;
};

const formatNumber = n => {
  n = n.toString();
  return n[1] ? n : `0${n}`;
};

// 格式化日期（只显示日期，支持Date对象和字符串）
const formatDate = date => {
  if (!date) return '';
  if (typeof date === 'string') {
    date = new Date(date);
  }
  const year = date.getFullYear();
  const month = date.getMonth() + 1;
  const day = date.getDate();
  return `${year}-${formatNumber(month)}-${formatNumber(day)}`;
};

// 计算距离（米转换为合适的单位）
const formatDistance = distance => {
  if (distance < 1000) {
    return `${Math.round(distance)}m`;
  } else {
    return `${(distance / 1000).toFixed(1)}km`;
  }
};

// 格式化金额
const formatMoney = money => {
  return `¥${parseFloat(money).toFixed(2)}`;
};

// 计算相对时间
const formatRelativeTime = dateStr => {
  const date = new Date(dateStr);
  const now = new Date();
  const diff = now - date;
  
  const minute = 60 * 1000;
  const hour = 60 * minute;
  const day = 24 * hour;
  
  if (diff < minute) {
    return '刚刚';
  } else if (diff < hour) {
    return `${Math.floor(diff / minute)}分钟前`;
  } else if (diff < day) {
    return `${Math.floor(diff / hour)}小时前`;
  } else if (diff < 7 * day) {
    return `${Math.floor(diff / day)}天前`;
  } else {
    return formatDate(date);
  }
};

// 防抖函数
const debounce = (fn, delay = 500) => {
  let timer = null;
  return function(...args) {
    if (timer) clearTimeout(timer);
    timer = setTimeout(() => {
      fn.apply(this, args);
    }, delay);
  };
};

// 节流函数
const throttle = (fn, delay = 500) => {
  let lastTime = 0;
  return function(...args) {
    const now = Date.now();
    if (now - lastTime >= delay) {
      fn.apply(this, args);
      lastTime = now;
    }
  };
};

// 验证手机号
const validatePhone = phone => {
  return /^1[3-9]\d{9}$/.test(phone);
};

// 处理图片URL，将相对路径转为完整URL
const processImageUrl = (url) => {
  if (!url) return '';
  // 已是完整URL，直接返回
  if (url.startsWith('http://') || url.startsWith('https://')) return url;
  // 相对路径，拼接baseUrl
  const app = getApp();
  return (app && app.globalData && app.globalData.baseUrl || '') + url;
};

// 批量处理工具图片数组
const processToolImages = (images) => {
  if (!images || !Array.isArray(images)) return [];
  return images.map(processImageUrl);
};

module.exports = {
  formatTime,
  formatDate,
  formatDistance,
  formatMoney,
  formatRelativeTime,
  debounce,
  throttle,
  validatePhone,
  processImageUrl,
  processToolImages
};
