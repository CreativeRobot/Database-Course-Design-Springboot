# 网上书店数据库课程设计答辩材料

> **适用项目**：Spring Boot + JPA + MySQL 8 + Flyway 后端，Flutter 客户端。  
> **使用方式**：按“演示脚本”操作系统；答辩时用“可讲的结论”解释设计，用“代码与迁移定位”回答追问。所有表述均以当前实现为准，不把模拟支付或未实现能力表述为生产能力。

---

## 1. 项目概述：30 秒开场

本项目是一个支持普通用户和管理员的网上书店。普通用户可以浏览图书、维护购物车和地址、创建订单、模拟支付、确认收货、评价，并对已支付订单项申请仅退款或退货退款；管理员可以管理用户、图书、分类、评价、库存流水和售后审核。

数据库设计的重点不只是完成增删改查，而是保证交易数据正确：

1. 图书与作者、分类的多对多关系被规范化；
2. 订单保存地址、书名、ISBN、成交价快照，保证历史不被主数据修改；
3. 金额、库存、默认地址、退款数量和金额都有数据库约束；
4. 下单、取消、退款审核等跨表动作放在事务中，并用行锁或条件更新处理并发；
5. 表结构由 Flyway 迁移管理，Hibernate 仅做映射校验。

**一句话总结：**这是一个以订单、库存流水和售后事务为核心，兼顾数据规范化、完整性约束、并发一致性和可演进性的书店数据库课设。

## 2. 系统角色与核心用例

| 角色 | 主要用例 | 涉及的核心表 |
|---|---|---|
| 普通用户 | 注册登录、维护地址、浏览图书、购物车、下单、支付、收货、评价、申请售后 | `users`、`user_address`、`cart_item`、`book_order`、`order_item`、`payment`、`book_review`、`refund_request` |
| 管理员 | 图书/分类/用户/评价管理、库存流水审计、审核退款/退货退款、查看统计 | `book`、`category`、`inventory_log`、`refund_request` 及交易表 |
| 定时任务 | 取消超时未支付订单并回补库存 | `book_order`、`order_item`、`inventory_log`、`book` |

订单状态：`PENDING_PAYMENT -> PENDING_SHIPMENT -> SHIPPED -> COMPLETED`；未支付订单可进入 `CANCELLED`。支付接口是**模拟支付**，用于验证订单和支付流水事务，不对接真实第三方支付网关。

## 3. ER 图讲解（答辩时 1–2 分钟）

ER 图和完整约束说明见 [`bookstore-er-diagram.md`](./bookstore-er-diagram.md)。建议从四个对象群讲起：

1. **主数据：**`book` 归属一个 `publisher`；图书—作者、图书—分类通过 `book_author`、`book_category` 拆成多对多；`category.parent_id` 支持分类树。
2. **交易：**`book_order` 是订单头，`order_item` 是订单行，一张订单至少有一条明细；`payment` 是支付流水，按当前模型一张订单可保留多条支付记录。
3. **审计：**`inventory_log` 记录每次库存变动的前后库存、变动量、类型及可选订单；`book_review` 使用 `order_item_id` 唯一键限制“一条订单明细最多评价一次”。
4. **售后：**`refund_request` 同时关联订单、订单项、申请用户和可空审核管理员，实现部分数量的仅退款或退货退款。

## 4. 规范化、快照和冗余：为什么这样设计

### 4.1 规范化处理

- 出版社、作者、分类均独立建表，图书只保存其出版社外键；作者/分类不塞进图书的逗号分隔字段。
- `book_author(book_id, author_id)` 与 `book_category(book_id, category_id)` 使用复合主键，天然去重并表达多对多。
- 用户地址从用户表拆出，支持多地址；购物车独立为 `cart_item`，并用 `(user_id, book_id)` 唯一键防止同一图书重复成多行。

### 4.2 必须反规范化的历史快照

订单创建时复制收货人、手机号、完整地址；订单项复制书名、ISBN、成交单价。原因是这些字段描述成交时的业务事实：用户后来修改地址、管理员改图书名或调价，都不能篡改已经完成的订单历史。

### 4.3 可控冗余

- `book.stock` 保存当前可售库存，保证高频读取效率；`inventory_log` 作为可追溯账本。二者不是重复冲突，而是“当前状态 + 历史事件”。
- `book.sales_count` 是展示/排序使用的统计冗余，迁移 V2 保证其非负；在订单确认收货流程中按明细原子累加。它不代替订单明细这个原始交易事实来源。
- `book_order.refunded_amount` 与 `order_item.refunded_quantity` 是已批准售后的汇总值，用于快速判断累计售后边界，同时由 CHECK 约束和审核事务共同保证不超额。

## 5. 完整性约束：数据库层如何防错

| 类别 | 实现 | 可以回答的问题 |
|---|---|---|
| 主键与唯一 | 用户名、ISBN、订单号、支付单号、退款单号唯一；购物车用户+图书唯一；订单项评价唯一 | “怎样避免重复数据？” |
| 外键 | 订单到用户、订单项到订单/图书、退款到订单/订单项/用户/审核人、流水到图书/订单 | “如何防孤儿记录？” |
| 值域 CHECK | 售价/库存非负且售价不超过原价、页数正数、评分 1–5、购物车数量 1–999 | “只靠前端校验够吗？”——不够，数据库是最后防线。 |
| 金额公式 CHECK | `payable_amount = total_amount - discount_amount + shipping_fee`；`subtotal = unit_price * quantity` | “金额会不会算错？”——非法行无法写入。 |
| 库存流水 CHECK | `after_stock = before_stock + change_quantity`、数量不可为 0，且按类型校验正负方向和订单是否存在 | “如何证明流水可信？” |
| 默认地址唯一 | 生成列 `default_user_id` + UNIQUE | “MySQL 没有部分唯一索引，如何做到每用户至多一个默认地址？” |
| 退款边界 | `refunded_amount <= payable_amount`、`refunded_quantity <= quantity`、退款申请的数量和金额均为正数 | “如何防止超额退款？” |

### 默认地址的实现要点

MySQL 普通唯一索引允许多个 `NULL`。因此新增生成列：默认地址为真时取 `user_id`，否则取 `NULL`；对生成列建唯一键。这样每个用户最多只有一个非空 `default_user_id`，同时允许多个非默认地址。服务层在改地址前锁定用户行，数据库唯一约束作为最终兜底。

## 6. 事务与并发一致性（重点）

### 6.1 创建订单与扣库存

`OrderService#createOrder` 使用 `@Transactional`，并按 `bookId` 固定排序来降低多图书锁顺序不一致造成的死锁风险。流程为：

1. 锁定/读取图书库存快照；
2. 用带条件的原子更新执行 `stock = stock - quantity`，条件要求图书在售且 `stock >= quantity`；
3. 同一事务写订单头、订单项快照、`ORDER_OUT` 库存流水；
4. 只清理本次结算读取到的购物车项；
5. 任一环节失败则整体回滚。

这避免了“先查库存再普通保存”在并发抢购时发生超卖。

### 6.2 取消与超时取消

取消订单以状态条件更新为入口，只有待支付订单真正改为取消后才回补库存并写 `ORDER_CANCEL_RETURN` 流水。定时任务 `OrderExpirationScheduler` 每隔 `app.order.expiration-scan-ms`（默认 `60000 ms`）扫描到期的待支付订单并走同一取消逻辑，因此重复执行不会重复回补库存。

### 6.3 退款/退货退款审核

用户创建申请时，服务端锁订单项并计算“原购买数量 − 已批准退款数量 − 待审核/已批准申请数量”，退款金额由服务端按 `unit_price × quantity` 计算，前端不能直接指定金额。

管理员审核时锁定退款申请、订单、订单项；拒绝只改变申请状态。批准退货退款时还会锁图书库存，增加库存并写入 `REFUND_RETURN` 流水，再同时更新订单项已退款数量、订单累计已退款金额和申请状态。重复审核因只允许 `PENDING` 申请进入审核而被拒绝，避免重复加库存。

> 当前退款实现以内部售后状态与库存/金额一致性为核心；订单全部应付金额退完时，最近一条成功支付记录被标为 `REFUNDED`。它不是对真实第三方支付渠道发起资金退款。

## 7. 数据库演进：Flyway 与 Hibernate validate

配置在 `src/main/resources/application.properties`：

```properties
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
spring.flyway.baseline-version=1
spring.jpa.hibernate.ddl-auto=validate
```

- **Flyway 是唯一的 schema writer。**空库按 V1 到 V5 依次建表/演进；已有但未纳入 Flyway 历史的旧库可从 baseline 版本接入。
- **Hibernate `validate` 不是建表。**应用启动时只校验 JPA 实体映射与已存在的表结构是否匹配，发现字段、类型或约束相关映射漂移及时失败。
- V2 对 `sales_count` 先检查 `information_schema`，兼顾空库与已有该列的历史库；V3 清理旧数据中重复默认地址后再加唯一约束，减少迁移失败风险。
- `DatabaseMigrationValidationTests` 在 Docker 可用时通过 MySQL Testcontainers 实测迁移和 Hibernate 校验；`FlywayMigrationResourceTests` 则提供不依赖 Docker 的快速脚本资源守卫。无 Docker 的机器可以运行后者及其它不依赖容器的测试，并在本机 MySQL 空库执行 Flyway 验证。

## 8. 查询性能与 EXPLAIN ANALYZE

后台图书列表常见形态是按状态过滤、按最新 ID 分页：

```sql
SELECT ...
FROM book
WHERE status = 'ON_SALE'
ORDER BY id DESC
LIMIT 20;
```

迁移 V5 增加 `idx_book_status_id(status, id)`，使索引前缀对应等值筛选列，随后利用 `id` 的有序性服务排序/分页，降低全表扫描和额外排序的风险。项目保留了可复现实验脚本：

- `sql/performance/explain-book-search-before.sql`
- `sql/performance/explain-book-search-after.sql`
- `docs/database/query-performance-baseline.md`

答辩时不要只说“有索引所以快”。应在**同一 MySQL 版本、同一数据量、同一参数**下运行 `EXPLAIN ANALYZE`，对比扫描行数、实际耗时、是否出现 filesort/全表扫描；不同数据分布的结果可能不同。

## 9. 测试与可验证证据

| 测试/位置 | 覆盖重点 |
|---|---|
| `DatabaseMigrationValidationTests` | Docker 可用时：Flyway 空库迁移、Hibernate validate、V2/V3 数据库结果。 |
| `FlywayMigrationResourceTests` | 不依赖 Docker：迁移脚本和关键 Flyway 配置存在性守卫。 |
| `BookRepositoryConcurrencyTests` | 并发库存扣减只有一个成功者、库存流水前后状态精确。 |
| `UserAddressServiceTests` | 修改默认地址前锁用户，避免同一用户并发写默认地址。 |
| `RefundServiceTests` | 服务端计算退款金额、退货退款原子回补库存、禁止重复审核、订单归属控制。 |
| `ReviewServiceTests` | 购买关联评价和每订单项一次评价规则。 |
| Flutter 退款相关测试 | 退款模型、仓库契约、导航、申请页面的用户端流程。 |

建议现场执行（环境具备 Maven 与数据库配置时）：

```powershell
cd "D:\no game\Code\DatabaseHomework\demo"
.\mvnw.cmd test
```

若机器没有 Docker，不需要为了普通单元测试安装 Docker；Docker 仅影响使用 Testcontainers 的真实 MySQL 迁移/并发集成测试是否运行。也可建立一个本机 MySQL 空数据库，配置 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD` 后启动后端，以验证 V1–V5 实际迁移。

## 10. 推荐现场演示脚本（约 6–8 分钟）

1. **图书与地址：**以普通用户登录，查看图书详情、添加购物车；新增两条地址并切换默认地址，说明“同一用户至多一条默认地址”。
2. **创建订单：**选择地址创建订单；展示订单中地址、书名和成交单价快照。
3. **支付与库存：**执行模拟支付；从管理员库存流水页查看对应 `ORDER_OUT`，指出其中的变更前库存、变更量、变更后库存。
4. **取消或超时：**对另一个待支付订单取消，展示库存回补和 `ORDER_CANCEL_RETURN` 流水。
5. **售后：**在已支付订单明细点“申请售后”，选择仅退款或退货退款、填写数量和原因；管理员审核退货退款后查看 `REFUND_RETURN` 流水、订单项退款数量和订单退款金额。
6. **评价：**在已完成订单项评价一次，再尝试同一明细重复评价，说明唯一约束和服务层校验。
7. **后台性能：**展示 V5 索引和 `EXPLAIN ANALYZE` 脚本，说明优化目标和对比方法。

## 11. 常见答辩追问与参考回答

### Q1：为什么订单不直接引用 `user_address.id`？
**答：**订单要保留成交时收货信息。若只引用地址，用户后来修改或删除地址会让历史订单显示错误。因此订单保存地址快照，地址表仍用于以后下单时选择。

### Q2：为什么订单项还要存书名、ISBN、单价，图书表已经有了？
**答：**图书的名称和售价会变化，而订单需要反映成交时的事实。订单项保存快照避免历史订单随主数据变化；`subtotal = unit_price × quantity` 也被数据库 CHECK 约束校验。

### Q3：仅靠 `stock >= 0` 的 CHECK 能防超卖吗？
**答：**不能。它只能阻止最终库存为负；并发下两个事务都读到库存充足仍可能竞争。项目同时使用锁和带 `stock >= quantity` 条件的原子扣减，并检查受影响行数，失败则订单事务回滚。

### Q4：为什么默认地址不用应用层先查询再判断？
**答：**先查再写在并发下会竞态。实现上先锁用户行串行化写操作，同时在数据库以生成列唯一键兜底，任何绕过服务层的并发写也不能留下两条默认地址。

### Q5：退款金额为什么不由前端传？
**答：**前端数据不可信。服务端用订单项成交单价乘申请数量计算，并锁定订单项；审核时再次校验累计退款数量和金额不超原订单。

### Q6：退款后为什么有的场景回补库存、有的没有？
**答：**仅退款不回收商品，不增加库存；退货退款才意味着商品回库，批准时增加库存并记录 `REFUND_RETURN` 流水。

### Q7：Flyway 和 Hibernate 同时存在会不会重复建表？
**答：**不会。本项目 Flyway 是唯一建表/改表工具；Hibernate 设置为 `validate`，只检查实体和表是否匹配，不能执行 DDL。

### Q8：为什么需要库存流水，直接改 `book.stock` 不行吗？
**答：**只存当前库存无法解释变化来源。库存流水保留每次变更的前后值、数量、类型、订单和时间，并有前后库存公式与业务类型 CHECK，便于审计和排错。

### Q9：联合索引 `(status, id)` 为什么不只建 `status`？
**答：**查询不仅按状态筛选，还按 `id DESC` 排序分页。单列 `status` 可能仍需要额外排序；联合索引把筛选和顺序访问的关键列放在一起，更匹配该访问路径。实际效果必须用 `EXPLAIN ANALYZE` 验证。

### Q10：项目还存在哪些局限？
**答：**支付与退款未接真实第三方网关；订单创建/支付尚未引入显式幂等键；退款支付状态是内部模拟；复杂组合检索可使用 DTO 投影、批量查询或 `EntityGraph` 降低 N+1 风险；生产环境还需完善 JWT 密钥管理、限流、日志和监控。

## 12. 代码与文档定位速查

| 主题 | 位置 |
|---|---|
| 建表与数据库演进 | `src/main/resources/db/migration/V1__create_initial_schema.sql` 至 `V5__add_book_admin_list_index.sql` |
| Flyway / Hibernate 配置 | `src/main/resources/application.properties` |
| 下单、取消、支付 | `src/main/java/com/example/demo/service/OrderService.java` |
| 库存回补与流水 | `src/main/java/com/example/demo/service/InventoryService.java` |
| 退款/退货退款 | `src/main/java/com/example/demo/service/RefundService.java`、`controller/RefundController.java` |
| 默认地址并发控制 | `src/main/java/com/example/demo/service/UserAddressService.java` |
| 超时取消任务 | `src/main/java/com/example/demo/service/OrderExpirationScheduler.java` |
| 用户端退款前端 | `D:\no game\Code\DatabaseHomework\BookStore_Flutter\flutter_application_bookstore\lib\features\refunds\` |
| ER 图 | `docs/bookstore-er-diagram.md`、`docs/bookstore-er-diagram.mmd` |
| 性能对比脚本 | `sql/performance/explain-book-search-before.sql`、`sql/performance/explain-book-search-after.sql` |