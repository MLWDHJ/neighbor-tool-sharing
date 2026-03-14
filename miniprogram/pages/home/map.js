const app = getApp();
const { request } = require('../../utils/request');
const { processToolImages } = require('../../utils/util');

Page({
  data: {
    latitude: 39.908823,
    longitude: 116.397470,
    scale: 14,
    markers: [],
    tools: [],
    selectedTool: null,
    showToolCard: false,
    loading: true
  },

  onLoad() {
    this.getLocation();
  },

  onShow() {
    this.loadNearbyTools();
  },

  // 获取当前位置
  getLocation() {
    wx.getLocation({
      type: 'gcj02',
      success: (res) => {
        this.setData({
          latitude: res.latitude,
          longitude: res.longitude
        });
        this.loadNearbyTools();
      },
      fail: (err) => {
        console.error('获取位置失败:', err);
        wx.showToast({ title: '获取位置失败', icon: 'none' });
        this.loadNearbyTools();
      }
    });
  },

  // 加载附近工具
  async loadNearbyTools() {
    const { latitude, longitude } = this.data;

    try {
      const res = await request({
        url: '/api/tools',
        method: 'GET',
        data: {
          latitude,
          longitude,
          distance: 5,
          status: 'available',
          page: 1,
          size: 50
        }
      });

      const tools = (res.tools || []).map(t => {
        t.images = processToolImages(t.images);
        return t;
      });
      const markers = this.generateMarkers(tools);

      this.setData({
        tools,
        markers,
        loading: false
      });
    } catch (err) {
      console.error('加载工具失败:', err);
      this.setData({ loading: false });
    }
  },

  // 生成地图标记
  generateMarkers(tools) {
    return tools.map((tool, index) => ({
      id: tool.id,
      latitude: parseFloat(tool.latitude) || this.data.latitude + (Math.random() - 0.5) * 0.01,
      longitude: parseFloat(tool.longitude) || this.data.longitude + (Math.random() - 0.5) * 0.01,
      width: 32,
      height: 32,
      callout: {
        content: tool.name,
        color: '#333333',
        fontSize: 12,
        borderRadius: 4,
        padding: 6,
        display: 'BYCLICK',
        bgColor: '#ffffff'
      },
      label: {
        content: '🔧',
        color: '#FF6B35',
        fontSize: 20,
        anchorX: 0,
        anchorY: 0
      }
    }));
  },

  // 点击标记
  onMarkerTap(e) {
    const markerId = e.markerId;
    const tool = this.data.tools.find(t => t.id === markerId);
    
    if (tool) {
      this.setData({
        selectedTool: tool,
        showToolCard: true
      });
    }
  },

  // 关闭工具卡片
  closeToolCard() {
    this.setData({
      showToolCard: false,
      selectedTool: null
    });
  },

  // 跳转到工具详情
  goToDetail() {
    const { selectedTool } = this.data;
    if (selectedTool) {
      wx.navigateTo({
        url: `/pages/tool/detail?id=${selectedTool.id}`
      });
    }
  },

  // 切换到列表视图
  switchToList() {
    wx.switchTab({
      url: '/pages/home/index'
    });
  },

  // 回到当前位置
  moveToLocation() {
    this.mapCtx = wx.createMapContext('toolMap');
    this.mapCtx.moveToLocation();
  },

  // 地图区域改变
  onRegionChange(e) {
    if (e.type === 'end' && e.causedBy === 'drag') {
      // 可以在这里加载新区域的工具
    }
  }
});
