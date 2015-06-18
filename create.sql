SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE Address, Author, Book, Comment, Customer, Feedback;
DROP TABLE KeywordInclude, Keyword, Orders, Publisher, RatingOfAuthor;
DROP TABLE RatingOfPublisher, SubjectInclude, Subject, Usefulness, WrittenBy;
DROP TABLE TrustRecords, ItemInOrder, Cart;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE Publisher(
    pid INT NOT NULL AUTO_INCREMENT,
    pubname VARCHAR(50) NOT NULL,
    intro VARCHAR(500),
    PRIMARY KEY (pid),
    UNIQUE KEY (pubname)
) CHARACTER SET utf8;

#1
INSERT INTO Publisher (pubname) VALUES ("南海出版公司");
#2
INSERT INTO Publisher (pubname) VALUES ("中信出版股份有限公司");

CREATE TABLE Author(
    authid INT NOT NULL AUTO_INCREMENT,
    authname VARCHAR(20) NOT NULL,
    intro VARCHAR(500),
    PRIMARY KEY (authid)
) CHARACTER SET utf8;

#1
INSERT INTO Author (authname, intro) VALUES ("(日)东野圭吾", "东野圭吾\n日本著名作家。\n1985年，《放学后》获第31届江户川乱步奖，开始专职写作；\n1999年，《秘密》获第52届日本推理作家协会奖；\n2005年出版的《嫌疑人X的献身》史无前例地同时获得第134届直木奖、第6届本格推理小说大奖，以及年度三大推理小说排行榜第1名；\n2008年，《流星之绊》获第43届新风奖；\n2009年出版的《新参者》获两大推理小说排行榜年度第1名；\n2012年，《解忧杂货店》获第7届中央公论文艺奖。\n2014年，《祈りの幕が下りる時》（暂译《祈祷落幕时》）获第48届吉川英治文学奖。");
#2
INSERT INTO Author (authname, intro) VALUES ("彼得•蒂尔", "彼得•蒂尔（Peter Thiel）\n被誉为硅谷的天使，投资界的思想家。1998年创办PayPal并担任CEO，2002年将PayPal以15亿美元出售给eBay，把电子商务带向新纪元。2004年做了首笔在Facebook的外部投资，并担任董事。同年成立软件公司Palantir，服务于国防安全与全球金融领域的数据分析。蒂尔联合创办了Founders Fund基金，为LinkedIn、SpaceX、Yelp等十几家出色的科技新创公司提供早期资金，其中多家公司由PayPal的同事负责营运，这些人在硅谷有“PayPal黑帮”之称。他成立了蒂尔奖学金（Thiel Fellowship）鼓励年轻人在校园之外学习和创业。他还成立了蒂尔基金（Thiel Foundation），推动科技进步和对未来的长远思考。");
#3
INSERT INTO Author (authname, intro) VALUES ("布莱克•马斯特斯", "布莱克•马斯特斯 （Blake Masters）\n2012年在斯坦福大学法学院就读，期间选修彼得•蒂尔的“初创企业”课，将细心整理的课堂笔记发布到网络，引起240万次的点击率。随后，彼得•蒂尔参与将这份神奇的笔记精编成为本书。");


CREATE TABLE Book(
    isbn VARCHAR(20) NOT NULL,
    title VARCHAR(50) NOT NULL,
    subtitle VARCHAR(50),
    price FLOAT NOT NULL,
    pid INT NOT NULL,
    copies INT NOT NULL,
    pubdate DATE,
    format VARCHAR(50),
    keyword VARCHAR(50),
    subject VARCHAR(50),
    summary VARCHAR(5000),
    #translator VARCHAR(50),
    PRIMARY KEY (isbn),
    FOREIGN KEY (pid) REFERENCES Publisher(pid)
) CHARACTER SET utf8;

INSERT INTO Book (isbn, title, price, pid, pubdate, copies, keyword, subject, format, summary) VALUES
    ("9787544270878", "解忧杂货店", 39.5, 1, "2014-5-1", 100, "东野圭吾 日本 小说 日本文学 推理", "小说", "精装", "现代人内心流失的东西，这家杂货店能帮你找回——\n僻静的街道旁有一家杂货店，只要写下烦恼投进卷帘门的投信口，第二天就会在店后的牛奶箱里得到回答。\n因男友身患绝症，年轻女孩静子在爱情与梦想间徘徊；克郎为了音乐梦想离家漂泊，却在现实中寸步难行；少年浩介面临家庭巨变，挣扎在亲情与未来的迷茫中……\n他们将困惑写成信投进杂货店，随即奇妙的事情竟不断发生。\n生命中的一次偶然交会，将如何演绎出截然不同的人生？\n如今回顾写作过程，我发现自己始终在思考一个问题：站在人生的岔路口，人究竟应该怎么做？我希望读者能在掩卷时喃喃自语：我从未读过这样的小说。——东野圭吾");
INSERT INTO Book (isbn, title, subtitle, price, pid, pubdate, copies, keyword, subject, format, summary) VALUES
    ("9787508649719", "从0到1", "开启商业与未来的秘密", 45.0, 2, "2015-1-1", 100, "创业 互联网 科技 管理", "流行经济读物", "精装", "图书简介：\nhttp://v.youku.com/v_show/id_XOTA0NjcyMzE2.html?wm=3333_2001\n硅谷创投教父、PayPal创始人作品，斯坦福大学改变未来的一堂课，为世界创造价值的商业哲学。\n在科技剧烈改变世界的今天，想要成功，你必须在一切发生之前研究结局。\n你必须找到创新的独特方式，让未来不仅仅与众不同，而且更加美好。\n从0到1，为自己创造无限的机会与价值！\nPaypal创始人、Facebook第一位外部投资者彼得•蒂尔在本书中详细阐述了自己的创业历程与心得，包括如何避免竞争、如何进行垄断、如何发现新的市场。《从0到1》还将带你穿越哲学、历史、经济等多元领域，解读世界运行的脉络，分享商业与未来发展的逻辑，帮助你思考从0到1的秘密，在意想不到之处发现价值与机会。\n揭开创新的秘密，进入彼得•蒂尔颠覆式的商业世界：\n创新不是从1到N，而是从0到1\n全球化并不全是进步\n竞争扼杀创新\n“产品会说话”是谎言\n失败者才去竞争，创业者应当选择垄断\n创业开局十分重要，“频繁试错”是错误的\n没有科技公司可以仅靠品牌吃饭\n初创公司要打造帮派文化");

CREATE TABLE WrittenBy(
    isbn VARCHAR(20) NOT NULL,
    authid INT NOT NULL,
    PRIMARY KEY (isbn, authid),
    FOREIGN KEY (isbn) REFERENCES Book(isbn),
    FOREIGN KEY (authid) REFERENCES Author(authid)
) CHARACTER SET utf8;

INSERT INTO WrittenBy (isbn, authid) VALUES ("9787544270878", 1);
INSERT INTO WrittenBy (isbn, authid) VALUES ("9787508649719", 2), ("9787508649719", 3);

CREATE TABLE Customer(
    cid INT NOT NULL AUTO_INCREMENT,
    username VARCHAR(20) NOT NULL,
    password CHAR(40) NOT NULL,
    name VARCHAR(20) NOT NULL,
    email VARCHAR(50),
    phone VARCHAR(50),
    PRIMARY KEY (cid),
    UNIQUE KEY (username)
) CHARACTER SET utf8;

#1
INSERT INTO Customer (username, password, name, email, phone) VALUES ("root", SHA1("root"), "administrator", "root@YuxinBookstore", "110");
#2
INSERT INTO Customer (username, password, name, email, phone) VALUES ("chenyuxin", SHA1("chenyuxin"), "Yuxin Chen", "chenyuxin.mail@gmail.com", "18601669278");
#3
INSERT INTO Customer (username, password, name, email, phone) VALUES ("nisiyu", SHA1("nisiyu"), "Siyu Ni", "nisiyu@qq.com", "111111111");
#4
INSERT INTO Customer (username, password, name) VALUES ("huangweijun", SHA1("huangweijun"), "Weijun Huang");


-- CREATE TABLE Subject(
--     sid INT NOT NULL AUTO_INCREMENT,
--     subject VARCHAR(20) NOT NULL,
--     PRIMARY KEY (sid)
-- ) CHARACTER SET utf8;

-- CREATE TABLE Keyword(
--     kid INT NOT NULL AUTO_INCREMENT,
--     word VARCHAR(20) NOT NULL,
--     PRIMARY KEY (kid)
-- ) CHARACTER SET utf8;

CREATE TABLE Address(
    addrid INT NOT NULL AUTO_INCREMENT,
    cid INT NOT NULL,
    zip INT,
    room VARCHAR(50),
    street VARCHAR(50),
    district VARCHAR(50),
    city VARCHAR(50),
    state VARCHAR(50),
    country VARCHAR(50),
    rphone VARCHAR(50),
    rname VARCHAR(50),
    PRIMARY KEY (addrid, cid),
    FOREIGN KEY (cid) REFERENCES Customer(cid)
) CHARACTER SET utf8;

#1
INSERT INTO Address (cid, zip, room, street, district, city, state, country) VALUES (2, 361009, "9X", "吕岭路X号", "思明区", "厦门", "福建省", "中国");
#2
INSERT INTO Address (cid, zip, room, street, district, city, state, country) VALUES (1, 201203, "8X", "蔡伦路X号", "浦东新区", "上海", "上海", "中国");

CREATE TABLE Orders(
    orderid INT NOT NULL AUTO_INCREMENT,
    time DATETIME NOT NULL,
    cid INT NOT NULL,
    addrid INT NOT NULL,
    PRIMARY KEY (orderid),
    FOREIGN KEY (addrid, cid) REFERENCES Address(addrid, cid)
) CHARACTER SET utf8;

#1
INSERT INTO Orders (time, cid, addrid) VALUES (now(), 2, 1);
#2
INSERT INTO Orders (time, cid, addrid) VALUES (now(), 2, 1);

CREATE TABLE ItemInOrder(
    id INT NOT NULL AUTO_INCREMENT,
    orderid INT NOT NULL,
    isbn VARCHAR(20) NOT NULL,
    amount INT NOT NULL,
    price FLOAT NOT NULL,
    UNIQUE (id),
    PRIMARY KEY (orderid, isbn),
    FOREIGN KEY (orderid) REFERENCES Orders(orderid),
    FOREIGN KEY (isbn) REFERENCES Book(isbn)
) CHARACTER SET utf8;

# orderid = 1
INSERT INTO ItemInOrder (orderid, isbn, price, amount) VALUES (1, "9787544270878", 33.3, 1);
INSERT INTO ItemInOrder (orderid, isbn, price, amount) VALUES (1, "9787508649719", 44.4, 2);
# orderid = 2
INSERT INTO ItemInOrder (orderid, isbn, price, amount) VALUES (2, "9787508649719", 44.4, 33);

-- CREATE TABLE KeywordInclude(
--     isbn VARCHAR(20) NOT NULL,
--     kid INT NOT NULL,
--     PRIMARY KEY (isbn, kid),
--     FOREIGN KEY (isbn) REFERENCES Book(isbn),
--     FOREIGN KEY (kid) REFERENCES Keyword(kid)
-- ) CHARACTER SET utf8;

-- CREATE TABLE SubjectInclude(
--     isbn VARCHAR(20) NOT NULL,
--     sid INT NOT NULL,
--     PRIMARY KEY (isbn, sid),
--     FOREIGN KEY (isbn) REFERENCES Book(isbn),
--     FOREIGN KEY (sid) REFERENCES Subject(sid)
-- ) CHARACTER SET utf8;

CREATE TABLE Feedback(
    fid INT NOT NULL AUTO_INCREMENT,
    isbn VARCHAR(20) NOT NULL,
    cid INT NOT NULL,
    score INT NOT NULL,
    time DATETIME NOT NULL,
    comment VARCHAR(500),
    PRIMARY KEY (fid),
    UNIQUE KEY (isbn, cid),
    FOREIGN KEY (isbn) REFERENCES Book(isbn),
    FOREIGN KEY (cid) REFERENCES Customer(cid)
) CHARACTER SET utf8;

#1
INSERT INTO Feedback (isbn, cid, score, comment, time) VALUES ("9787544270878", 1, 5, "很好", NOW());
#2
INSERT INTO Feedback (isbn, cid, score, comment, time) VALUES ("9787544270878", 4, 10, "赞赞赞", NOW());
#3
INSERT INTO Feedback (isbn, cid, score, comment, time) VALUES ("9787544270878", 2, 1, "不知道在说什么", NOW());
#4
INSERT INTO Feedback (isbn, cid, score, time) VALUES ("9787544270878", 3, 1, NOW());
#5
INSERT INTO Feedback (isbn, cid, score, comment, time) VALUES ("9787508649719", 1, 1, "什么鬼", NOW());
#6
INSERT INTO Feedback (isbn, cid, score, comment, time) VALUES ("9787508649719", 4, 10, "超好看", NOW());
#7
INSERT INTO Feedback (isbn, cid, score, comment, time) VALUES ("9787508649719", 2, 3, "又不知道在说什么", NOW());


CREATE TABLE Usefulness(
    fid INT NOT NULL,
    cid INT NOT NULL,
    rating INT NOT NULL,
    PRIMARY KEY (fid, cid),
    FOREIGN KEY (fid) REFERENCES Feedback(fid),
    FOREIGN KEY (cid) REFERENCES Customer(cid)
) CHARACTER SET utf8;

#1
INSERT INTO Usefulness (fid, cid, rating) VALUES (1, 2, 1);
#2
INSERT INTO Usefulness (fid, cid, rating) VALUES (1, 3, 2);
#3
INSERT INTO Usefulness (fid, cid, rating) VALUES (1, 4, 1);
#4
INSERT INTO Usefulness (fid, cid, rating) VALUES (2, 4, 0);
#5
INSERT INTO Usefulness (fid, cid, rating) VALUES (2, 1, 1);

CREATE TABLE TrustRecords(
    cid1 INT NOT NULL,
    cid2 INT NOT NULL,
    trust BOOLEAN NOT NULL,
    PRIMARY KEY (cid1, cid2),
    FOREIGN KEY (cid1) REFERENCES Customer(cid),
    FOREIGN KEY (cid2) REFERENCES Customer(cid)
) CHARACTER SET utf8;

INSERT INTO TrustRecords (cid1, cid2, trust) VALUES (2, 1, true);
INSERT INTO TrustRecords (cid1, cid2, trust) VALUES (2, 3, false);

CREATE TABLE Cart(
    id INT NOT NULL AUTO_INCREMENT,
    cid INT NOT NULL,
    isbn VARCHAR(20) NOT NULL,
    amount INT NOT NULL,
    UNIQUE (id),
    PRIMARY KEY (cid, isbn),
    FOREIGN KEY (cid) REFERENCES Customer(cid),
    FOREIGN KEY (isbn) REFERENCES Book(isbn)
) CHARACTER SET utf8;

INSERT INTO Cart (cid, isbn, amount) VALUES (4, "9787544270878", 2);
INSERT INTO Cart (cid, isbn, amount) VALUES (3, "9787544270878", 5);
INSERT INTO Cart (cid, isbn, amount) VALUES (3, "9787508649719", 2);

-- CREATE TABLE RatingOfPublisher(
--     pid INT NOT NULL,
--     cid INT NOT NULL,
--     rating INT NOT NULL,
--     PRIMARY KEY (pid, cid),
--     FOREIGN KEY (pid) REFERENCES Publisher(pid),
--     FOREIGN KEY (cid) REFERENCES Customer(cid)
-- ) CHARACTER SET utf8;

-- CREATE TABLE RatingOfAuthor(
--     authid INT NOT NULL,
--     cid INT NOT NULL,
--     rating INT NOT NULL,
--     PRIMARY KEY (authid, cid),
--     FOREIGN KEY (authid) REFERENCES Author(authid),
--     FOREIGN KEY (cid) REFERENCES Customer(cid)
-- ) CHARACTER SET utf8;