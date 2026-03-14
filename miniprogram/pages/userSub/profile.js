const app = getApp();
const { request } = require('../../utils/request');

Page({
  data: {
    userInfo: {},
    nickname: '',
    avatar: '',
    location: '',
    latitude: null,
    longitude: null,
    isSubmitting: false
  },

  onLoad() {
    this.loadUserInfo();
  },

  // 加载用户信息
  async loadUserInfo() {
    try {
      const res = await request({
        url: '/api/users/profile',
        method: 'GET'
      });

      if (res.user) {
        this.setData({
          userInfo: res.user,
          nickname: res.user.nickname || '',
          avatar: res.user.avatar || '',
          location: res.user.location || ''
        });
      }
    } catch (err) {
      console.error('加载用户信息失败:', err);
    }
  },

  // 选择头像
  chooseAvatar() {
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const tempFilePath = res.tempFiles[0].tempFilePath;
        this.uploadAvatar(tempFilePath);
      }
    });
  },

  // 上传头像
  async uploadAvatar(filePath) {
    wx.showLoading({ title: '上传中...' });
    try {
      const uploadRes = await new Promise((resolve, reject) => {
        wx.uploadFile({
          url: `${app.globalData.baseUrl}/api/files/upload`,
          filePath: filePath,
          name: 'file',
          header: {
            'Authorization': `Bearer ${app.globalData.token}`
          },
          success: (res) => {
            const data = JSON.parse(res.data);
            resolve(data);
          },
          fail: reject
        });
      });

      if (uploadRes.url) {
        const url = uploadRes.url.startsWith('/') ? app.globalData.baseUrl + uploadRes.url : uploadRes.url;
        this.setData({ avatar: url });
        wx.hideLoading();
        wx.showToast({ title: '上传成功', icon: 'success' });
      } else {
        wx.hideLoading();
      }
    } catch (err) {
      wx.hideLoading();
      console.error('上传头像失败:', err);
      wx.showToast({ title: '上传失败', icon: 'none' });
    }
  },

  // 输入昵称
  onNicknameInput(e) {
    this.setData({ nickname: e.detail.value });
  },

  // 选择位置
  chooseLocation() {
    wx.chooseLocation({
      success: (res) => {
        this.setData({
          location: res.name || res.address,
          latitude: res.latitude,
          longitude: res.longitude
        });
      },
      fail: (err) => {
        if (err.errMsg.includes('auth deny')) {
          wx.showModal({
            title: '提示',
            content: '需要授权位置权限才能选择地址',
            confirmText: '去设置',
            success: (res) => {
              if (res.confirm) {
                wx.openSetting();
              }
            }
          });
        }
      }
    });
  },

  // 保存资料
  async saveProfile() {
    const { nickname, avatar, location, latitude, longitude } = this.data;

    if (!nickname.trim()) {
      wx.showToast({ title: '请输入昵称', icon: 'none' });
      return;
    }

    this.setData({ isSubmitting: true });

    try {
      const res = await request({
        url: '/api/users/profile',
        method: 'PUT',
        data: {
          nickname: nickname.trim(),
          avatar,
          location,
          latitude,
          longitude
        }
      });

      if (res.user) {
        wx.setStorageSync('userInfo', res.user);
        wx.showToast({ title: '保存成功', icon: 'success' });
        setTimeout(() => {
          wx.navigateBack();
        }, 1500);
      }
    } catch (err) {
      console.error('保存失败:', err);
      wx.showToast({ title: '保存失败', icon: 'none' });
    } finally {
      this.setData({ isSubmitting: false });
    }
  }
});
