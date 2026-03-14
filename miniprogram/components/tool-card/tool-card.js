const util = require('../../utils/util.js');

Component({
  properties: {
    tool: {
      type: Object,
      value: {}
    },
    userLocation: {
      type: Object,
      value: null
    }
  },

  data: {
    distance: '',
    imgError: false,
    categoryMap: {
      'electric': '电动工具',
      'manual': '手动工具',
      'outdoor': '户外工具',
      'digital': '数码设备',
      'daily': '日常用品'
    },
    conditionMap: {
      'new': '全新',
      'like_new': '几乎全新',
      'good': '良好',
      'fair': '一般'
    }
  },

  observers: {
    'tool': function(tool) {
      if (tool && tool.images && tool.images.length > 0) {
        // 处理图片URL，将相对路径转为完整URL
        const imageUrl = util.processImageUrl(tool.images[0]);
        this.setData({ displayImageUrl: imageUrl, imgError: false });
      }
    },
    'tool, userLocation': function(tool, userLocation) {
      if (tool && userLocation) {
        this.calculateDistance();
      }
    }
  },

  methods: {
    // 计算距离
    calculateDistance() {
      const { tool, userLocation } = this.properties;
      if (!tool || !userLocation) return;
      
      const distance = this.getDistance(
        userLocation.latitude,
        userLocation.longitude,
        tool.latitude,
        tool.longitude
      );
      
      this.setData({
        distance: util.formatDistance(distance)
      });
    },

    // Haversine公式计算距离
    getDistance(lat1, lon1, lat2, lon2) {
      const R = 6371000; // 地球半径（米）
      const dLat = this.toRad(lat2 - lat1);
      const dLon = this.toRad(lon2 - lon1);
      const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(this.toRad(lat1)) * Math.cos(this.toRad(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);
      const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
      return R * c;
    },

    toRad(deg) {
      return deg * (Math.PI / 180);
    },

    // 图片加载失败时显示占位视图
    onImageError() {
      this.setData({ imgError: true });
    },

    // 点击卡片
    onTap() {
      wx.navigateTo({
        url: `/pages/tool/detail?id=${this.properties.tool.id}`
      });
    }
  }
});
