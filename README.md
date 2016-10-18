# UCHomePage
UC浏览器主页大部分动画效果的演示项目  

**这不是一个可以直接使用的lib，有很多 hardcode 的地方**  

![](https://github.com/29995270/UCHomePage/blob/master/demo.gif "uc browser")

![](https://github.com/29995270/UCHomePage/blob/master/struct.gif "uc browser")

1. ViewPager
2. ViewPager 的第一页，DragTracker，继承自 LinearLayout，用 ViewDragHelper 追踪在这一页的上下拉行为并分发给接收者
3. ViewPager 的第二页，普通布局
4. 与 ViewPager 在同一个FrameLayout 中的 SearchBar 控件，实现了 ViewPager 的 OnPageChangeListener，在 ViewPager 横向滑动的时候修改自己的 mHeight，但其子 view 的高度不变，通过 layout 子 view 到不同的位置实现折叠/展开的效果
5. DragUpReceiver，继承自 ViewGroup，布局方式同垂直的 LinearLayout，用于接收 2 中 DragTracker 的 上拉行为，通过和 4 类似的方式实现折叠/展开效果，并绘制阴影效果
6. 一个与 NewsActivity 中新闻列表一样的布局，长度要求有一屏（上下滑动不会空白），由于DragTracker 是 LinearLayout，所以在上方布局高度变化（上下拉）时，这个新闻列表也会上下滑动
7. SearchBar 布局和 4 一样，但是是处于展开状态，并接受 2 中 DragTracker 的下拉行为，通过改变自身高度 和 绘制背景（贝塞尔曲线的弧形底边效果），实现下拉动画效果
8. 菜单布局

ps.上下拉时会隐藏 4 的 SearchBar， 并展示 7 的 SearchBar
