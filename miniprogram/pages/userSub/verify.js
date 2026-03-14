const app = getApp();
const { request } = require('../../utils/request');

Page({
  data: {
    idCardFront: '',
    idCardBack: '',
    isSubmitting: false,
    isVerified: false
  },

  onLoad() {
    this.checkVerifyStatus();
  },

  // 检查认证状态
  async checkVerifyStatus() {
    try {
      const res = await request({
        url: '/api/users/profile',
        method: 'GET'
      });

      if (res.user && res.user.isVerified) {
        this.setData({ isVerified: true });
      }
    } catch (err) {
      console.error('检查认证状态失败:', err);
    }
  },

  // 选择身份证正面
  chooseIdCardFront() {
    this.chooseImage('idCardFront');
  },

  // 选择身份证反面
  chooseIdCardBack() {
    this.chooseImage('idCardBack');
  },

  // 选择图片
  chooseImage(type) {
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const tempFilePath = res.tempFiles[0].tempFilePath;
        this.uploadImage(tempFilePath, type);
      }
    });
  },

  // 上传图片
  async uploadImage(filePath, type) {
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
        this.setData({ [type]: url });
        wx.hideLoading();
        wx.showToast({ title: '上传成功', icon: 'success' });
      } else {
        wx.hideLoading();
      }
    } catch (err) {
      wx.hideLoading();
      console.error('上传失败:', err);
      wx.showToast({ title: '上传失败', icon: 'none' });
    }
  },

  // 提交认证
  async submitVerify() {
    const { idCardFront, idCardBack } = this.data;

    if (!idCardFront) {
      wx.showToast({ title: '请上传身份证正面', icon: 'none' });
      return;
    }

    if (!idCardBack) {
      wx.showToast({ title: '请上传身份证反面', icon: 'none' });
      return;
    }

    this.setData({ isSubmitting: true });

    try {
      await request({
        url: '/api/users/verify',
        method: 'POST',
        data: {
          idCardFront,
          idCardBack
        }
      });

      wx.showToast({ title: '提交成功', icon: 'success' });
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
    } catch (err) {
      console.error('提交失败:', err);
      wx.showToast({ title: '提交失败', icon: 'none' });
    } finally {
      this.setData({ isSubmitting: false });
    }
  }
});
