# Progress

- 2026-08-26：建立本次排查记录。
- 2026-08-26：确认公共图书 API 正常，受保护推荐 API 未带令牌时按预期返回 401。
- 2026-08-26：用 Origin + Access-Control-Request-Headers 模拟浏览器预检，稳定复现：登录 OPTIONS=200，其他内容接口 OPTIONS=401。
- 2026-08-26：已定位为 Spring Security 未正确处理跨域 OPTIONS 预检；尚未改动业务代码。

- 2026-08-26：RED：新增 SecurityCorsPreflightTests，期望跨域 OPTIONS 返回 200 和允许来源响应头。尚未改 SecurityConfig。

- 2026-08-26：测试框架未包含 WebMvcTest，改用项目已具备的 SpringBootTest + MockMvc；测试仍只覆盖 OPTIONS 预检行为。

- 2026-08-26：第一次 SpringBootTest/MockMvc 未加入 Spring Security 过滤链，造成假通过；已把 springSecurityFilterChain 显式加入 MockMvc，准备重新执行 RED。

- 2026-08-26：修正测试文件时 PowerShell 换行替换写入了字面量 
，已整体重写新增测试文件为正确 Java 源码；未触及生产代码。

- 2026-08-26：GREEN 实现：SecurityConfig 启用 http.cors(Customizer.withDefaults())，并在授权规则最前面 permitAll 全部 OPTIONS。一次 PowerShell 换行替换写错后已立即完整重写该文件，未保留字面 r/n。
