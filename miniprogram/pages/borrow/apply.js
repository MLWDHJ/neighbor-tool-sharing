const { request } = require('../../utils/request');
const { formatDate, processToolImages } = require('../../utils/util');

Page({
  data: {
    toolId: null,
    tool: null,
    startDate: '',
    endDate: '',
    borrowDays: 0,
    note: '',
    minDate: '',
    maxDate: ''
  },

  onLoad(options) {
    if (options.toolId) {
      this.setData({ toolId: options.toolId });
      this.loadToolInfo();
      this.initDates();
    }
  },

  // 初始化日期
  initDates() {
    const today = new Date();
    const minDate = formatDate(today);
    
    const maxDateObj = new Date();
    maxDateObj.setMonth(maxDateObj.getMonth() + 3);
    const maxDate = formatDate(maxDateObj);
    
    this.setData({ minDate, maxDate });
  },

  // 加载工具信息
  async loadToolInfo() {
    wx.showLoading({ title: '加载中...' });
    
    try {
      const res = await request({
        url: `/api/tools/${this.data.toolId}`,
        method: 'GET'
      });
      
      const tool = res.tool;
      tool.images = processToolImages(tool.images);
      this.setData({ tool });
    } catch (err) {
      wx.showToast({ title: err.message || '加载失败', icon: 'none' });
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
    } finally {
      wx.hideLoading();
    }
  },

  // 选择开始日期
  onStartDateChange(e) {
    const startDate = e.detail.value;
    this.setData({ startDate }, () => {
      this.calculateDays();
    });
  },

  // 选择结束日期
  onEndDateChange(e) {
    const endDate = e.detail.value;
    this.setData({ endDate }, () => {
      this.calculateDays();
    });
  },

  // 计算借用天数
  calculateDays() {
    const { startDate, endDate, tool } = this.data;
    
    if (!startDate || !endDate || !tool) return;
    
    const start = new Date(startDate);
    const end = new Date(endDate);
    
    if (end < start) {
      wx.showToast({ title: '归还日期不能早于开始日期', icon: 'none' });
      this.setData({ borrowDays: 0 });
      return;
    }
    
    const borrowDays = Math.ceil((end - start) / (1000 * 60 * 60 * 24)) + 1;
    
    if (tool.maxDays && borrowDays > tool.maxDays) {
      wx.showToast({ title: `最多借用${tool.maxDays}天`, icon: 'none' });
      this.setData({ borrowDays: 0 });
      return;
    }
    
    this.setData({ borrowDays });
  },

  // 输入借用说明
  onNoteInput(e) {
    this.setData({ note: e.detail.value });
  },

  // 提交申请
  async submit() {
    const { toolId, startDate, endDate, borrowDays, note, tool } = this.data;
    
    // 验证
    if (!startDate) {
      wx.showToast({ title: '请选择开始时间', icon: 'none' });
      return;
    }
    if (!endDate) {
      wx.showToast({ title: '请选择归还时间', icon: 'none' });
      return;
    }
    if (borrowDays <= 0) {
      wx.showToast({ title: '请选择有效的借用时间', icon: 'none' });
      return;
    }
    if (tool.maxDays && borrowDays > tool.maxDays) {
      wx.showToast({ title: `最多借用${tool.maxDays}天`, icon: 'none' });
      return;
    }
    
    wx.showLoading({ title: '提交中...' });
    
    try {
      await request({
        url: '/api/borrows',
        method: 'POST',
        data: {
          toolId: parseInt(toolId),
          startDate,
          endDate,
          note: note.trim()
        }
      });
      
      wx.hideLoading();
      wx.showToast({ title: '申请成功', icon: 'success' });
      
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
    } catch (err) {
      wx.hideLoading();
      wx.showToast({ title: err.message || '申请失败', icon: 'none' });
    }
  }
});
