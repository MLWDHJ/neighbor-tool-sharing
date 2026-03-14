const app = getApp();
const { request } = require('../../utils/request');
const { formatDate, processToolImages } = require('../../utils/util');

Page({
  data: {
    borrowId: null,
    status: '',
    statusText: '',
    statusClass: '',
    createTime: '',
    tool: {},
    borrower: {},
    lender: {},
    startDate: '',
    endDate: '',
    days: 0,
    note: '',
    rentAmount: 0,
    depositAmount: 0,
    totalAmount: 0,
    rejectReason: '',
    actualReturnDate: '',
    isDamaged: false,
    damageNote: '',
    deductAmount: 0,
    isLender: false,
    showActions: false,
    hasReviewed: false,
    showRejectModal: false,
    showConfirmModal: false
  },

  onLoad(options) {
    if (options.id) {
      this.setData({ borrowId: options.id });
      this.loadBorrowDetail(options.id);
    }
  },

  // 加载借用详情
  async loadBorrowDetail(id) {
    wx.showLoading({ title: '加载中...' });

    try {
      const res = await request({
        url: `/api/borrows/${id}`,
        method: 'GET'
      });

      const borrow = res.borrow;
      const userInfo = wx.getStorageSync('userInfo');
      const isLender = borrow.lenderId === userInfo.id;

      // 状态映射
      const statusMap = {
        'pending': { text: '待处理', class: 'pending' },
        'in_use': { text: '进行中', class: 'in-use' },
        'rejected': { text: '已拒绝', class: 'rejected' },
        'returned': { text: '已归还', class: 'returned' },
        'cancelled': { text: '已取消', class: 'cancelled' }
      };

      const statusInfo = statusMap[borrow.status] || { text: '未知', class: '' };

      // 处理工具图片URL
      if (borrow.tool && borrow.tool.images) {
        borrow.tool.images = processToolImages(borrow.tool.images);
      }

      this.setData({
        status: borrow.status,
        statusText: statusInfo.text,
        statusClass: statusInfo.class,
        createTime: formatDate(borrow.createdAt),
        tool: borrow.tool,
        borrower: borrow.borrower,
        lender: borrow.lender,
        startDate: borrow.startDate,
        endDate: borrow.endDate,
        days: borrow.days,
        note: borrow.note,
        rentAmount: borrow.rentAmount,
        depositAmount: borrow.depositAmount,
        totalAmount: parseFloat(borrow.rentAmount || 0) + parseFloat(borrow.depositAmount || 0),
        rejectReason: borrow.rejectReason || '',
        actualReturnDate: borrow.actualReturnDate ? formatDate(borrow.actualReturnDate) : '',
        isDamaged: borrow.isDamaged || false,
        damageNote: borrow.damageNote || '',
        deductAmount: borrow.deductAmount || 0,
        isLender,
        showActions: this.shouldShowActions(borrow.status, isLender),
        hasReviewed: borrow.hasReviewed || false
      });
    } catch (err) {
      wx.showToast({ title: err.message || '加载失败', icon: 'none' });
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
    } finally {
      wx.hideLoading();
    }
  },

  // 判断是否显示操作按钮
  shouldShowActions(status, isLender) {
    if (status === 'pending' && isLender) return true;
    if (status === 'in_use' && !isLender) return true;
    if (status === 'returned') return true;
    return false;
  },

  // 显示拒绝弹窗
  showRejectModal() {
    this.setData({ showRejectModal: true });
  },

  // 隐藏拒绝弹窗
  hideRejectModal() {
    this.setData({ showRejectModal: false, rejectReason: '' });
  },

  // 输入拒绝原因
  onRejectReasonInput(e) {
    this.setData({ rejectReason: e.detail.value });
  },

  // 拒绝申请
  async reject() {
    wx.showLoading({ title: '处理中...' });

    try {
      await request({
        url: `/api/borrows/${this.data.borrowId}/reject`,
        method: 'PUT',
        data: {
          reason: this.data.rejectReason
        }
      });

      wx.showToast({ title: '已拒绝', icon: 'success' });
      
      setTimeout(() => {
        this.hideRejectModal();
        this.loadBorrowDetail(this.data.borrowId);
      }, 1500);
    } catch (err) {
      wx.showToast({ title: err.message || '操作失败', icon: 'none' });
    } finally {
      wx.hideLoading();
    }
  },

  // 同意申请
  async approve() {
    wx.showModal({
      title: '确认同意',
      content: '同意后工具将被标记为已借出',
      success: async (res) => {
        if (res.confirm) {
          wx.showLoading({ title: '处理中...' });

          try {
            await request({
              url: `/api/borrows/${this.data.borrowId}/approve`,
              method: 'PUT'
            });

            wx.showToast({ title: '已同意', icon: 'success' });
            
            setTimeout(() => {
              this.loadBorrowDetail(this.data.borrowId);
            }, 1500);
          } catch (err) {
            wx.showToast({ title: err.message || '操作失败', icon: 'none' });
          } finally {
            wx.hideLoading();
          }
        }
      }
    });
  },

  // 标记已归还
  async markReturned() {
    wx.showModal({
      title: '确认归还',
      content: '确认已将工具归还给出借人？',
      success: async (res) => {
        if (res.confirm) {
          wx.showLoading({ title: '处理中...' });

          try {
            await request({
              url: `/api/borrows/${this.data.borrowId}/return`,
              method: 'PUT'
            });

            wx.showToast({ title: '已标记归还', icon: 'success' });
            
            setTimeout(() => {
              this.loadBorrowDetail(this.data.borrowId);
            }, 1500);
          } catch (err) {
            wx.showToast({ title: err.message || '操作失败', icon: 'none' });
          } finally {
            wx.hideLoading();
          }
        }
      }
    });
  },

  // 显示确认归还弹窗
  showConfirmModal() {
    this.setData({ showConfirmModal: true });
  },

  // 隐藏确认归还弹窗
  hideConfirmModal() {
    this.setData({ 
      showConfirmModal: false,
      isDamaged: false,
      damageNote: '',
      deductAmount: 0
    });
  },

  // 切换损坏状态
  onDamagedChange(e) {
    this.setData({ isDamaged: e.detail.value });
  },

  // 输入损坏说明
  onDamageNoteInput(e) {
    this.setData({ damageNote: e.detail.value });
  },

  // 输入扣除金额
  onDeductAmountInput(e) {
    this.setData({ deductAmount: e.detail.value });
  },

  // 确认归还
  async confirmReturn() {
    const { isDamaged, damageNote, deductAmount, depositAmount } = this.data;

    // 验证
    if (isDamaged && !damageNote) {
      wx.showToast({ title: '请输入损坏说明', icon: 'none' });
      return;
    }
    if (isDamaged && (!deductAmount || parseFloat(deductAmount) <= 0)) {
      wx.showToast({ title: '请输入扣除金额', icon: 'none' });
      return;
    }
    if (isDamaged && parseFloat(deductAmount) > depositAmount) {
      wx.showToast({ title: '扣除金额不能超过押金', icon: 'none' });
      return;
    }

    wx.showLoading({ title: '处理中...' });

    try {
      await request({
        url: `/api/borrows/${this.data.borrowId}/confirm`,
        method: 'PUT',
        data: {
          isDamaged,
          damageNote: isDamaged ? damageNote : '',
          deductAmount: isDamaged ? parseFloat(deductAmount) : 0
        }
      });

      wx.showToast({ title: '归还已确认', icon: 'success' });
      
      setTimeout(() => {
        this.hideConfirmModal();
        this.loadBorrowDetail(this.data.borrowId);
      }, 1500);
    } catch (err) {
      wx.showToast({ title: err.message || '操作失败', icon: 'none' });
    } finally {
      wx.hideLoading();
    }
  },

  // 跳转到工具详情
  goToToolDetail() {
    wx.navigateTo({
      url: `/pages/tool/detail?id=${this.data.tool.id}`
    });
  },

  // 跳转到评价页
  goToReview() {
    wx.navigateTo({
      url: `/pages/borrow/review?borrowId=${this.data.borrowId}`
    });
  },

  // 阻止冒泡
  stopPropagation() {}
});
