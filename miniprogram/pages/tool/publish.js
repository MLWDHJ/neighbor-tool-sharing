const app = getApp();
const { request } = require('../../utils/request');

Page({
  data: {
    images: [],
    name: '',
    category: '',
    categoryLabel: '',
    categoryIndex: 0,
    categoryList: [
      { value: 'electric', label: '电动工具' },
      { value: 'manual', label: '手动工具' },
      { value: 'outdoor', label: '户外装备' },
      { value: 'digital', label: '数码设备' },
      { value: 'daily', label: '生活用品' }
    ],
    price: '',
    condition: '',
    conditionList: [
      { value: 'new', label: '全新' },
      { value: 'like_new', label: '九成新' },
      { value: 'good', label: '八成新' },
      { value: 'fair', label: '七成新及以下' }
    ],
    isFree: true,
    rentFee: '',
    deposit: '',
    maxDays: '',
    maxDaysLabel: '',
    maxDaysIndex: 0,
    maxDaysList: [
      { value: 1, label: '1天内' },
      { value: 3, label: '3天内' },
      { value: 7, label: '1周内' },
      { value: 14, label: '2周内' },
      { value: 30, label: '1个月内' }
    ],
    description: ''
  },

  // 选择图片
  chooseImage() {
    const maxCount = 3 - this.data.images.length;
    if (maxCount <= 0) {
      wx.showToast({ title: '最多上传3张图片', icon: 'none' });
      return;
    }

    wx.chooseMedia({
      count: maxCount,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const tempFiles = res.tempFiles;
        tempFiles.forEach(file => {
          if (file.size > 5 * 1024 * 1024) {
            wx.showToast({ title: '图片不能超过5MB', icon: 'none' });
            return;
          }
          this.uploadImage(file.tempFilePath);
        });
      }
    });
  },

  // 上传图片
  uploadImage(filePath) {
    wx.showLoading({ title: '上传中...' });
    
    const token = wx.getStorageSync('token');
    
    wx.uploadFile({
      url: app.globalData.baseUrl + '/api/files/upload',
      filePath: filePath,
      name: 'file',
      header: {
        'Authorization': `Bearer ${token}`
      },
      success: (res) => {
        wx.hideLoading();
        const response = JSON.parse(res.data);
        if (response.success) {
          // 相对路径拼接baseUrl
          const url = response.url.startsWith('/') ? app.globalData.baseUrl + response.url : response.url;
          this.setData({
            images: [...this.data.images, url]
          });
        } else {
          wx.showToast({ 
            title: response.message || '上传失败', 
            icon: 'none' 
          });
        }
      },
      fail: (err) => {
        wx.hideLoading();
        console.error('上传图片失败', err);
        wx.showToast({ 
          title: '上传失败', 
          icon: 'none' 
        });
      }
    });
  },

  // 删除图片
  deleteImage(e) {
    const index = e.currentTarget.dataset.index;
    const images = this.data.images;
    images.splice(index, 1);
    this.setData({ images });
  },

  // 输入事件
  onNameInput(e) {
    this.setData({ name: e.detail.value });
  },

  onCategoryChange(e) {
    const index = e.detail.value;
    this.setData({ 
      category: this.data.categoryList[index].value,
      categoryLabel: this.data.categoryList[index].label,
      categoryIndex: index
    });
  },

  onPriceInput(e) {
    this.setData({ price: e.detail.value });
  },

  onConditionTap(e) {
    this.setData({ 
      condition: e.currentTarget.dataset.value 
    });
  },

  onFreeChange(e) {
    this.setData({ isFree: !e.detail.value });
  },

  onRentFeeInput(e) {
    this.setData({ rentFee: e.detail.value });
  },

  onDepositInput(e) {
    this.setData({ deposit: e.detail.value });
  },

  onMaxDaysChange(e) {
    const index = e.detail.value;
    this.setData({ 
      maxDays: this.data.maxDaysList[index].value,
      maxDaysLabel: this.data.maxDaysList[index].label,
      maxDaysIndex: index
    });
  },

  onDescriptionInput(e) {
    this.setData({ description: e.detail.value });
  },

  // 提交
  async submit() {
    const { images, name, category, price, condition, isFree, rentFee, deposit, maxDays, description } = this.data;

    // 验证
    if (images.length === 0) {
      wx.showToast({ title: '请上传至少1张图片', icon: 'none' });
      return;
    }
    if (!name || name.trim().length === 0) {
      wx.showToast({ title: '请输入工具名称', icon: 'none' });
      return;
    }
    if (!category) {
      wx.showToast({ title: '请选择工具分类', icon: 'none' });
      return;
    }
    if (!price || parseFloat(price) <= 0) {
      wx.showToast({ title: '请输入正确的购买价格', icon: 'none' });
      return;
    }
    if (!condition) {
      wx.showToast({ title: '请选择新旧程度', icon: 'none' });
      return;
    }
    if (!isFree && (!rentFee || parseFloat(rentFee) <= 0)) {
      wx.showToast({ title: '请输入正确的租金', icon: 'none' });
      return;
    }
    if (!deposit || parseFloat(deposit) <= 0) {
      wx.showToast({ title: '请输入正确的押金', icon: 'none' });
      return;
    }
    if (!maxDays) {
      wx.showToast({ title: '请选择可借时长', icon: 'none' });
      return;
    }

    try {
      wx.showLoading({ title: '发布中...' });
      
      const response = await request({
        url: '/api/tools',
        method: 'POST',
        data: {
          images,
          name: name.trim(),
          category,
          price: parseFloat(price),
          condition,
          isFree,
          rentFee: isFree ? 0 : parseFloat(rentFee),
          deposit: parseFloat(deposit),
          maxDays: parseInt(maxDays),
          description: description.trim()
        }
      });

      wx.hideLoading();
      wx.showToast({ 
        title: '发布成功', 
        icon: 'success' 
      });
      
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
    } catch (err) {
      wx.hideLoading();
      wx.showToast({ 
        title: err.message || '发布失败', 
        icon: 'none' 
      });
      console.error('发布工具失败', err);
    }
  }
});
