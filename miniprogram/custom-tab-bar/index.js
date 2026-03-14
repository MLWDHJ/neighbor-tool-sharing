Component({
  data: {
    selected: 0,
    list: [
      {
        pagePath: "/pages/home/index",
        text: "首页",
        icon: "🏠"
      },
      {
        pagePath: "/pages/message/index",
        text: "消息",
        icon: "💬"
      },
      {
        pagePath: "/pages/user/index",
        text: "我的",
        icon: "👤"
      }
    ]
  },

  methods: {
    switchTab(e) {
      const index = e.currentTarget.dataset.index;
      const item = this.data.list[index];
      
      wx.switchTab({
        url: item.pagePath
      });
    }
  }
});
