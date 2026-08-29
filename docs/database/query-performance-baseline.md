# 核心图书查询 EXPLAIN ANALYZE 前后对比

## 1. 场景

对应后端 `BookService.listAllBooks` 的常用管理端列表：只按图书状态筛选，按图书 `id` 倒序，取第 1 页 20 条。

```text
GET /api/admin/books?status=ON_SALE&page=1&size=20
```

选择这个场景的原因是它是管理端目录页的稳定基线，SQL 形状不会受可选的关键词、作者、分类参数影响，适合在数据库课设中展示索引设计和执行计划变化。

## 2. 优化内容

原始 `book` 表已有 `idx_book_title`、`idx_book_publisher`，但没有以 `status` 开头的索引。该列表的谓词和排序分别使用 `status`、`id`，因此新增 Flyway 迁移：

```sql
CREATE INDEX idx_book_status_id ON book (status, id);
```

迁移文件：`src/main/resources/db/migration/V5__add_book_admin_list_index.sql`

该索引同时覆盖状态过滤和 `id DESC` 的有序扫描（MySQL 可反向扫描 B-Tree），减少全表扫描和额外排序。它不会改变接口契约，也不会影响实体映射。

## 3. 如何执行可复现对比

要求 MySQL 8.0.18+，因为 `EXPLAIN ANALYZE` 在该版本开始可用。使用一份数据快照，或者在同一个数据库中按下列顺序操作：

### 优化前

在执行 V5 之前运行：

```powershell
mysql -h localhost -P 3306 -u <user> -p bookstore < sql/performance/explain-book-search-before.sql
```

对应脚本：`sql/performance/explain-book-search-before.sql`

### 应用优化

启动后端让 Flyway 自动执行，或手动执行 V5 迁移。确认索引已经存在：

```sql
SHOW INDEX FROM book WHERE Key_name = 'idx_book_status_id';
```

### 优化后

```powershell
mysql -h localhost -P 3306 -u <user> -p bookstore < sql/performance/explain-book-search-after.sql
```

对应脚本：`sql/performance/explain-book-search-after.sql`。脚本使用 `USE INDEX`，避免优化器在多个索引之间选择不同路径，便于课设答辩复现实验。

## 4. 记录结果

不要预先填写耗时或扫描行数；这些数字必须来自你的本机数据。把两次输出中的以下字段记录到答辩表格：

| 指标 | 优化前 | 优化后 |
| --- | ---: | ---: |
| 实际耗时（ms） | 运行后填写 | 运行后填写 |
| 扫描/读取行数 | 运行后填写 | 运行后填写 |
| 返回行数 | 运行后填写 | 运行后填写 |
| 访问类型 | 运行后填写 | 运行后填写 |
| 是否 filesort | 运行后填写 | 运行后填写 |

`EXPLAIN ANALYZE` 输出通常包含 `actual time`、`rows`、`loops` 等信息；不同数据量、MySQL 小版本和缓存状态会导致数值不同，因此仓库不伪造实验结果。

## 5. 关键词搜索的边界

管理员关键词搜索当前是 `lower(title) LIKE '%keyword%'`。前缀和后缀都有 `%` 时，普通 B-Tree 标题索引不能有效解决任意子串检索；不要把 `idx_book_title` 的存在误写成“关键词一定命中索引”。如果数据规模继续增长，应单独评估 MySQL FULLTEXT、搜索服务或调整为前缀搜索，并用另一组数据和执行计划验证。

## 6. 当前环境验证记录（2026-08-29）

本次已完成 SQL 脚本、Flyway V5 迁移和对比说明，但没有伪造本机 `EXPLAIN ANALYZE` 数字：当前工作环境没有可用的项目数据库凭据，且 Docker 不可用，无法启动隔离 MySQL 数据库。拿到数据库账号后按第 3 节执行即可补齐真实结果。
