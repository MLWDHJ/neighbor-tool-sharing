const app = getApp();
const { request } = require('../../utils/request');
const { debounce } = require('../../utils/util');

Page({
  data: {
    keyword: '',
    searchHistory: [],
    hotSearch: [],
    searchResults: [],
    loading: false
  },

  onLoad() {
    this.loadSearchHistory();
    this.loadHotSearch();
  },

  // 加载搜索历史
  loadSearchHistory() {
    const history = wx.getStorageSync('searchHistory') || [];
    this.setData({ searchHistory: history });
  },

  // 加载热门搜索
  async loadHotSearch() {
    try {
      const res = await request({
        url: '/api/tools/hot-search',
        method: 'GET'
      });
      this.setData({ hotSearch: res.keywords || [] });
    } catch (err) {
      console.error('加载热门搜索失败:', err);
    }
  },

  // 输入关键词
  onKeywordInput: debounce(function(e) {
    const keyword = e.detail.value.trim();
    this.setData({ keyword });
    
    if (keyword) {
      this.searchTools(keyword);
    } else {
      this.setData({ searchResults: [] });
    }
  }, 300),

  // 搜索工具
  async searchTools(keyword) {
    if (!keyword) return;

    this.setData({ loading: true });

    try {
      const res = await request({
        url: '/api/tools',
        method: 'GET',
        data: {
          keyword,
          page: 1,
          size: 20
        }
      });

      this.setData({ 
        searchResults: res.tools || [],
        loading: false
      });

      // 保存搜索历史
      this.saveSearchHistory(keyword);
    } catch (err) {
      console.error('搜索失败:', err);
      this.setData({ loading: false });
      wx.showToast({ title: '搜索失败', icon: 'none' });
    }
  },

  // 执行搜索
  onSearch(e) {
    const keyword = e.detail.value.trim();
    if (keyword) {
      this.setData({ keyword });
      this.searchTools(keyword);
    }
  },

  // 清空关键词
  clearKeyword() {
    this.setData({ 
      keyword: '',
      searchResults: []
    });
  },

  // 点击搜索历史
  onHistoryTap(e) {
    const keyword = e.currentTarget.dataset.keyword;
    this.setData({ keyword });
    this.searchTools(keyword);
  },

  // 点击热门搜索
  onHotTap(e) {
    const keyword = e.currentTarget.dataset.keyword;
    this.setData({ keyword });
    this.searchTools(keyword);
  },

  // 保存搜索历史
  saveSearchHistory(keyword) {
    let history = wx.getStorageSync('searchHistory') || [];
    
    // 移除重复项
    history = history.filter(item => item !== keyword);
    
    // 添加到开头
    history.unshift(keyword);
    
    // 最多保存10条
    if (history.length > 10) {
      history = history.slice(0, 10);
    }
    
    wx.setStorageSync('searchHistory', history);
    this.setData({ searchHistory: history });
  },

  // 清空搜索历史
  clearHistory() {
    wx.showModal({
      title: '提示',
      content: '确定清空搜索历史吗？',
      success: (res) => {
        if (res.confirm) {
          wx.removeStorageSync('searchHistory');
          this.setData({ searchHistory: [] });
          
          // 调用后端接口清空搜索历史
          request({
            url: '/api/tools/search-history',
            method: 'DELETE'
          }).catch(err => {
            console.error('清空搜索历史失败:', err);
          });
        }
      }
    });
  },

  // 跳转到工具详情
  goToDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/tool/detail?id=${id}`
    });
  },

  // 返回
  goBack() {
    wx.navigateBack();
  }
});
