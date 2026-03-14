const app = getApp();
const { request } = require('../../utils/request');

Page({
  data: {
    tools: [],
    loading: true,
    refreshing: false
  },

  onLoad() {
    this.loadCollections();
  },

  onShow() {
    this.loadCollections();
  },

  // 下拉刷新
  onPullDownRefresh() {
    this.setData({ refreshing: true });
    this.loadCollections().then(() => {
      wx.stopPullDownRefresh();
      this.setData({ refreshing: false });
    });
  },

  // 加载收藏列表
  async loadCollections() {
    try {
      const res = await request({
        url: '/api/tools/collections',
        method: 'GET'
      });

      this.setData({
        tools: res.tools || [],
        loading: false
      });
    } catch (err) {
      console.error('加载收藏失败:', err);
      this.setData({ loading: false });
    }
  },

  // 跳转到工具详情
  goToDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/tool/detail?id=${id}`
    });
  },

  // 取消收藏
  async cancelCollection(e) {
    const id = e.currentTarget.dataset.id;

    wx.showModal({
      title: '取消收藏',
      content: '确定要取消收藏该工具吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await request({
              url: `/api/tools/${id}/collect`,
              method: 'POST'
            });

            wx.showToast({ title: '已取消收藏', icon: 'success' });
            this.loadCollections();
          } catch (err) {
            console.error('取消收藏失败:', err);
            wx.showToast({ title: '操作失败', icon: 'none' });
          }
        }
      }
    });
  },

  // 跳转到首页
  goToHome() {
    wx.switchTab({
      url: '/pages/home/index'
    });
  }
});
