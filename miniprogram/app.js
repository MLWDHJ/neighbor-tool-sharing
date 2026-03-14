// app.js
App({
  globalData: {
    userInfo: null,
    token: null,
    baseUrl: 'http://localhost:8080',
    location: {
      latitude: null,
      longitude: null
    }
  },

  onLaunch() {
    // 检查登录状态
    this.checkLoginStatus();
    
    // 获取用户位置
    this.getUserLocation();
  },

  // 检查登录状态
  checkLoginStatus() {
    const token = wx.getStorageSync('token');
    if (token) {
      this.globalData.token = token;
      // 获取用户信息
      this.getUserInfo();
    }
  },

  // 获取用户信息
  getUserInfo() {
    wx.request({
      url: `${this.globalData.baseUrl}/api/users/profile`,
      header: {
        'Authorization': `Bearer ${this.globalData.token}`
      },
      success: (res) => {
        if (res.statusCode === 200 && res.data.user) {
          this.globalData.userInfo = res.data.user;
          wx.setStorageSync('userInfo', res.data.user);
        }
      }
    });
  },

  // 获取用户位置
  getUserLocation() {
    wx.getLocation({
      type: 'gcj02',
      success: (res) => {
        this.globalData.location = {
          latitude: res.latitude,
          longitude: res.longitude
        };
      },
      fail: () => {
        console.log('获取位置失败，使用默认位置');
        // 游客模式或拒绝授权时使用默认位置（广州）
        this.globalData.location = {
          latitude: 23.1291,
          longitude: 113.2644
        };
      }
    });
  },

  // 登录
  login(token, userInfo) {
    this.globalData.token = token;
    this.globalData.userInfo = userInfo;
    wx.setStorageSync('token', token);
    wx.setStorageSync('userInfo', userInfo);
  },

  // 登出
  logout() {
    this.globalData.token = null;
    this.globalData.userInfo = null;
    wx.removeStorageSync('token');
    wx.removeStorageSync('userInfo');
    wx.reLaunch({
      url: '/pages/auth/login'
    });
  },

  // 检查是否登录
  isLoggedIn() {
    return !!this.globalData.token;
  }
});
