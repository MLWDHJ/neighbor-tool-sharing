const app = getApp();
const { request } = require('../../utils/request');

Page({
  data: {
    faqs: [],
    expandedIndex: -1,
    feedbackContent: '',
    feedbackContact: '',
    showFeedback: false,
    isSubmitting: false
  },

  onLoad() {
    this.loadFaqs();
  },

  // 加载常见问题
  async loadFaqs() {
    try {
      const res = await request({
        url: '/api/help/faqs',
        method: 'GET',
        needAuth: false
      });

      this.setData({
        faqs: res.faqs || this.getDefaultFaqs()
      });
    } catch (err) {
      console.error('加载FAQ失败:', err);
      this.setData({
        faqs: this.getDefaultFaqs()
      });
    }
  },

  // 默认FAQ
  getDefaultFaqs() {
    return [
      {
        id: 1,
        question: '如何发布工具？',
        answer: '点击首页右下角的"+"按钮，填写工具信息（名称、分类、图片、租金等），点击发布即可。'
      },
      {
        id: 2,
        question: '如何借用工具？',
        answer: '在首页浏览或搜索工具，点击进入详情页，选择借用日期后提交申请，等待工具主人审批。'
      },
      {
        id: 3,
        question: '押金如何退还？',
        answer: '归还工具并经出借人确认无损坏后，押金将在1-3个工作日内原路退还。'
      },
      {
        id: 4,
        question: '如何提高信用分？',
        answer: '按时归还工具、获得好评、完成实名认证都可以提高信用分。逾期归还或损坏工具会扣除信用分。'
      },
      {
        id: 5,
        question: '工具损坏怎么办？',
        answer: '如借用期间工具损坏，出借人可在确认归还时标记损坏并从押金中扣除相应赔偿。'
      },
      {
        id: 6,
        question: '如何联系工具主人？',
        answer: '提交借用申请后，可在借用详情页通过消息功能与工具主人沟通。'
      }
    ];
  },

  // 展开/收起FAQ
  toggleFaq(e) {
    const index = e.currentTarget.dataset.index;
    this.setData({
      expandedIndex: this.data.expandedIndex === index ? -1 : index
    });
  },

  // 显示反馈弹窗
  showFeedbackModal() {
    this.setData({ showFeedback: true });
  },

  // 隐藏反馈弹窗
  hideFeedbackModal() {
    this.setData({
      showFeedback: false,
      feedbackContent: '',
      feedbackContact: ''
    });
  },

  // 输入反馈内容
  onFeedbackInput(e) {
    this.setData({ feedbackContent: e.detail.value });
  },

  // 输入联系方式
  onContactInput(e) {
    this.setData({ feedbackContact: e.detail.value });
  },

  // 提交反馈
  async submitFeedback() {
    const { feedbackContent, feedbackContact } = this.data;

    if (!feedbackContent.trim()) {
      wx.showToast({ title: '请输入反馈内容', icon: 'none' });
      return;
    }

    this.setData({ isSubmitting: true });

    try {
      await request({
        url: '/api/help/feedback',
        method: 'POST',
        data: {
          content: feedbackContent.trim(),
          contact: feedbackContact.trim()
        }
      });

      wx.showToast({ title: '反馈提交成功', icon: 'success' });
      this.hideFeedbackModal();
    } catch (err) {
      console.error('提交反馈失败:', err);
      wx.showToast({ title: '提交失败', icon: 'none' });
    } finally {
      this.setData({ isSubmitting: false });
    }
  },

  // 拨打客服电话
  callService() {
    wx.makePhoneCall({
      phoneNumber: '400-123-4567',
      fail: () => {}
    });
  }
});
