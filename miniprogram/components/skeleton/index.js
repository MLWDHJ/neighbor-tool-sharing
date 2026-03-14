Component({
  properties: {
    // 骨架屏类型: list, detail, card
    type: {
      type: String,
      value: 'list'
    },
    // 列表项数量
    count: {
      type: Number,
      value: 3
    },
    // 是否显示
    loading: {
      type: Boolean,
      value: true
    }
  },

  data: {
    items: []
  },

  lifetimes: {
    attached() {
      this.setData({
        items: Array(this.data.count).fill(0)
      });
    }
  }
});
