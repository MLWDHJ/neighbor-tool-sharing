const app = getApp();
const { request } = require('../../utils/request');
const { processToolImages } = require('../../utils/util');

Page({
  data: {
    borrowId: null,
    borrowInfo: {},
    rating: 5,
    selectedTags: [],
    content: '',
    contentLength: 0,
    maxLength: 200,
    isSubmitting: false,
    tags: [
      '工具好用',
      '按时归还',
      '爱惜工具',
      '沟通顺畅',
      '友好热情',
      '响应及时'
    ]
  },

  onLoad(options) {
    if (options.borrowId) {
      this.setData({ borrowId: options.borrowId });
      this.loadBorrowInfo(options.borrowId);
    }
  },

  // 加载借用信息
  async loadBorrowInfo(borrowId) {
    try {
      const res = await request({
        url: `/api/borrows/${borrowId}`,
        method: 'GET'
      });

      if (res.borrow) {
        if (res.borrow.tool && res.borrow.tool.images) {
          res.borrow.tool.images = processToolImages(res.borrow.tool.images);
        }
        this.setData({ borrowInfo: res.borrow });
      }
    } catch (err) {
      console.error('加载借用信息失败:', err);
    }
  },

  // 选择评分
  selectRating(e) {
    const rating = e.currentTarget.dataset.rating;
    this.setData({ rating });
  },

  // 切换标签
  toggleTag(e) {
    const tag = e.currentTarget.dataset.tag;
    const { selectedTags } = this.data;
    
    const index = selectedTags.indexOf(tag);
    if (index > -1) {
      selectedTags.splice(index, 1);
    } else {
      selectedTags.push(tag);
    }
    
    this.setData({ selectedTags });
  },

  // 输入评价内容
  onContentInput(e) {
    const content = e.detail.value;
    this.setData({
      content,
      contentLength: content.length
    });
  },

  // 提交评价
  async submitReview() {
    const { borrowId, rating, selectedTags, content } = this.data;

    if (rating === 0) {
      wx.showToast({ title: '请选择评分', icon: 'none' });
      return;
    }

    this.setData({ isSubmitting: true });

    try {
      await request({
        url: '/api/reviews',
        method: 'POST',
        data: {
          borrowId,
          rating,
          tags: selectedTags,
          comment: content.trim()
        }
      });

      wx.showToast({ title: '评价成功', icon: 'success' });
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
    } catch (err) {
      console.error('评价失败:', err);
      wx.showToast({ title: '评价失败', icon: 'none' });
    } finally {
      this.setData({ isSubmitting: false });
    }
  }
});
