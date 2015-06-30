# Bookstore Backend

### Relative Page
Online Demo: [http://chenyux.in/bookstore](http://chenyux.in/bookstore)

Bookstore Frontend: [http://github.com/Orthocenter/BookstoreFrontend](http://github.com/Orthocenter/BookstoreFrontend)

Bookstore CLI: [http://github.com/Orthocenter/BookstoreCLI](http://github.com/Orthocenter/BookstoreCLI)

## Introduction

- A roughly RESTful API created for Bookstore project using JSP.
- Router.jsp, working as a router, matches the specific url and forward to corresponding JAVA function.
- Using JSP session to handle authentication, not the best practice for RESTful API.
- Sideloading is not fully implemented.

## Error conventions

Status Code | Description
--------- | -------- 
405 | This means no method can handle your request, check following list.
404 | All other errors, such as validation failed.

## List of APIs

Description | HTTP Method | URL | JAVA Implementation | Permission
------ | --------- | ---------- | ---------- | ------------
`获取登陆状态` | GET | /whoAmI | Customer.whoAmI() | 
`登陆` | POST | /customers/login | Customer.login() | 
`登出` | POST | /customers/logout | - | 
`注册` | POST | /customers/signup | Customer.signup() | 
`返回某本书的评价统计信息` | GET | /feedbacks/statistic/:isbn | Feedback.statistic() |
`返回区间内每天的站点访问量` | GET | /customers/visits?start=&end= | Customer.visits() | *ADMIN*
`获取最新订单` | GET | /orders/latest?limit=&offset= | Orders.latest() | *ADMIN*
`获取热门图书` | GET | /books/popular?limit=&offset=&start=&end= | Book.popular() | 
`获取图书详细信息` | GET | /books/:isbn | Book.details()
`获取热门作者` | GET | /authors/popular?limit=&offset=&start=&end= | Author.popular() |
`获取作者详细信息` | GET | /authors/:auth_id | Author.deatils()
`获取热门出版社` | GET | /publishers/popular?limit=&offset=&start=&end= | 
`获取出版社详细信息` | GET | /publishers/:pid | Publisher.showDeatils() |
`获取某本书的所有评论` | GET | /feedbacks?isbn=&limit=&offset=&orderBy= | Feedbacks.feedbacks() |
`获取某条评价详细信息` | GET | /feedbacks/:fid| Feedback.details() |
`评价一条评价` | PUT | /feedbacks/:fid | Feedback.assess() | *LOGINED* |
`新评价` | POST | /feedbacks | Feedback.add() | *LOGINED* |
`最Useful的用户` | GET | /customers/useful?limit=&offset= | Customer.useful() | *ADMIN*
`最trusted的用户` | GET | /customers/trusted?limit=&offset= | Customer.trusted() | *ADMIN*
`获取某个用户的详细信息` | GET | /customers/:cid | Customer.details() | *LOGINED*，若用户被要查询的用户信任，则返回详细信息，否则只返回username
`信任某个用户` | PUT | /customers/:cid | Customer.trust() | *LOGINED*
`确认下单` | POST | /orders | Orders.add() | *LOGINED*
`获取区间内每天的orders数量` | GET | /orders/orders?start=&end= | *ADMIN*
`获取订单详情` | GET | /orders/:orderid | Orders.details() | *LOGINED*
`获取购物车内的物品列表` | GET | /carts | Order.cart() | *LOGINED*
`获取购物车内某件物品信息` | GET | /carts/:cart_id |  Order.cartDetails() | *LOGINED*
`加入新物品到购物车内` | POST | /carts | Order.add2cart() | *LOGINED*
`添加出版社` | POST | /publisher | Publisher.add() | *ADMIN*
`添加作者` | POST | /authors | Author.add() | *ADMIN*
`添加图书` | POST | /books | Book.add() | *ADMIN*
`搜索作者` | GET | /authors?limit=&name= | Author.find() |
`搜索出版社` | GET | /publishers?limit=&name= | Publisher.find() |
`搜索图书 SimpleSearch` | GET | /books?all=&limit=&offset=&orderBy= | Book.simpleSearch() |  
`搜索图书 AdvancedSearch` | GET | /books?advanced=&limit=&offset=&orderBy= | Book.advancedSearch() |
`补货` | PUT | /books/:isbn | Book.add() | *ADMIN*
`修改购物车内物品数量` | PUT | /carts/:cart_id | Order.add2Cart() | *LOGINED*
`查询两个作者之间的度` | GET | /authors/degree?author1=&author2= | Author.degree() | *ADMIN*

# License
Code licensed under the MIT license. See LICENSE for more information.
