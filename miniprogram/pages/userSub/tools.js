const app = getApp();
const { request } = require('../../utils/request');

Page({
  data: {
    tools: [],
    loading: true,
    refreshing: false
  },

  onLoad() {
    this.loadMyTools();
  },

  onShow() {
    this.loadMyTools();
  },

  // 下拉刷新
  onPullDownRefresh() {
    this.setData({ refreshing: true });
    this.loadMyTools().then(() => {
      wx.stopPullDownRefresh();
      this.setData({ refreshing: false });
    });
  },

  // 加载我的工具
  async loadMyTools() {
    try {
      const res = await request({
        url: '/api/tools/my',
        method: 'GET'
      });

      this.setData({
        tools: res.tools || [],
        loading: false
      });
    } catch (err) {
      console.error('加载工具失败:', err);
      this.setData({ loading: false });
    }
  },

  // 跳转到工具详情
  goToDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/tool/detail?id=${id}`
    });
  },

  // 跳转到编辑工具
  goToEdit(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/tool/edit?id=${id}`
    });
  },

  // 切换工具状态
  async toggleStatus(e) {
    const { id, status } = e.currentTarget.dataset;
    const newStatus = status === 'available' ? 'offline' : 'available';
    const actionText = newStatus === 'available' ? '上架' : '下架';

    wx.showModal({
      title: '确认操作',
      content: `确定要${actionText}该工具吗？`,
      success: async (res) => {
        if (res.confirm) {
          try {
            await request({
              url: `/api/tools/${id}/status`,
              method: 'PUT',
              data: { status: newStatus }
            });

            wx.showToast({ title: `${actionText}成功`, icon: 'success' });
            this.loadMyTools();
          } catch (err) {
            console.error('操作失败:', err);
            wx.showToast({ title: '操作失败', icon: 'none' });
          }
        }
      }
    });
  },

  // 删除工具
  deleteTool(e) {
    const id = e.currentTarget.dataset.id;

    wx.showModal({
      title: '确认删除',
      content: '删除后无法恢复，确定要删除吗？',
      confirmColor: '#ff4444',
      success: async (res) => {
        if (res.confirm) {
          try {
            await request({
              url: `/api/tools/${id}`,
              method: 'DELETE'
            });

            wx.showToast({ title: '删除成功', icon: 'success' });
            this.loadMyTools();
          } catch (err) {
            console.error('删除失败:', err);
            wx.showToast({ title: '删除失败', icon: 'none' });
          }
        }
      }
    });
  },

  // 跳转到发布工具
  goToPublish() {
    wx.navigateTo({
      url: '/pages/tool/publish'
    });
  }
});
