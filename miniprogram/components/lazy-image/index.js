Component({
  properties: {
    // 图片地址
    src: {
      type: String,
      value: ''
    },
    // 图片模式
    mode: {
      type: String,
      value: 'aspectFill'
    },
    // 占位图
    placeholder: {
      type: String,
      value: '/images/placeholder.png'
    },
    // 加载失败图
    errorImage: {
      type: String,
      value: '/images/error.png'
    },
    // 自定义样式类
    customClass: {
      type: String,
      value: ''
    }
  },

  data: {
    loaded: false,
    error: false,
    inView: false,
    currentSrc: ''
  },

  lifetimes: {
    attached() {
      // 创建IntersectionObserver监听元素是否进入视口
      this.observer = this.createIntersectionObserver();
      this.observer.relativeToViewport({ bottom: 100 }).observe('.lazy-image-container', (res) => {
        if (res.intersectionRatio > 0 && !this.data.inView) {
          this.setData({ 
            inView: true,
            currentSrc: this.data.src
          });
          // 进入视口后断开观察
          this.observer.disconnect();
        }
      });
    },

    detached() {
      if (this.observer) {
        this.observer.disconnect();
      }
    }
  },

  observers: {
    'src': function(src) {
      if (this.data.inView && src) {
        this.setData({ 
          currentSrc: src,
          loaded: false,
          error: false
        });
      }
    }
  },

  methods: {
    // 图片加载完成
    onLoad() {
      this.setData({ loaded: true });
      this.triggerEvent('load');
    },

    // 图片加载失败
    onError() {
      this.setData({ 
        error: true,
        currentSrc: this.data.errorImage
      });
      this.triggerEvent('error');
    },

    // 点击图片
    onTap() {
      this.triggerEvent('tap');
    }
  }
});
