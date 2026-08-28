# 登录后内容不可用排查

## 目标
确认“登录成功但其他内容显示不可用”的根因，覆盖前端 API 地址、登录 Token、接口返回与后端安全配置。

## 阶段
- [进行中] 确认运行中的服务与实际 HTTP 返回
- [待开始] 对照前后端请求、认证与权限实现
- [待开始] 复现并验证根因
- [待开始] 给出最小操作或修复建议

## 约束
- 先收集证据，不修改业务代码。

---

# 订单与评价测试数据

## 目标
新增可重复执行的 SQL 脚本，为现有书店开发库生成订单、订单明细和用户评价测试数据。

## 阶段
- [已完成] 核对订单、明细、评价和图书的表结构及约束
- [已完成] 生成带固定标识的独立 SQL 测试数据脚本
- [进行中] 静态检查 SQL 的语法、金额与关联完整性
- [待开始] 交付导入方法与预期验证结果

## 约束
- 不执行脚本、不直接向数据库写入数据。
- 只使用 `sql/02_data.sql` 已建立的客户与图书。

---

# Flyway + Empty-Database Validation

## Goal
Adopt Flyway as the only schema migration mechanism, fix the missing `book.sales_count` schema column, and test migrations against an empty MySQL Testcontainer with Hibernate `validate` enabled.

## Phases
- [completed] 1. Commit pre-existing working-tree changes without user-upload runtime data.
- [completed] 2. Add a failing blank-database migration validation test.
- [completed] 3. Add Flyway baseline migration and application configuration.
- [completed] 4. Verify targeted tests, support an existing schema without history, and commit the implementation.

## Constraints
- Keep Hibernate at `ddl-auto=validate`.
- Do not use Hibernate DDL generation to mask schema drift.
- Keep `uploads/avatars/22/` untracked.
## Errors Encountered
| Error | Attempt | Resolution |
|---|---:|---|
| Full Maven test could not start two Spring contexts because the non-empty local `bookstore` schema has no Flyway history table. | 1 | Baseline existing schemas at V1 and introduce an idempotent V2 migration for `book.sales_count`. |
| Full Maven suite retains one unrelated environment configuration failure: `DemoApplicationTests` supplies no `DB_URL`, so Spring receives the literal `${DB_URL}` as a JDBC URL. | 2 | Logged as pre-existing test configuration debt; migration-specific tests pass and the live legacy schema was baselined then migrated successfully. |
