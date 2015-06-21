Description | HTTP method | URL | JAVA implementation | Note
-- | -- | -- | -- | --
`获取热门图书` | GET | /api/books/popular?limit=&offset=&start=&end= | Book.popular() | *ADMIN*: 返回sales
`获取图书详细信息` | GET | /api/books/:isbn | Book.details()
搜索图书 | GET | /api/books/search | Book.simpleSearch(), Book.advancedSearch()
`搜索图书 SimpleSearch` | GET | /api/books?all=&orderBy= | Book.simpleSearch(), all里的关键词由空格隔开，关键词可被ISBN, title, subtitle, authname, summary, pubname, keyword, subject之一匹配，关键词之间的关系是AND。orderBy总共有6种选择，奇数代表升序，偶数是降序，0/1出版年份排序，2/3平均评分排序，4/5（当用户登陆之后可用）被当前用户所信任的用户的平均评分，默认为1 | *PARTIALLY LOGINED*
获取购买建议 | GET | /api/books/:isbn/suggestions | Book.suggest() | *LOGINED*
获取图书的评价 | GET | /api/books/:isbn/feedbacks | Feedback.showFeedbacks()
`获取作者详细信息` | GET | /api/authors/:auth_id | Author.deatils()
`获取热门作者` | GET | /api/authors/popular?limit=&offset=&start=&end= | Author.popular() | 参见获取热门图书
`搜索作者` | GET | /api/authors?limit=&name= | Author.find()
`查询两个作者之间的度` | GET | /api/authors/degree?author1=&author2= | Author.degree()
`获取热门出版社` | GET | /api/publishers/popular?limit=&offset=&start=&end= | Publisher.popular() | 参见获取热门图书
`获取出版社详细信息` | GET | /api/publishers/:pid | Publisher.showDeatils()
`搜索出版社` | GET | /api/publishers?limit=&name= | Publisher.find()
`获取某条评价详细信息` | GET | /api/feedbacks/:fid| Feedback.details()
`获取某个用户的详细信息` | GET | /api/customers/:cid | Customer.details() | *LOGINED*
(Deprecated: 已包含在用户的详细信息中)获取某个用户的所有Order | GET | /api/orders | Order.showAllOrder() | *LOGINED*
`获取某个Order的详细信息` | GET | /api/orders/:oid | Order.showOrderDetails() | *LOGINED*
显示购物车 | GET | **/api/carts/:cid** | Order.showCart() | *LOGINED*
`最Useful的用户` | GET | /api/customers/useful | Customer.usefulUsers() | *ADMIN*
`最trusted的用户` | GET | /api/customers/trusted | Customer.trustedUsers() | *ADMIN*
`添加图书` | POST | /api/books | Book.add() | *ADMIN*
`添加作者` | POST | /api/authors | Author.add() | *ADMIN*
`添加出版社` | POST | /api/publisher | Publisher.add() | *ADMIN*
添加用户 | POST | /api/customers | Customer.signupMenu()
`补货` | PUT | /api/books/:isbn | Book.add() | *ADMIN*
Declare用户 | POST | /api/customers/declare | Customer.declareUser() | *LOGINED*
评价一本书 | POST | /api/books/:isbn/feedbacks | Feedback.record() | *LOGINED*
评价一条评价 | POST | /api/feedbacks/:fid | Feedback.assessFeedback() | *LOGINED*
加入购物车 | POST | **/api/carts** | Order.add2Cart() | *LOGINED*

