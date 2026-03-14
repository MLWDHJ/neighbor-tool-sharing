const request = require('../../utils/request.js');
const { processToolImages } = require('../../utils/util.js');
const app = getApp();

Page({
  data: {
    toolId: null,
    tool: {},
    lender: {},
    borrowHistory: [],
    isCollected: false,
    categoryMap: {
      'electric': '电动工具',
      'manual': '手动工具',
      'outdoor': '户外工具',
      'digital': '数码设备',
      'daily': '日常用品'
    },
    conditionMap: {
      'new': '全新',
      'like_new': '几乎全新',
      'good': '良好',
      'fair': '一般'
    }
  },

  onLoad(options) {
    if (options.id) {
      this.setData({ toolId: options.id });
      this.loadToolDetail();
      this.checkCollection();
    }
  },

  // 加载工具详情
  async loadToolDetail() {
    try {
      wx.showLoading({ title: '加载中...' });
      
      // 获取用户位置（如果有）
      const userLocation = wx.getStorageSync('userLocation');
      const params = {};
      if (userLocation && userLocation.latitude && userLocation.longitude) {
        params.latitude = userLocation.latitude;
        params.longitude = userLocation.longitude;
      }
      
      const response = await request.get(`/api/tools/${this.data.toolId}`, params);
      const tool = response.tool;
      
      // 格式化预计归还日期
      if (tool.expectedReturnDate) {
        tool.expectedReturnDate = this.formatDate(tool.expectedReturnDate);
      }
      
      // 格式化借用历史日期
      if (tool.borrowHistory && tool.borrowHistory.length > 0) {
        tool.borrowHistory.forEach(item => {
          item.startDate = this.formatDate(item.startDate);
          item.endDate = this.formatDate(item.endDate);
        });
      }
      
      // 处理图片URL
      tool.images = processToolImages(tool.images);
      
      // 后端已经返回了出借人信息和借用历史
      this.setData({
        tool: tool,
        lender: tool.lender || {},
        borrowHistory: tool.borrowHistory || []
      });
      
      wx.hideLoading();
    } catch (err) {
      wx.hideLoading();
      wx.showToast({
        title: '加载失败',
        icon: 'none'
      });
      console.error('加载工具详情失败', err);
    }
  },

  // 格式化日期
  formatDate(dateStr) {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    const month = date.getMonth() + 1;
    const day = date.getDate();
    return `${month}月${day}日`;
  },

  // 检查是否已收藏
  async checkCollection() {
    if (!app.isLoggedIn()) return;
    
    try {
      const response = await request.get('/api/tools/collections');
      const collections = response.tools || [];
      const isCollected = collections.some(item => item.id === parseInt(this.data.toolId));
      this.setData({ isCollected });
    } catch (err) {
      console.error('检查收藏状态失败', err);
    }
  },

  // 切换收藏
  async toggleCollection() {
    if (!app.isLoggedIn()) {
      wx.showToast({
        title: '请先登录',
        icon: 'none'
      });
      setTimeout(() => {
        wx.navigateTo({
          url: '/pages/auth/login'
        });
      }, 1500);
      return;
    }

    try {
      await request.post(`/api/tools/${this.data.toolId}/collect`);
      
      this.setData({
        isCollected: !this.data.isCollected
      });
      
      wx.showToast({
        title: this.data.isCollected ? '收藏成功' : '取消收藏',
        icon: 'success'
      });
    } catch (err) {
      console.error('收藏操作失败', err);
    }
  },

  // 查看出借人资料
  viewLenderProfile() {
    wx.navigateTo({
      url: `/pages/userSub/profile?userId=${this.data.lender.id}`
    });
  },

  // 申请借用
  applyBorrow() {
    if (!app.isLoggedIn()) {
      wx.showToast({
        title: '请先登录',
        icon: 'none'
      });
      setTimeout(() => {
        wx.navigateTo({
          url: '/pages/auth/login'
        });
      }, 1500);
      return;
    }

    wx.navigateTo({
      url: `/pages/borrow/apply?toolId=${this.data.toolId}`
    });
  },

  // 分享
  onShareAppMessage() {
    const tool = this.data.tool;
    return {
      title: `${tool.name || '工具详情'} - 邻里工具共享`,
      path: `/pages/tool/detail?id=${this.data.toolId}`,
      imageUrl: tool.images && tool.images.length > 0 ? tool.images[0] : ''
    };
  },

  // 下拉刷新
  onPullDownRefresh() {
    this.loadToolDetail();
    this.checkCollection();
    wx.stopPullDownRefresh();
  }
});
