// 封装网络请求
const app = getApp();

const request = (options) => {
  return new Promise((resolve, reject) => {
    const { url, method = 'GET', data = {}, needAuth = true } = options;
    
    const header = {
      'Content-Type': 'application/json'
    };
    
    // 添加认证token
    if (needAuth && app.globalData.token) {
      header['Authorization'] = `Bearer ${app.globalData.token}`;
    }
    
    wx.request({
      url: `${app.globalData.baseUrl}${url}`,
      method,
      data,
      header,
      success: (res) => {
        if (res.statusCode === 200) {
          resolve(res.data);
        } else if (res.statusCode === 401) {
          // 未授权，跳转到登录页
          wx.showToast({
            title: '请先登录',
            icon: 'none'
          });
          setTimeout(() => {
            wx.reLaunch({
              url: '/pages/auth/login'
            });
          }, 1500);
          reject(res);
        } else {
          wx.showToast({
            title: res.data.message || '请求失败',
            icon: 'none'
          });
          reject(res);
        }
      },
      fail: (err) => {
        wx.showToast({
          title: '网络错误',
          icon: 'none'
        });
        reject(err);
      }
    });
  });
};

module.exports = {
  request,
  get: (url, data = {}, needAuth = true) => {
    return request({ url, method: 'GET', data, needAuth });
  },
  
  post: (url, data = {}, needAuth = true) => {
    return request({ url, method: 'POST', data, needAuth });
  },
  
  put: (url, data = {}, needAuth = true) => {
    return request({ url, method: 'PUT', data, needAuth });
  },
  
  delete: (url, data = {}, needAuth = true) => {
    return request({ url, method: 'DELETE', data, needAuth });
  }
};
