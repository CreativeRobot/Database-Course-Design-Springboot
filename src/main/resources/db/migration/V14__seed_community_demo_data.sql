-- Community demo accounts all use password: 123456
INSERT INTO users (username, password, status, create_time, update_time, role, nickname, email, phone, avatar_url)
SELECT 'community_linan', '$2a$10$5jJDx2EJji2BXPUZ9LnI6O4jE7OJrpPr6s0S79nKNDSrsWOS3lGV2', 1,
       CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 'CUSTOMER', '林安', 'linan@example.com', NULL, NULL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'community_linan');

INSERT INTO users (username, password, status, create_time, update_time, role, nickname, email, phone, avatar_url)
SELECT 'community_muyu', '$2a$10$5jJDx2EJji2BXPUZ9LnI6O4jE7OJrpPr6s0S79nKNDSrsWOS3lGV2', 1,
       CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 'CUSTOMER', '木鱼同学', 'muyu@example.com', NULL, NULL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'community_muyu');

INSERT INTO users (username, password, status, create_time, update_time, role, nickname, email, phone, avatar_url)
SELECT 'community_xiaozhou', '$2a$10$5jJDx2EJji2BXPUZ9LnI6O4jE7OJrpPr6s0S79nKNDSrsWOS3lGV2', 1,
       CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 'CUSTOMER', '小舟读书', 'xiaozhou@example.com', NULL, NULL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'community_xiaozhou');

INSERT INTO users (username, password, status, create_time, update_time, role, nickname, email, phone, avatar_url)
SELECT 'community_ake', '$2a$10$5jJDx2EJji2BXPUZ9LnI6O4jE7OJrpPr6s0S79nKNDSrsWOS3lGV2', 1,
       CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 'CUSTOMER', '阿柯', 'ake@example.com', NULL, NULL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'community_ake');

SET @community_user_linan = (SELECT id FROM users WHERE username = 'community_linan' LIMIT 1);
SET @community_user_muyu = (SELECT id FROM users WHERE username = 'community_muyu' LIMIT 1);
SET @community_user_xiaozhou = (SELECT id FROM users WHERE username = 'community_xiaozhou' LIMIT 1);
SET @community_user_ake = (SELECT id FROM users WHERE username = 'community_ake' LIMIT 1);

SET @community_book_literature_1 = (SELECT id FROM book WHERE isbn = '9787300008001' LIMIT 1);
SET @community_book_literature_2 = (SELECT id FROM book WHERE isbn = '9787300008002' LIMIT 1);
SET @community_book_computer_1 = (SELECT id FROM book WHERE isbn = '9787300008003' LIMIT 1);
SET @community_book_computer_2 = (SELECT id FROM book WHERE isbn = '9787300008004' LIMIT 1);
SET @community_book_education = (SELECT id FROM book WHERE isbn = '9787300008005' LIMIT 1);
SET @community_book_economics = (SELECT id FROM book WHERE isbn = '9787300008006' LIMIT 1);
SET @community_book_life = (SELECT id FROM book WHERE isbn = '9787300008007' LIMIT 1);
SET @community_book_children = (SELECT id FROM book WHERE isbn = '9787300008008' LIMIT 1);

INSERT INTO community_post (user_id, title, content, status, create_time, update_time) VALUES
(@community_user_linan, '【读书交流】读完《远方来信》，最打动你的是哪一封？',
 '周末读完了《远方来信》。我很喜欢书里用信件串起人物成长的方式，尤其是关于离开故乡后重新理解家人的部分。大家最喜欢哪一封信？也欢迎分享你读到的细节。',
 1, CURRENT_TIMESTAMP(6) - INTERVAL 2 HOUR, CURRENT_TIMESTAMP(6) - INTERVAL 2 HOUR),
(@community_user_muyu, '【读书交流】《月光书店》无剧透短评：适合睡前慢慢读',
 '这本书的节奏不快，但书店里每位客人的故事都很温暖。前半段像日常随笔，后半段人物关系慢慢连起来。个人推荐每天睡前读一两章，不建议一次赶完。',
 1, CURRENT_TIMESTAMP(6) - INTERVAL 6 HOUR, CURRENT_TIMESTAMP(6) - INTERVAL 6 HOUR),
(@community_user_xiaozhou, '【读书交流】零基础学编程，这两本书应该先看哪一本？',
 '最近准备系统补一下编程和数据库基础。我目前只写过一点简单代码，想先建立完整知识框架，再做课程项目。大家建议先学编程基础，还是边做数据库项目边学？',
 1, CURRENT_TIMESTAMP(6) - INTERVAL 12 HOUR, CURRENT_TIMESTAMP(6) - INTERVAL 12 HOUR),
(@community_user_ake, '【读书交流】数据库实践指南第 4 章笔记：索引不是越多越好',
 '今天整理了索引章节：索引能加快查询，但也会增加写入成本和存储占用。给高频筛选、排序、关联字段建索引更合理。准备拿社区帖子列表的时间排序做一次 EXPLAIN 实验。',
 1, CURRENT_TIMESTAMP(6) - INTERVAL 1 DAY, CURRENT_TIMESTAMP(6) - INTERVAL 1 DAY),
(@community_user_linan, '【读书交流】考试复习总是看了就忘，有没有更有效的方法？',
 '以前复习主要靠反复看书，短期觉得熟悉，真正做题时却想不起来。最近尝试读完一节就合上书复述，再用错题检验。大家还有哪些亲测有效的主动回忆方法？',
 1, CURRENT_TIMESTAMP(6) - INTERVAL 1 DAY - INTERVAL 5 HOUR, CURRENT_TIMESTAMP(6) - INTERVAL 1 DAY - INTERVAL 5 HOUR),
(@community_user_muyu, '【读书交流】经济学入门读书笔记：机会成本比价格更重要',
 '读到机会成本这一节，最大的收获是做选择时不能只看付出了多少钱，还要看放弃了什么。比如买书的成本不只是书价，也包括阅读时间。这个视角很适合分析日常选择。',
 1, CURRENT_TIMESTAMP(6) - INTERVAL 2 DAY, CURRENT_TIMESTAMP(6) - INTERVAL 2 DAY),
(@community_user_xiaozhou, '【读书交流】求推荐：有没有适合新手照着做的家常菜书？',
 '刚开始自己做饭，希望菜谱步骤清楚、材料容易买到，而且能解释火候和调味逻辑。复杂摆盘不重要，最好一周能学会几道稳定的家常菜。',
 1, CURRENT_TIMESTAMP(6) - INTERVAL 2 DAY - INTERVAL 7 HOUR, CURRENT_TIMESTAMP(6) - INTERVAL 2 DAY - INTERVAL 7 HOUR),
(@community_user_ake, '【读书交流】亲子阅读时，孩子总想跳页怎么办？',
 '陪孩子读绘本时，他经常只看图、跳过文字，还会反复翻回喜欢的页面。我原来担心这样没有读完整，后来觉得主动观察也很重要。大家会坚持按顺序讲完吗？',
 1, CURRENT_TIMESTAMP(6) - INTERVAL 3 DAY, CURRENT_TIMESTAMP(6) - INTERVAL 3 DAY),
(@community_user_linan, '【读书交流】两本文学书一起读，意外发现了相似的主题',
 '《远方来信》和《月光书店》的写法不同，但都在讨论人与地方的关系：一个通过离开后的书信回望故乡，一个通过留在书店的人观察来往。很适合连着读再做对照笔记。',
 1, CURRENT_TIMESTAMP(6) - INTERVAL 4 DAY, CURRENT_TIMESTAMP(6) - INTERVAL 4 DAY),
(@community_user_muyu, '【读书交流】新学期阅读计划：专业书和轻阅读怎么平衡？',
 '我给自己定的计划是工作日读专业书，每次 30 分钟；周末读文学或生活类图书。专业书做卡片笔记，轻阅读只记一句当天最喜欢的话。先执行一个月再来复盘。',
 1, CURRENT_TIMESTAMP(6) - INTERVAL 5 DAY, CURRENT_TIMESTAMP(6) - INTERVAL 5 DAY);

SET @community_post_1 = (SELECT id FROM community_post WHERE title = '【读书交流】读完《远方来信》，最打动你的是哪一封？' ORDER BY id DESC LIMIT 1);
SET @community_post_2 = (SELECT id FROM community_post WHERE title = '【读书交流】《月光书店》无剧透短评：适合睡前慢慢读' ORDER BY id DESC LIMIT 1);
SET @community_post_3 = (SELECT id FROM community_post WHERE title = '【读书交流】零基础学编程，这两本书应该先看哪一本？' ORDER BY id DESC LIMIT 1);
SET @community_post_4 = (SELECT id FROM community_post WHERE title = '【读书交流】数据库实践指南第 4 章笔记：索引不是越多越好' ORDER BY id DESC LIMIT 1);
SET @community_post_5 = (SELECT id FROM community_post WHERE title = '【读书交流】考试复习总是看了就忘，有没有更有效的方法？' ORDER BY id DESC LIMIT 1);
SET @community_post_6 = (SELECT id FROM community_post WHERE title = '【读书交流】经济学入门读书笔记：机会成本比价格更重要' ORDER BY id DESC LIMIT 1);
SET @community_post_7 = (SELECT id FROM community_post WHERE title = '【读书交流】求推荐：有没有适合新手照着做的家常菜书？' ORDER BY id DESC LIMIT 1);
SET @community_post_8 = (SELECT id FROM community_post WHERE title = '【读书交流】亲子阅读时，孩子总想跳页怎么办？' ORDER BY id DESC LIMIT 1);
SET @community_post_9 = (SELECT id FROM community_post WHERE title = '【读书交流】两本文学书一起读，意外发现了相似的主题' ORDER BY id DESC LIMIT 1);
SET @community_post_10 = (SELECT id FROM community_post WHERE title = '【读书交流】新学期阅读计划：专业书和轻阅读怎么平衡？' ORDER BY id DESC LIMIT 1);

INSERT INTO community_post_book (post_id, book_id) VALUES
(@community_post_1, @community_book_literature_1),
(@community_post_2, @community_book_literature_2),
(@community_post_3, @community_book_computer_1),
(@community_post_3, @community_book_computer_2),
(@community_post_4, @community_book_computer_2),
(@community_post_5, @community_book_education),
(@community_post_6, @community_book_economics),
(@community_post_7, @community_book_life),
(@community_post_8, @community_book_children),
(@community_post_9, @community_book_literature_1),
(@community_post_9, @community_book_literature_2),
(@community_post_10, @community_book_computer_1),
(@community_post_10, @community_book_education),
(@community_post_10, @community_book_life);

INSERT INTO community_post_image (post_id, image_url, sort_order, create_time)
SELECT @community_post_1, cover_url, 0, CURRENT_TIMESTAMP(6) - INTERVAL 2 HOUR
FROM book WHERE id = @community_book_literature_1 AND cover_url IS NOT NULL;
INSERT INTO community_post_image (post_id, image_url, sort_order, create_time)
SELECT @community_post_2, cover_url, 0, CURRENT_TIMESTAMP(6) - INTERVAL 6 HOUR
FROM book WHERE id = @community_book_literature_2 AND cover_url IS NOT NULL;
INSERT INTO community_post_image (post_id, image_url, sort_order, create_time)
SELECT @community_post_3, cover_url, 0, CURRENT_TIMESTAMP(6) - INTERVAL 12 HOUR
FROM book WHERE id = @community_book_computer_1 AND cover_url IS NOT NULL;
INSERT INTO community_post_image (post_id, image_url, sort_order, create_time)
SELECT @community_post_3, cover_url, 1, CURRENT_TIMESTAMP(6) - INTERVAL 12 HOUR
FROM book WHERE id = @community_book_computer_2 AND cover_url IS NOT NULL;
INSERT INTO community_post_image (post_id, image_url, sort_order, create_time)
SELECT @community_post_6, cover_url, 0, CURRENT_TIMESTAMP(6) - INTERVAL 2 DAY
FROM book WHERE id = @community_book_economics AND cover_url IS NOT NULL;
INSERT INTO community_post_image (post_id, image_url, sort_order, create_time)
SELECT @community_post_7, cover_url, 0, CURRENT_TIMESTAMP(6) - INTERVAL 2 DAY - INTERVAL 7 HOUR
FROM book WHERE id = @community_book_life AND cover_url IS NOT NULL;
INSERT INTO community_post_image (post_id, image_url, sort_order, create_time)
SELECT @community_post_8, cover_url, 0, CURRENT_TIMESTAMP(6) - INTERVAL 3 DAY
FROM book WHERE id = @community_book_children AND cover_url IS NOT NULL;
INSERT INTO community_post_image (post_id, image_url, sort_order, create_time)
SELECT @community_post_9, cover_url, 0, CURRENT_TIMESTAMP(6) - INTERVAL 4 DAY
FROM book WHERE id = @community_book_literature_1 AND cover_url IS NOT NULL;
INSERT INTO community_post_image (post_id, image_url, sort_order, create_time)
SELECT @community_post_9, cover_url, 1, CURRENT_TIMESTAMP(6) - INTERVAL 4 DAY
FROM book WHERE id = @community_book_literature_2 AND cover_url IS NOT NULL;

INSERT INTO community_comment (post_id, user_id, parent_id, content, status, create_time, update_time) VALUES
(@community_post_1, @community_user_muyu, NULL, '我最喜欢写给父亲的那一封，很多情绪没有直接说出来，但读完会回想很久。', 1, CURRENT_TIMESTAMP(6) - INTERVAL 95 MINUTE, CURRENT_TIMESTAMP(6) - INTERVAL 95 MINUTE),
(@community_post_1, @community_user_ake, NULL, '信件体很适合慢读，我会在每封信后面写一句自己的回应。', 1, CURRENT_TIMESTAMP(6) - INTERVAL 70 MINUTE, CURRENT_TIMESTAMP(6) - INTERVAL 70 MINUTE),
(@community_post_2, @community_user_linan, NULL, '同感，连续读完反而会错过很多小细节。我最喜欢雨夜来书店的那位客人。', 1, CURRENT_TIMESTAMP(6) - INTERVAL 5 HOUR, CURRENT_TIMESTAMP(6) - INTERVAL 5 HOUR),
(@community_post_2, @community_user_xiaozhou, NULL, '请问故事会不会很伤感？最近想找一本比较治愈的书。', 1, CURRENT_TIMESTAMP(6) - INTERVAL 4 HOUR, CURRENT_TIMESTAMP(6) - INTERVAL 4 HOUR),
(@community_post_3, @community_user_ake, NULL, '建议先把编程基础的前几章过一遍，然后马上用数据库小项目练习，不必整本读完才动手。', 1, CURRENT_TIMESTAMP(6) - INTERVAL 10 HOUR, CURRENT_TIMESTAMP(6) - INTERVAL 10 HOUR),
(@community_post_3, @community_user_linan, NULL, '我当时是两本交替读：上午看概念，晚上把当天内容写进项目，记得更牢。', 1, CURRENT_TIMESTAMP(6) - INTERVAL 9 HOUR, CURRENT_TIMESTAMP(6) - INTERVAL 9 HOUR),
(@community_post_4, @community_user_muyu, NULL, '可以顺便比较单列索引和联合索引，看看最左前缀对查询计划有什么影响。', 1, CURRENT_TIMESTAMP(6) - INTERVAL 20 HOUR, CURRENT_TIMESTAMP(6) - INTERVAL 20 HOUR),
(@community_post_4, @community_user_xiaozhou, NULL, '社区列表用 create_time 和 id 联合排序，正好是很直观的例子。', 1, CURRENT_TIMESTAMP(6) - INTERVAL 18 HOUR, CURRENT_TIMESTAMP(6) - INTERVAL 18 HOUR),
(@community_post_5, @community_user_ake, NULL, '我会把每章标题改写成问题，第二天只看问题回答，答不出来再翻书。', 1, CURRENT_TIMESTAMP(6) - INTERVAL 27 HOUR, CURRENT_TIMESTAMP(6) - INTERVAL 27 HOUR),
(@community_post_5, @community_user_muyu, NULL, '间隔复习很有用，第一次复习不要拖太久，最好当天晚上快速回忆一次。', 1, CURRENT_TIMESTAMP(6) - INTERVAL 25 HOUR, CURRENT_TIMESTAMP(6) - INTERVAL 25 HOUR),
(@community_post_6, @community_user_xiaozhou, NULL, '这个角度也能解释为什么有些便宜书买回来一直没读，真正稀缺的是注意力。', 1, CURRENT_TIMESTAMP(6) - INTERVAL 44 HOUR, CURRENT_TIMESTAMP(6) - INTERVAL 44 HOUR),
(@community_post_6, @community_user_ake, NULL, '我读这一章时拿选课做了例子，比只背定义容易理解。', 1, CURRENT_TIMESTAMP(6) - INTERVAL 42 HOUR, CURRENT_TIMESTAMP(6) - INTERVAL 42 HOUR),
(@community_post_7, @community_user_linan, NULL, '这本演示生活类图书步骤比较短，新手可以先试番茄炒蛋和炖菜。', 1, CURRENT_TIMESTAMP(6) - INTERVAL 50 HOUR, CURRENT_TIMESTAMP(6) - INTERVAL 50 HOUR),
(@community_post_7, @community_user_ake, NULL, '建议优先找标明食材重量的版本，只写少许和适量对新手不太友好。', 1, CURRENT_TIMESTAMP(6) - INTERVAL 48 HOUR, CURRENT_TIMESTAMP(6) - INTERVAL 48 HOUR),
(@community_post_8, @community_user_muyu, NULL, '不用强求顺序，孩子愿意描述画面已经是在阅读。可以顺着他的问题展开。', 1, CURRENT_TIMESTAMP(6) - INTERVAL 68 HOUR, CURRENT_TIMESTAMP(6) - INTERVAL 68 HOUR),
(@community_post_8, @community_user_linan, NULL, '我们家会先自由翻一遍，第二遍再尝试完整讲故事，接受度更高。', 1, CURRENT_TIMESTAMP(6) - INTERVAL 66 HOUR, CURRENT_TIMESTAMP(6) - INTERVAL 66 HOUR),
(@community_post_9, @community_user_xiaozhou, NULL, '这个对照很有意思，我觉得两本书里的空间都像一个保存记忆的容器。', 1, CURRENT_TIMESTAMP(6) - INTERVAL 90 HOUR, CURRENT_TIMESTAMP(6) - INTERVAL 90 HOUR),
(@community_post_9, @community_user_muyu, NULL, '准备按你的顺序重读，读完也想做一张人物和地点关系图。', 1, CURRENT_TIMESTAMP(6) - INTERVAL 88 HOUR, CURRENT_TIMESTAMP(6) - INTERVAL 88 HOUR),
(@community_post_10, @community_user_ake, NULL, '计划很实际。专业书每天半小时比周末突击三个小时更容易坚持。', 1, CURRENT_TIMESTAMP(6) - INTERVAL 112 HOUR, CURRENT_TIMESTAMP(6) - INTERVAL 112 HOUR),
(@community_post_10, @community_user_xiaozhou, NULL, '我会再留一天完全不安排阅读，避免计划太满之后产生挫败感。', 1, CURRENT_TIMESTAMP(6) - INTERVAL 110 HOUR, CURRENT_TIMESTAMP(6) - INTERVAL 110 HOUR);

SET @community_parent_2 = (SELECT id FROM community_comment WHERE post_id = @community_post_2 AND content = '请问故事会不会很伤感？最近想找一本比较治愈的书。' ORDER BY id DESC LIMIT 1);
SET @community_parent_3 = (SELECT id FROM community_comment WHERE post_id = @community_post_3 AND content = '建议先把编程基础的前几章过一遍，然后马上用数据库小项目练习，不必整本读完才动手。' ORDER BY id DESC LIMIT 1);
SET @community_parent_4 = (SELECT id FROM community_comment WHERE post_id = @community_post_4 AND content = '可以顺便比较单列索引和联合索引，看看最左前缀对查询计划有什么影响。' ORDER BY id DESC LIMIT 1);
SET @community_parent_5 = (SELECT id FROM community_comment WHERE post_id = @community_post_5 AND content = '我会把每章标题改写成问题，第二天只看问题回答，答不出来再翻书。' ORDER BY id DESC LIMIT 1);
SET @community_parent_7 = (SELECT id FROM community_comment WHERE post_id = @community_post_7 AND content = '这本演示生活类图书步骤比较短，新手可以先试番茄炒蛋和炖菜。' ORDER BY id DESC LIMIT 1);
SET @community_parent_8 = (SELECT id FROM community_comment WHERE post_id = @community_post_8 AND content = '不用强求顺序，孩子愿意描述画面已经是在阅读。可以顺着他的问题展开。' ORDER BY id DESC LIMIT 1);

INSERT INTO community_comment (post_id, user_id, parent_id, content, status, create_time, update_time) VALUES
(@community_post_2, @community_user_muyu, @community_parent_2, '整体是温柔的，虽然有一点遗憾，但结尾会让人觉得安心，可以放心读。', 1, CURRENT_TIMESTAMP(6) - INTERVAL 3 HOUR, CURRENT_TIMESTAMP(6) - INTERVAL 3 HOUR),
(@community_post_3, @community_user_xiaozhou, @community_parent_3, '明白了，我先学到函数和集合，然后就开始做图书数据库的小功能。', 1, CURRENT_TIMESTAMP(6) - INTERVAL 8 HOUR, CURRENT_TIMESTAMP(6) - INTERVAL 8 HOUR),
(@community_post_4, @community_user_ake, @community_parent_4, '好建议，我会把查询条件顺序和索引列顺序都记录下来一起比较。', 1, CURRENT_TIMESTAMP(6) - INTERVAL 17 HOUR, CURRENT_TIMESTAMP(6) - INTERVAL 17 HOUR),
(@community_post_5, @community_user_linan, @community_parent_5, '把标题改成问题这个方法很好，我今晚就用当前章节试一下。', 1, CURRENT_TIMESTAMP(6) - INTERVAL 24 HOUR, CURRENT_TIMESTAMP(6) - INTERVAL 24 HOUR),
(@community_post_7, @community_user_xiaozhou, @community_parent_7, '收到，先从这两道开始，也顺便练一下控制火候。', 1, CURRENT_TIMESTAMP(6) - INTERVAL 47 HOUR, CURRENT_TIMESTAMP(6) - INTERVAL 47 HOUR),
(@community_post_8, @community_user_ake, @community_parent_8, '这样想就轻松多了，下次我先听他讲画面，不急着纠正顺序。', 1, CURRENT_TIMESTAMP(6) - INTERVAL 65 HOUR, CURRENT_TIMESTAMP(6) - INTERVAL 65 HOUR);