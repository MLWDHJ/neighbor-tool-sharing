const app = getApp();
const { request } = require('../../utils/request');
const { formatDate, processToolImages } = require('../../utils/util');

Page({
  data: {
    activeTab: 'borrow', // borrow: 我的借用, lend: 我的出借
    statusFilter: '', // 状态筛选
    list: [],
    page: 1,
    size: 20,
    loading: false,
    hasMore: true
  },

  onLoad(options) {
    // 从参数获取初始标签页
    if (options.tab) {
      this.setData({ activeTab: options.tab });
    }
    this.loadList();
  },

  // 切换标签页
  switchTab(e) {
    const tab = e.currentTarget.dataset.tab;
    if (tab === this.data.activeTab) return;

    this.setData({
      activeTab: tab,
      statusFilter: '',
      list: [],
      page: 1,
      hasMore: true
    });
    this.loadList();
  },

  // 按状态筛选
  filterByStatus(e) {
    const status = e.currentTarget.dataset.status;
    if (status === this.data.statusFilter) return;

    this.setData({
      statusFilter: status,
      list: [],
      page: 1,
      hasMore: true
    });
    this.loadList();
  },

  // 加载列表
  async loadList() {
    if (this.data.loading || !this.data.hasMore) return;

    this.setData({ loading: true });

    try {
      const { activeTab, statusFilter, page, size } = this.data;
      const url = activeTab === 'borrow' ? '/api/borrows/my' : '/api/borrows/my-lends';

      const res = await request({
        url,
        method: 'GET',
        data: {
          status: statusFilter || undefined,
          page,
          size
        }
      });

      const dataKey = activeTab === 'borrow' ? 'borrows' : 'lends';
      const newList = res[dataKey] || [];
      const userInfo = wx.getStorageSync('userInfo');

      // 状态映射
      const statusMap = {
        'pending': '待处理',
        'in_use': '进行中',
        'rejected': '已拒绝',
        'returned': '已归还',
        'cancelled': '已取消'
      };

      // 处理数据
      const processedList = newList.map(item => {
        if (item.tool && item.tool.images) {
          item.tool.images = processToolImages(item.tool.images);
        }
        return {
          ...item,
          statusText: statusMap[item.status] || '未知',
          createTime: formatDate(item.createdAt),
          totalAmount: parseFloat(item.rentAmount || 0) + parseFloat(item.depositAmount || 0),
          otherUser: activeTab === 'borrow' ? item.lender : item.borrower
        };
      });

      this.setData({
        list: page === 1 ? processedList : [...this.data.list, ...processedList],
        hasMore: newList.length === size,
        page: page + 1
      });
    } catch (err) {
      wx.showToast({
        title: err.message || '加载失败',
        icon: 'none'
      });
    } finally {
      this.setData({ loading: false });
    }
  },

  // 加载更多
  loadMore() {
    if (!this.data.loading && this.data.hasMore) {
      this.loadList();
    }
  },

  // 跳转到详情页
  goToDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/borrow/detail?id=${id}`
    });
  },

  // 下拉刷新
  onPullDownRefresh() {
    this.setData({
      list: [],
      page: 1,
      hasMore: true
    });
    this.loadList().then(() => {
      wx.stopPullDownRefresh();
    });
  }
});
