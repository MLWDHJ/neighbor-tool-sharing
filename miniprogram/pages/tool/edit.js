const app = getApp();
const { request } = require('../../utils/request');
const { processToolImages } = require('../../utils/util');

Page({
  data: {
    toolId: null,
    images: [],
    name: '',
    categoryIndex: -1,
    categories: ['电动工具', '手动工具', '户外装备', '数码设备', '生活用品'],
    categoryValues: ['electric', 'manual', 'outdoor', 'digital', 'daily'],
    price: '',
    conditionIndex: -1,
    conditions: ['全新', '九成新', '八成新', '七成新及以下'],
    conditionValues: ['new', 'like_new', 'good', 'fair'],
    isFree: true,
    rentFee: '',
    deposit: '',
    durationIndex: -1,
    durations: ['1天内', '3天内', '1周内', '2周内', '1个月内', '可协商'],
    pickupMethods: [],
    description: '',
    status: ''
  },

  onLoad(options) {
    if (options.id) {
      this.setData({ toolId: options.id });
      this.loadToolDetail(options.id);
    }
  },

  // 加载工具详情
  async loadToolDetail(id) {
    wx.showLoading({ title: '加载中...' });

    try {
      const res = await request({
        url: `/api/tools/${id}`,
        method: 'GET'
      });

      const tool = res.tool || res.data || res;
      const categoryIndex = this.data.categoryValues.indexOf(tool.category);
      const conditionIndex = this.data.conditionValues.indexOf(tool.condition);
      const durationIndex = this.getDurationIndex(tool.maxDays);

      this.setData({
        images: processToolImages(tool.images),
        name: tool.name,
        categoryIndex,
        price: tool.price.toString(),
        conditionIndex,
        isFree: tool.isFree,
        rentFee: tool.rentFee ? tool.rentFee.toString() : '',
        deposit: tool.deposit.toString(),
        durationIndex,
        pickupMethods: tool.pickupMethod ? tool.pickupMethod.split(',') : [],
        description: tool.description || '',
        status: tool.status
      });
    } catch (err) {
      wx.hideLoading();
      wx.showToast({ title: err.message || '加载失败', icon: 'none' });
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
      return;
    }
    wx.hideLoading();
  },

  // 获取时长索引
  getDurationIndex(maxDays) {
    const durationMap = {
      1: 0,
      3: 1,
      7: 2,
      14: 3,
      30: 4,
      999: 5
    };
    return durationMap[maxDays] || -1;
  },

  // 选择图片
  chooseImage() {
    const remainCount = 3 - this.data.images.length;
    wx.chooseMedia({
      count: remainCount,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const tempFiles = res.tempFiles;
        tempFiles.forEach(file => {
          if (file.size > 5 * 1024 * 1024) {
            wx.showToast({ title: '图片大小不能超过5MB', icon: 'none' });
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
          // 相对路径拼接baseUrl
          const url = data.url.startsWith('/') ? app.globalData.baseUrl + data.url : data.url;
          const images = [...this.data.images, url];
          this.setData({ images });
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

  // 删除图片
  deleteImage(e) {
    const index = e.currentTarget.dataset.index;
    const images = this.data.images.filter((_, i) => i !== index);
    this.setData({ images });
  },

  // 输入工具名称
  onNameInput(e) {
    this.setData({ name: e.detail.value });
  },

  // 选择分类
  onCategoryChange(e) {
    this.setData({ categoryIndex: parseInt(e.detail.value) });
  },

  // 输入价格
  onPriceInput(e) {
    this.setData({ price: e.detail.value });
  },

  // 选择新旧程度
  onConditionChange(e) {
    this.setData({ conditionIndex: parseInt(e.detail.value) });
  },

  // 切换是否收费
  onFreeChange(e) {
    this.setData({ 
      isFree: !e.detail.value,
      rentFee: e.detail.value ? '' : this.data.rentFee
    });
  },

  // 输入租金
  onRentFeeInput(e) {
    this.setData({ rentFee: e.detail.value });
  },

  // 输入押金
  onDepositInput(e) {
    this.setData({ deposit: e.detail.value });
  },

  // 选择可借时长
  onDurationChange(e) {
    this.setData({ durationIndex: parseInt(e.detail.value) });
  },

  // 选择取还方式
  onPickupMethodChange(e) {
    const method = e.currentTarget.dataset.method;
    let methods = [...this.data.pickupMethods];
    
    if (methods.includes(method)) {
      methods = methods.filter(m => m !== method);
    } else {
      methods.push(method);
    }
    
    this.setData({ pickupMethods: methods });
  },

  // 输入描述
  onDescriptionInput(e) {
    this.setData({ description: e.detail.value });
  },

  // 切换工具状态
  async toggleStatus() {
    const newStatus = this.data.status === 'available' ? 'offline' : 'available';
    
    wx.showLoading({ title: '处理中...' });

    try {
      await request({
        url: `/api/tools/${this.data.toolId}/status`,
        method: 'PUT',
        data: { status: newStatus }
      });

      this.setData({ status: newStatus });
      wx.hideLoading();
      wx.showToast({ 
        title: newStatus === 'available' ? '已上架' : '已下架', 
        icon: 'success' 
      });
    } catch (err) {
      wx.hideLoading();
      wx.showToast({ title: err.message || '操作失败', icon: 'none' });
    }
  },

  // 提交
  async submit() {
    const { 
      toolId, images, name, categoryIndex, price, conditionIndex,
      isFree, rentFee, deposit, durationIndex, pickupMethods, description
    } = this.data;

    // 验证
    if (images.length === 0) {
      wx.showToast({ title: '请上传工具图片', icon: 'none' });
      return;
    }
    if (!name || name.trim().length === 0) {
      wx.showToast({ title: '请输入工具名称', icon: 'none' });
      return;
    }
    if (categoryIndex === -1) {
      wx.showToast({ title: '请选择工具分类', icon: 'none' });
      return;
    }
    if (!price || parseFloat(price) <= 0) {
      wx.showToast({ title: '请输入有效的购买价格', icon: 'none' });
      return;
    }
    if (conditionIndex === -1) {
      wx.showToast({ title: '请选择新旧程度', icon: 'none' });
      return;
    }
    if (!isFree && (!rentFee || parseFloat(rentFee) <= 0)) {
      wx.showToast({ title: '请输入有效的租金', icon: 'none' });
      return;
    }
    if (!deposit || parseFloat(deposit) <= 0) {
      wx.showToast({ title: '请输入有效的押金', icon: 'none' });
      return;
    }
    if (durationIndex === -1) {
      wx.showToast({ title: '请选择可借时长', icon: 'none' });
      return;
    }

    // 计算最大天数
    const maxDaysMap = [1, 3, 7, 14, 30, 999];
    const maxDays = maxDaysMap[durationIndex];

    wx.showLoading({ title: '保存中...' });

    try {
      await request({
        url: `/api/tools/${toolId}`,
        method: 'PUT',
        data: {
          images,
          name: name.trim(),
          category: this.data.categoryValues[categoryIndex],
          price: parseFloat(price),
          condition: this.data.conditionValues[conditionIndex],
          isFree,
          rentFee: isFree ? 0 : parseFloat(rentFee),
          deposit: parseFloat(deposit),
          maxDays,
          pickupMethod: pickupMethods.join(','),
          description: description.trim()
        }
      });

      wx.hideLoading();
      wx.showToast({ title: '保存成功', icon: 'success' });
      
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
    } catch (err) {
      wx.hideLoading();
      wx.showToast({ title: err.message || '保存失败', icon: 'none' });
    }
  }
});
