Description | HTTP method | URL | JAVA implementation | Note
-- | -- | -- | -- | --
获取热门图书列表 | GET | /api/books | Book.showPopularBooks()
获取图书详细信息 | GET | /api/books/:isbn | Book.details()
搜索图书 | GET | /api/books/search | Book.simpleSearch(), Book.advancedSearch()
获取购买建议 | GET | /api/books/:isbn/suggestions | Book.suggest()
获取图书的评价 | GET | /api/books/:isbn/feedbacks | Feedback.showFeedbacks()
获取作者详细信息 | GET | /api/authors/:auth_id | Author.showDeatils()
获取热门作者列表 | GET | /api/authors | Author.showPopularAuthors()
搜索作者 | GET | /api/authors/search | Author.search()
查询两个作者之间的度 | GET | /api/authors/degree | Author.showDegreesOfSeperation()
获取热门出版社 | GET | /api/publishers 
获取出版社详细信息 | GET | /api/publishers/:pid | Publisher.showDeatils()
获取某个用户的所有Order | GET | /api/orders | Order.showAllOrder() | *LOGINED*
获取某个Order的详细信息 | GET | /api/orders/:oid | Order.showOrderDetails() | *LOGINED*
显示购物车 | GET | **/api/carts** | Order.showCart() | *LOGINED*
最Useful的用户 | GET | /api/users/useful | Customer.usefulUsers() | *ADMIN*
最trusted的用户 | GET | /api/users/trusted | Customer.trustedUsers() | *ADMIN*
添加图书 | POST | /api/books | Book.add(), **Author.writtenBy()** | *ADMIN*
添加作者 | POST | /api/authors | Author.add() | *ADMIN*
添加用户 | POST | /api/users | Customer.signupMenu()
补货 | POST | /api/books/:isbn | Book.replenish() | *ADMIN*
Declare用户 | POST | /api/users/declare | Customer.declareUser() | *LOGINED*
评价一本书 | POST | /api/books/:isbn/feedbacks | Feedback.record() | *LOGINED*
评价一条评价 | POST | /api/feedbacks/:fid | Feedback.assessFeedback() | *LOGINED*
加入购物车 | POST | **/api/carts** | Order.add2Cart() | *LOGINED*

