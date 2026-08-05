# 网上书店系统 ER 图

本 ER 图根据 `src/main/java/com/example/demo/entity` 中的 JPA 实体类生成。

- `PK`：主键
- `FK`：外键
- `UK`：唯一键
- `BOOK_AUTHOR` 和 `BOOK_CATEGORY` 使用联合主键
- `CART_ITEM` 中 `(user_id, book_id)` 构成联合唯一约束
- `BOOK_REVIEW.order_item_id` 唯一，表示一条订单明细最多评价一次
- 订单地址、书名、ISBN 和成交单价属于历史快照字段

```mermaid
erDiagram
    USERS {
        BIGINT id PK "用户主键"
        VARCHAR username UK "登录用户名"
        VARCHAR password "加密密码"
        INT status "账户状态"
        DATETIME create_time "创建时间"
        DATETIME update_time "更新时间"
        VARCHAR role "ADMIN或CUSTOMER"
        VARCHAR nickname "昵称"
        VARCHAR email "邮箱"
        VARCHAR phone "手机号"
    }

    USER_ADDRESS {
        BIGINT id PK "地址主键"
        BIGINT user_id FK "所属用户"
        VARCHAR receiver_name "收货人"
        VARCHAR receiver_phone "收货手机号"
        VARCHAR province "省份"
        VARCHAR city "城市"
        VARCHAR district "区县"
        VARCHAR detail_address "详细地址"
        VARCHAR postal_code "邮政编码"
        BOOLEAN default_address "是否默认地址"
        DATETIME create_time "创建时间"
        DATETIME update_time "更新时间"
    }

    PUBLISHER {
        BIGINT id PK "出版社主键"
        VARCHAR name UK "出版社名称"
        VARCHAR phone "联系电话"
        VARCHAR address "出版社地址"
        VARCHAR introduction "出版社简介"
        DATETIME create_time "创建时间"
        DATETIME update_time "更新时间"
    }

    AUTHOR {
        BIGINT id PK "作者主键"
        VARCHAR name "作者姓名"
        VARCHAR country "国家或地区"
        VARCHAR introduction "作者简介"
        DATETIME create_time "创建时间"
        DATETIME update_time "更新时间"
    }

    CATEGORY {
        BIGINT id PK "分类主键"
        VARCHAR name UK "分类名称"
        BIGINT parent_id FK "父分类，可为空"
        INT sort_order "同级排序"
        INT status "分类状态"
        DATETIME create_time "创建时间"
        DATETIME update_time "更新时间"
    }

    BOOK {
        BIGINT id PK "图书主键"
        VARCHAR isbn UK "ISBN"
        VARCHAR title "书名"
        BIGINT publisher_id FK "出版社"
        DECIMAL original_price "原价"
        DECIMAL sale_price "售价"
        INT stock "当前库存"
        DATE publish_date "出版日期"
        VARCHAR edition "版本"
        INT pages "页数"
        TEXT description "图书简介"
        VARCHAR cover_url "封面地址"
        VARCHAR status "ON_SALE或OFF_SALE"
        DATETIME create_time "创建时间"
        DATETIME update_time "更新时间"
    }

    BOOK_AUTHOR {
        BIGINT book_id PK, FK "图书主键组成部分"
        BIGINT author_id PK, FK "作者主键组成部分"
        INT author_order "作者顺序"
    }

    BOOK_CATEGORY {
        BIGINT book_id PK, FK "图书主键组成部分"
        BIGINT category_id PK, FK "分类主键组成部分"
    }

    CART_ITEM {
        BIGINT id PK "购物车明细主键"
        BIGINT user_id FK "所属用户"
        BIGINT book_id FK "图书"
        INT quantity "购买数量"
        BOOLEAN selected "是否选中结算"
        DATETIME create_time "加入时间"
        DATETIME update_time "更新时间"
    }

    BOOK_ORDER {
        BIGINT id PK "订单主键"
        VARCHAR order_no UK "订单编号"
        BIGINT user_id FK "下单用户"
        VARCHAR status "订单状态"
        DECIMAL total_amount "商品总金额"
        DECIMAL discount_amount "优惠金额"
        DECIMAL shipping_fee "运费"
        DECIMAL payable_amount "应付金额"
        VARCHAR receiver_name "收货人快照"
        VARCHAR receiver_phone "手机号快照"
        VARCHAR receiver_address "收货地址快照"
        VARCHAR remark "订单备注"
        DATETIME create_time "下单时间"
        DATETIME update_time "更新时间"
        DATETIME paid_time "支付时间"
        DATETIME shipped_time "发货时间"
        DATETIME completed_time "完成时间"
        DATETIME cancelled_time "取消时间"
    }

    ORDER_ITEM {
        BIGINT id PK "订单明细主键"
        BIGINT order_id FK "所属订单"
        BIGINT book_id FK "关联图书"
        VARCHAR book_title "下单时书名快照"
        VARCHAR isbn "下单时ISBN快照"
        DECIMAL unit_price "下单时单价"
        INT quantity "购买数量"
        DECIMAL subtotal "明细小计"
    }

    PAYMENT {
        BIGINT id PK "支付记录主键"
        VARCHAR payment_no UK "支付流水号"
        BIGINT order_id FK "关联订单"
        VARCHAR payment_method "支付方式"
        DECIMAL amount "支付金额"
        VARCHAR status "支付状态"
        DATETIME paid_time "支付完成时间"
        DATETIME create_time "创建时间"
        DATETIME update_time "更新时间"
    }

    BOOK_REVIEW {
        BIGINT id PK "评价主键"
        BIGINT user_id FK "评价用户"
        BIGINT book_id FK "评价图书"
        BIGINT order_item_id FK, UK "对应订单明细"
        INT rating "评分1至5"
        VARCHAR content "评价内容"
        INT status "评价状态"
        DATETIME create_time "创建时间"
        DATETIME update_time "更新时间"
    }

    INVENTORY_LOG {
        BIGINT id PK "库存流水主键"
        BIGINT book_id FK "发生变动的图书"
        INT change_quantity "库存变化量"
        INT before_stock "变化前库存"
        INT after_stock "变化后库存"
        VARCHAR change_type "变动类型"
        BIGINT order_id FK "关联订单，可为空"
        VARCHAR remark "变动备注"
        DATETIME create_time "创建时间"
    }

    USERS ||--o{ USER_ADDRESS : "拥有"
    USERS ||--o{ CART_ITEM : "加入购物车"
    USERS ||--o{ BOOK_ORDER : "提交订单"
    USERS ||--o{ BOOK_REVIEW : "发表评价"

    PUBLISHER ||--o{ BOOK : "出版"
    CATEGORY o|--o{ CATEGORY : "父子分类"

    BOOK ||--o{ BOOK_AUTHOR : "关联作者"
    AUTHOR ||--o{ BOOK_AUTHOR : "参与创作"

    BOOK ||--o{ BOOK_CATEGORY : "关联分类"
    CATEGORY ||--o{ BOOK_CATEGORY : "包含图书"

    BOOK ||--o{ CART_ITEM : "加入购物车"
    BOOK_ORDER ||--|{ ORDER_ITEM : "包含"
    BOOK ||--o{ ORDER_ITEM : "形成订单明细"

    BOOK_ORDER ||--o{ PAYMENT : "产生支付记录"
    ORDER_ITEM ||--o| BOOK_REVIEW : "对应评价"
    BOOK ||--o{ BOOK_REVIEW : "获得评价"

    BOOK ||--o{ INVENTORY_LOG : "产生库存流水"
    BOOK_ORDER o|--o{ INVENTORY_LOG : "关联库存变动"
```

## 核心关系说明

1. 一个用户可以拥有多个收货地址、购物车明细、订单和评价。
2. 一个出版社可以出版多本图书。
3. 图书与作者通过 `BOOK_AUTHOR` 形成多对多关系。
4. 图书与分类通过 `BOOK_CATEGORY` 形成多对多关系。
5. 分类通过 `parent_id` 实现树形自关联。
6. 一个订单包含一条或多条订单明细，也可以产生多次支付记录。
7. 一条订单明细最多对应一条评价。
8. 图书库存的每次变化记录在 `INVENTORY_LOG` 中，库存流水可以关联订单。