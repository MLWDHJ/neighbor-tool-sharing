const request = require('../../utils/request.js');
const util = require('../../utils/util.js');
const app = getApp();

Page({
  data: {
    phone: '',
    code: '',
    canSendCode: false,
    canLogin: false,
    codeButtonText: '发送验证码',
    countdown: 60
  },

  onLoad() {
    // 如果已登录，跳转到首页
    if (app.isLoggedIn()) {
      wx.switchTab({
        url: '/pages/home/index'
      });
    }
  },

  // 手机号输入
  onPhoneInput(e) {
    const phone = e.detail.value;
    this.setData({
      phone,
      canSendCode: util.validatePhone(phone),
      canLogin: util.validatePhone(phone) && this.data.code.length === 6
    });
  },

  // 验证码输入
  onCodeInput(e) {
    const code = e.detail.value;
    this.setData({
      code,
      canLogin: util.validatePhone(this.data.phone) && code.length === 6
    });
  },

  // 发送验证码
  async sendCode() {
    if (!this.data.canSendCode) return;

    try {
      await request.post('/api/auth/send-code', {
        phone: this.data.phone
      }, false);

      wx.showToast({
        title: '验证码已发送',
        icon: 'success'
      });

      // 开始倒计时
      this.startCountdown();
    } catch (err) {
      console.error('发送验证码失败', err);
    }
  },

  // 倒计时
  startCountdown() {
    this.setData({
      canSendCode: false,
      countdown: 60
    });

    const timer = setInterval(() => {
      if (this.data.countdown <= 1) {
        clearInterval(timer);
        this.setData({
          canSendCode: true,
          codeButtonText: '发送验证码',
          countdown: 60
        });
      } else {
        this.setData({
          countdown: this.data.countdown - 1,
          codeButtonText: `${this.data.countdown - 1}秒后重试`
        });
      }
    }, 1000);
  },

  // 登录
  async login() {
    if (!this.data.canLogin) return;

    try {
      wx.showLoading({ title: '登录中...' });

      const res = await request.post('/api/auth/login', {
        phone: this.data.phone,
        code: this.data.code
      }, false);

      wx.hideLoading();

      // 保存token和用户信息
      app.login(res.token, res.user);

      // 检查是否需要完善信息
      if (!res.user.nickname || !res.user.avatar) {
        wx.redirectTo({
          url: '/pages/auth/complete-profile'
        });
      } else {
        wx.switchTab({
          url: '/pages/home/index'
        });
      }
    } catch (err) {
      wx.hideLoading();
      console.error('登录失败', err);
    }
  }
});
