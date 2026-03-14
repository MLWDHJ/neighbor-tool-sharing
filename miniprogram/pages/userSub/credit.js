const app = getApp();
const { request } = require('../../utils/request');

Page({
  data: {
    creditScore: 80,
    creditLevel: '良好',
    creditHistory: [],
    loading: true
  },

  onLoad() {
    this.loadCreditInfo();
    this.loadCreditHistory();
  },

  // 加载信用信息
  async loadCreditInfo() {
    try {
      const res = await request({
        url: '/api/users/profile',
        method: 'GET'
      });

      if (res.user) {
        const score = res.user.creditScore || 80;
        this.setData({
          creditScore: score,
          creditLevel: this.getCreditLevel(score)
        });
      }
    } catch (err) {
      console.error('加载信用信息失败:', err);
    }
  },

  // 加载信用历史
  async loadCreditHistory() {
    try {
      const res = await request({
        url: '/api/users/credit-history',
        method: 'GET'
      });

      this.setData({
        creditHistory: res.history || [],
        loading: false
      });
    } catch (err) {
      console.error('加载信用历史失败:', err);
      this.setData({ loading: false });
    }
  },

  // 获取信用等级
  getCreditLevel(score) {
    if (score >= 90) return '优秀';
    if (score >= 80) return '良好';
    if (score >= 60) return '一般';
    return '较差';
  },

  // 获取信用等级颜色
  getCreditColor(score) {
    if (score >= 90) return '#07c160';
    if (score >= 80) return '#FF6B35';
    if (score >= 60) return '#ffc107';
    return '#ff4444';
  },

  // 格式化时间
  formatTime(timeStr) {
    if (!timeStr) return '';
    const date = new Date(timeStr);
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
  }
});
