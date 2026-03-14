const app = getApp();
const { request } = require('../../utils/request');

Page({
  data: {
    isLoggedIn: false,
    userInfo: {},
    maskedPhone: '',
    stats: {
      lendCount: 0,
      borrowCount: 0,
      borrowingCount: 0,
      pendingCount: 0
    },
    myTools: []
  },

  onLoad() {
    this.checkLoginStatus();
  },

  onShow() {
    // 设置TabBar选中状态
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 2 });
    }
    // 每次显示页面时检查登录状态
    this.checkLoginStatus();
  },

  // 检查登录状态
  checkLoginStatus() {
    const token = wx.getStorageSync('token');
    if (token) {
      this.setData({ isLoggedIn: true });
      this.loadUserInfo();
      this.loadStats();
      this.loadMyTools();
    } else {
      this.setData({ isLoggedIn: false });
    }
  },

  // 跳转到登录页
  goToLogin() {
    wx.navigateTo({
      url: '/pages/auth/login'
    });
  },

  // 加载用户信息
  async loadUserInfo() {
    try {
      const userInfo = wx.getStorageSync('userInfo');
      if (userInfo) {
        this.setData({
          userInfo,
          maskedPhone: this.maskPhone(userInfo.phone)
        });
      }

      // 从服务器获取最新信息
      const res = await request({
        url: '/api/users/profile',
        method: 'GET'
      });

      if (res.user) {
        wx.setStorageSync('userInfo', res.user);
        this.setData({
          userInfo: res.user,
          maskedPhone: this.maskPhone(res.user.phone)
        });
      }
    } catch (err) {
      console.error('加载用户信息失败:', err);
    }
  },

  // 加载统计数据
  async loadStats() {
    try {
      const res = await request({
        url: '/api/statistics/user',
        method: 'GET'
      });

      const stats = res.statistics || res;
      this.setData({
        stats: {
          lendCount: stats.lendCount || 0,
          borrowCount: stats.borrowCount || 0,
          borrowingCount: stats.borrowingCount || 0,
          pendingCount: stats.pendingCount || 0
        }
      });
    } catch (err) {
      console.error('加载统计数据失败:', err);
    }
  },

  // 加载我的工具（最多3个）
  async loadMyTools() {
    try {
      const res = await request({
        url: '/api/tools/my',
        method: 'GET',
        data: {
          page: 1,
          size: 3
        }
      });

      this.setData({
        myTools: res.tools || []
      });
    } catch (err) {
      console.error('加载我的工具失败:', err);
    }
  },

  // 手机号脱敏
  maskPhone(phone) {
    if (!phone) return '';
    return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2');
  },

  // 跳转到个人资料页
  goToProfile() {
    wx.navigateTo({
      url: '/pages/userSub/profile'
    });
  },

  // 跳转到信用详情页
  goToCredit() {
    wx.navigateTo({
      url: '/pages/userSub/credit'
    });
  },

  // 跳转到借用记录列表
  goToBorrowList(e) {
    const tab = e.currentTarget.dataset.tab;
    wx.navigateTo({
      url: `/pages/borrow/list?tab=${tab}`
    });
  },

  // 跳转到我的收藏
  goToCollection() {
    wx.navigateTo({
      url: '/pages/userSub/collection'
    });
  },

  // 跳转到我的评价
  goToReviews() {
    wx.navigateTo({
      url: '/pages/userSub/reviews'
    });
  },

  // 跳转到实名认证
  goToVerify() {
    if (this.data.userInfo.isVerified) {
      wx.showToast({
        title: '您已完成实名认证',
        icon: 'success'
      });
      return;
    }
    wx.navigateTo({
      url: '/pages/userSub/verify'
    });
  },

  // 跳转到我的钱包
  goToWallet() {
    wx.showToast({
      title: '功能开发中',
      icon: 'none'
    });
  },

  // 邀请好友
  inviteFriends() {
    wx.showShareMenu({
      withShareTicket: true,
      menus: ['shareAppMessage', 'shareTimeline']
    });
    wx.showToast({
      title: '请点击右上角分享',
      icon: 'none'
    });
  },

  // 跳转到帮助中心
  goToHelp() {
    wx.navigateTo({
      url: '/pages/help/index'
    });
  },

  // 跳转到我的工具列表
  goToMyTools() {
    wx.navigateTo({
      url: '/pages/userSub/tools'
    });
  },

  // 跳转到工具详情
  goToToolDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/tool/detail?id=${id}`
    });
  },

  // 跳转到发布工具
  goToPublish() {
    wx.navigateTo({
      url: '/pages/tool/publish'
    });
  },

  // 跳转到账号设置
  goToSettings() {
    wx.showToast({
      title: '功能开发中',
      icon: 'none'
    });
  },

  // 跳转到隐私设置
  goToPrivacy() {
    wx.showToast({
      title: '功能开发中',
      icon: 'none'
    });
  },

  // 跳转到关于我们
  goToAbout() {
    wx.showToast({
      title: '功能开发中',
      icon: 'none'
    });
  },

  // 退出登录
  logout() {
    wx.showModal({
      title: '退出登录',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          app.logout();
        }
      }
    });
  },

  // 分享
  onShareAppMessage() {
    return {
      title: '邻里工具共享 - 让闲置工具流动起来',
      path: '/pages/home/index',
      imageUrl: '/images/share.png'
    };
  }
});
