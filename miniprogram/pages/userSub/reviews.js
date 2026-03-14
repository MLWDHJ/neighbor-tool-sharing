const app = getApp();
const { request } = require('../../utils/request');

Page({
  data: {
    reviews: [],
    loading: true,
    activeTab: 'received'
  },

  onLoad() {
    this.loadReviews();
  },

  // 切换标签
  switchTab(e) {
    const tab = e.currentTarget.dataset.tab;
    this.setData({ activeTab: tab, loading: true });
    this.loadReviews();
  },

  // 加载评价列表
  async loadReviews() {
    try {
      const userInfo = wx.getStorageSync('userInfo');
      const url = this.data.activeTab === 'received' 
        ? `/api/reviews/user/${userInfo.id}`
        : '/api/reviews/my';

      const res = await request({
        url,
        method: 'GET'
      });

      this.setData({
        reviews: res.reviews || [],
        loading: false
      });
    } catch (err) {
      console.error('加载评价失败:', err);
      this.setData({ loading: false });
    }
  },

  // 跳转到工具详情
  goToToolDetail(e) {
    const id = e.currentTarget.dataset.toolId;
    if (id) {
      wx.navigateTo({
        url: `/pages/tool/detail?id=${id}`
      });
    }
  }
});
