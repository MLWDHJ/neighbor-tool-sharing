const app = getApp();
const { request } = require('../../utils/request');
const { formatDate, formatTime } = require('../../utils/util');

Page({
  data: {
    messages: [],
    unreadCount: 0,
    page: 1,
    size: 20,
    loading: false,
    hasMore: true
  },

  onLoad() {
    this.loadMessages();
  },

  onShow() {
    // 设置TabBar选中状态
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 1 });
    }
    // 每次显示页面时刷新消息列表
    this.setData({
      messages: [],
      page: 1,
      hasMore: true
    });
    this.loadMessages();
  },

  // 加载消息列表
  async loadMessages() {
    if (this.data.loading || !this.data.hasMore) return;

    this.setData({ loading: true });

    try {
      const { page, size } = this.data;
      const res = await request({
        url: '/api/messages',
        method: 'GET',
        data: { page, size }
      });

      const newMessages = res.messages || [];
      
      // 处理消息数据
      const processedMessages = newMessages.map(msg => {
        // 消息类型映射
        const typeMap = {
          'borrow_request': { iconType: 'borrow', iconText: '借' },
          'request_approved': { iconType: 'borrow', iconText: '准' },
          'request_rejected': { iconType: 'borrow', iconText: '拒' },
          'return_reminder': { iconType: 'borrow', iconText: '还' },
          'overdue_reminder': { iconType: 'borrow', iconText: '逾' },
          'return_confirmed': { iconType: 'borrow', iconText: '确' },
          'review_received': { iconType: 'review', iconText: '评' },
          'system': { iconType: 'system', iconText: '系' }
        };

        const typeInfo = typeMap[msg.type] || { iconType: 'system', iconText: '系' };

        return {
          ...msg,
          ...typeInfo,
          timeText: this.formatMessageTime(msg.createdAt)
        };
      });

      this.setData({
        messages: page === 1 ? processedMessages : [...this.data.messages, ...processedMessages],
        unreadCount: res.unreadCount || 0,
        hasMore: newMessages.length === size,
        page: page + 1
      });

      // 更新tabBar未读数
      if (res.unreadCount > 0) {
        wx.setTabBarBadge({
          index: 1,
          text: res.unreadCount > 99 ? '99+' : String(res.unreadCount)
        });
      } else {
        wx.removeTabBarBadge({ index: 1 });
      }
    } catch (err) {
      wx.showToast({
        title: err.message || '加载失败',
        icon: 'none'
      });
    } finally {
      this.setData({ loading: false });
    }
  },

  // 格式化消息时间
  formatMessageTime(dateStr) {
    const date = new Date(dateStr);
    const now = new Date();
    const diff = now - date;
    const oneDay = 24 * 60 * 60 * 1000;

    if (diff < 60 * 1000) {
      return '刚刚';
    } else if (diff < 60 * 60 * 1000) {
      return Math.floor(diff / (60 * 1000)) + '分钟前';
    } else if (diff < oneDay) {
      return Math.floor(diff / (60 * 60 * 1000)) + '小时前';
    } else if (diff < 2 * oneDay) {
      return '昨天';
    } else if (diff < 7 * oneDay) {
      return Math.floor(diff / oneDay) + '天前';
    } else {
      return formatDate(dateStr);
    }
  },

  // 处理消息点击
  async handleMessageTap(e) {
    const item = e.currentTarget.dataset.item;

    // 标记为已读
    if (!item.isRead) {
      try {
        await request({
          url: `/api/messages/${item.id}/read`,
          method: 'PUT'
        });

        // 更新本地状态
        const messages = this.data.messages.map(msg => {
          if (msg.id === item.id) {
            return { ...msg, isRead: true };
          }
          return msg;
        });

        this.setData({
          messages,
          unreadCount: Math.max(0, this.data.unreadCount - 1)
        });

        // 更新tabBar
        const newCount = this.data.unreadCount;
        if (newCount > 0) {
          wx.setTabBarBadge({
            index: 1,
            text: newCount > 99 ? '99+' : String(newCount)
          });
        } else {
          wx.removeTabBarBadge({ index: 1 });
        }
      } catch (err) {
        console.error('标记已读失败:', err);
      }
    }

    // 跳转到相关页面
    this.navigateToRelated(item);
  },

  // 跳转到相关页面
  navigateToRelated(item) {
    if (!item.relatedId) return;

    const typeRouteMap = {
      'borrow_request': `/pages/borrow/detail?id=${item.relatedId}`,
      'request_approved': `/pages/borrow/detail?id=${item.relatedId}`,
      'request_rejected': `/pages/borrow/detail?id=${item.relatedId}`,
      'return_reminder': `/pages/borrow/detail?id=${item.relatedId}`,
      'overdue_reminder': `/pages/borrow/detail?id=${item.relatedId}`,
      'return_confirmed': `/pages/borrow/detail?id=${item.relatedId}`,
      'review_received': `/pages/tool/detail?id=${item.relatedId}`
    };

    const url = typeRouteMap[item.type];
    if (url) {
      wx.navigateTo({ url });
    }
  },

  // 全部已读
  async markAllAsRead() {
    wx.showLoading({ title: '处理中...' });

    try {
      await request({
        url: '/api/messages/read-all',
        method: 'PUT'
      });

      // 更新本地状态
      const messages = this.data.messages.map(msg => ({
        ...msg,
        isRead: true
      }));

      this.setData({
        messages,
        unreadCount: 0
      });

      // 移除tabBar未读标记
      wx.removeTabBarBadge({ index: 1 });

      wx.hideLoading();
      wx.showToast({
        title: '全部已读',
        icon: 'success'
      });
    } catch (err) {
      wx.hideLoading();
      wx.showToast({
        title: err.message || '操作失败',
        icon: 'none'
      });
    }
  },

  // 删除消息
  async deleteMessage(e) {
    const { id, index } = e.currentTarget.dataset;

    wx.showModal({
      title: '确认删除',
      content: '确定要删除这条消息吗？',
      success: async (res) => {
        if (res.confirm) {
          wx.showLoading({ title: '删除中...' });

          try {
            await request({
              url: `/api/messages/${id}`,
              method: 'DELETE'
            });

            // 更新本地状态
            const messages = [...this.data.messages];
            const deletedMsg = messages[index];
            messages.splice(index, 1);

            const unreadCount = deletedMsg.isRead 
              ? this.data.unreadCount 
              : Math.max(0, this.data.unreadCount - 1);

            this.setData({
              messages,
              unreadCount
            });

            // 更新tabBar
            if (unreadCount > 0) {
              wx.setTabBarBadge({
                index: 1,
                text: unreadCount > 99 ? '99+' : String(unreadCount)
              });
            } else {
              wx.removeTabBarBadge({ index: 1 });
            }

            wx.hideLoading();
            wx.showToast({
              title: '删除成功',
              icon: 'success'
            });
          } catch (err) {
            wx.hideLoading();
            wx.showToast({
              title: err.message || '删除失败',
              icon: 'none'
            });
          }
        }
      }
    });
  },

  // 加载更多
  loadMore() {
    if (!this.data.loading && this.data.hasMore) {
      this.loadMessages();
    }
  },

  // 下拉刷新
  onPullDownRefresh() {
    this.setData({
      messages: [],
      page: 1,
      hasMore: true
    });
    this.loadMessages().then(() => {
      wx.stopPullDownRefresh();
    });
  },

});
