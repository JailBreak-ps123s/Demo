# GlobalAppJumpBlocker V3

V3 日常使用优化：
- 总开关
- 仅 App -> App
- 两种模式：仅黑名单 / 拦截所有第三方 App（白名单除外）
- 已安装 App 列表，可直接加入黑名单/白名单
- 白名单最高优先级
- 跳转日志
- 允许一次 / 白名单 / 拦截
- 修复拦截层触控参数
- Android 15 / Z Fold6
- GitHub Actions 云端构建

推荐：
1. 第一次使用选择“仅黑名单拦截”。
2. 开启“仅 App -> App”。
3. 将一个测试 App 加入黑名单。
4. 确认拦截正常后，再考虑“拦截所有第三方 App”。

限制：
普通无 Root 第三方 App 不能获得系统级“所有 Intent 发出前拦截”能力。AccessibilityService 方案是覆盖面较广的用户态方案，但不能保证捕获所有内部跳转或系统组件切换。
