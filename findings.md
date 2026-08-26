# Findings

- 2026-08-26：后端 `127.0.0.1:8080` 正在监听，`GET /api/categories` 与 `GET /api/books` 返回 HTTP 200 且有数据，说明公共图书内容和数据库可用。
- 同一后端上 `GET /api/recommendations/home`（不携带 Authorization）返回 401；该接口按设计要求要求登录后的 Bearer Token。
- `SecurityConfig` 仅将图书、分类、作者、出版社的 GET 请求公开；推荐、购物车、订单、资料等均须携带有效 JWT。
- Flutter `ApiClient` 会从 SharedPreferences 读取 `bookstore.jwt` 并为每个请求添加 `Authorization: Bearer <token>`；任何 401 都会清除本地 token 并触发会话过期。
- 根因已复现：浏览器发出携带 Authorization 的跨域请求前，都会先发送 OPTIONS 预检。`OPTIONS /api/auth/login` 返回 200 且带 CORS 响应头，但 `OPTIONS /api/books`、`/api/categories`、`/api/recommendations/home`、`/api/user/me` 和 `/api/cart` 都返回 401，且没有 `Access-Control-Allow-Origin`。
- `JwtAuthenticationFilter` 已跳过 OPTIONS，但 `SecurityConfig` 没有启用 Spring Security CORS（未调用 `http.cors(...)`），也没有放行 OPTIONS；因此后续仍被 `.anyRequest().authenticated()` 拦截。WebMvcConfig 的 CORS 映射无法在该安全拦截之前完成预检响应。
- 这解释了现象：登录接口路径被 permitAll，预检成功；登录后其它接口的预检被浏览器阻断，Flutter 将请求异常映射为“服务暂时不可用/无法加载”。即使 `/api/books` 本身是公开 GET，登录后拦截器添加 Authorization 也会触发预检，故同样失败。
