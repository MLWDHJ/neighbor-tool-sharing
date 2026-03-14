const request = require('../../utils/request.js');
const app = getApp();

Page({
  data: {
    tools: [],
    page: 1,
    pageSize: 10,
    hasMore: true,
    loading: false,
    keyword: '',
    filters: {
      category: '',
      isFree: null,
      status: '',
      maxDistance: null
    },
    userLocation: null
  },

  onLoad() {
    this.getUserLocation();
    this.loadTools();
  },

  onShow() {
    // 设置TabBar选中状态
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 0 });
    }
    // 刷新未读消息数
    this.getUnreadCount();
  },

  // 获取用户位置
  getUserLocation() {
    const location = app.globalData.location;
    if (location.latitude && location.longitude) {
      this.setData({ userLocation: location });
    } else {
      wx.getLocation({
        type: 'gcj02',
        success: (res) => {
          const location = {
            latitude: res.latitude,
            longitude: res.longitude
          };
          app.globalData.location = location;
          this.setData({ userLocation: location });
          this.loadTools();
        }
      });
    }
  },

  // 加载工具列表
  async loadTools(refresh = false) {
    if (this.data.loading) return;
    if (!refresh && !this.data.hasMore) return;

    this.setData({ loading: true });

    try {
      const page = refresh ? 1 : this.data.page;
      const params = {
        page,
        size: this.data.pageSize
      };

      // 只添加非空参数
      if (this.data.keyword) params.keyword = this.data.keyword;
      if (this.data.filters.category) params.category = this.data.filters.category;
      if (this.data.filters.isFree !== null) params.isFree = this.data.filters.isFree;
      if (this.data.filters.status) params.status = this.data.filters.status;
      if (this.data.filters.maxDistance !== null) params.maxDistance = this.data.filters.maxDistance;

      // 添加用户位置
      if (this.data.userLocation) {
        params.latitude = this.data.userLocation.latitude;
        params.longitude = this.data.userLocation.longitude;
      }

      const res = await request.get('/api/tools', params);

      const newTools = res.tools || [];
      const tools = refresh ? newTools : [...this.data.tools, ...newTools];
      
      this.setData({
        tools,
        page: page + 1,
        hasMore: newTools.length === this.data.pageSize,
        loading: false
      });
    } catch (err) {
      this.setData({ loading: false });
      console.error('加载工具列表失败', err);
    }
  },

  // 下拉刷新
  onPullDownRefresh() {
    this.loadTools(true).then(() => {
      wx.stopPullDownRefresh();
    });
  },

  // 上拉加载更多
  onReachBottom() {
    this.loadTools();
  },

  // 搜索
  onSearch(e) {
    this.setData({
      keyword: e.detail.value,
      page: 1
    });
    this.loadTools(true);
  },

  // 跳转到搜索页
  goToSearch() {
    wx.navigateTo({
      url: '/pages/home/search'
    });
  },

  // 跳转到筛选页
  goToFilter() {
    wx.showToast({
      title: '筛选功能开发中',
      icon: 'none'
    });
  },

  // 跳转到地图视图
  goToMap() {
    wx.navigateTo({
      url: '/pages/home/map'
    });
  },

  // 跳转到发布页
  goToPublish() {
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
      url: '/pages/tool/publish'
    });
  },

  // 获取未读消息数
  async getUnreadCount() {
    if (!app.isLoggedIn()) return;

    try {
      const res = await request.get('/api/messages/unread-count');
      const count = res.count || 0;
      if (count > 0) {
        wx.setTabBarBadge({
          index: 1,
          text: count > 99 ? '99+' : String(count)
        });
      } else {
        wx.removeTabBarBadge({ index: 1 });
      }
    } catch (err) {
      console.error('获取未读消息数失败', err);
    }
  }
});
