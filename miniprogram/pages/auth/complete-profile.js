const app = getApp();
const { request } = require('../../utils/request');

Page({
  data: {
    avatar: '',
    nickname: '',
    location: '',
    latitude: null,
    longitude: null
  },

  onLoad() {
    // 获取用户信息
    const userInfo = wx.getStorageSync('userInfo');
    if (userInfo) {
      this.setData({
        avatar: userInfo.avatar || '',
        nickname: userInfo.nickname || '',
        location: userInfo.location || '',
        latitude: userInfo.latitude,
        longitude: userInfo.longitude
      });
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
  uploadAvatar(filePath) {
    wx.showLoading({ title: '上传中...' });
    
    wx.uploadFile({
      url: app.globalData.baseUrl + '/api/files/upload',
      filePath: filePath,
      name: 'file',
      header: {
        'Authorization': 'Bearer ' + wx.getStorageSync('token')
      },
      success: (res) => {
        wx.hideLoading();
        const data = JSON.parse(res.data);
        if (data.success) {
          const url = data.url.startsWith('/') ? app.globalData.baseUrl + data.url : data.url;
          this.setData({ avatar: url });
        } else {
          wx.showToast({ title: data.message || '上传失败', icon: 'none' });
        }
      },
      fail: () => {
        wx.hideLoading();
        wx.showToast({ title: '上传失败', icon: 'none' });
      }
    });
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
          location: res.address,
          latitude: res.latitude,
          longitude: res.longitude
        });
      },
      fail: (err) => {
        if (err.errMsg.includes('auth deny')) {
          wx.showModal({
            title: '需要位置权限',
            content: '请在设置中开启位置权限',
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

  // 提交
  async submit() {
    const { avatar, nickname, location, latitude, longitude } = this.data;

    // 验证
    if (!avatar) {
      wx.showToast({ title: '请上传头像', icon: 'none' });
      return;
    }
    if (!nickname || nickname.trim().length === 0) {
      wx.showToast({ title: '请输入昵称', icon: 'none' });
      return;
    }
    if (!location) {
      wx.showToast({ title: '请选择位置', icon: 'none' });
      return;
    }

    wx.showLoading({ title: '提交中...' });

    try {
      const res = await request({
        url: '/api/users/profile',
        method: 'PUT',
        data: {
          avatar,
          nickname: nickname.trim(),
          location,
          latitude,
          longitude
        }
      });

      // 更新本地用户信息
      const userInfo = wx.getStorageSync('userInfo') || {};
      Object.assign(userInfo, { avatar, nickname, location, latitude, longitude });
      wx.setStorageSync('userInfo', userInfo);

      wx.showToast({ title: '完善成功', icon: 'success' });
      
      setTimeout(() => {
        wx.switchTab({ url: '/pages/home/index' });
      }, 1500);
    } catch (err) {
      wx.showToast({ title: err.message || '提交失败', icon: 'none' });
    } finally {
      wx.hideLoading();
    }
  }
});
