## 1. 专有名词

### 



## 2. 服务端架构（淘宝为例）

![1593226386049](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1593226386049.png)

### 2.1 安全体系

- 数据安全体系
- 应用安全体系
- 前端安全体系
- 开发者工具链

### 2.2 业务运营系统

- 会员服务
- 商品服务
- 店铺服务
- 交易服务
- 营销服务
- 物流服务
- 分账服务

### 2.3 共享业务

- 分布式数据层
- 数据分析服务
- 数据同步服务
- 数据搜索服务
- 配置服务
- 运行容器
- 服务管理
- 消息服务
- 负载均衡

### 2.4 中间件服务

- ECS 云服务器
- MQS 队列服务
- OCS 缓存
- OSS
- OTS
- CDN

### 2.5 支撑体系

- 身份服务
- 访问控制服务
- 日志服务
- 监控服务
- 统计服务



## 3. 配置相关

### 3.1 vsftpd文件服务器安装

vsftpd：免费，开源的ftp服务器软件，支持虚拟用户、带宽限制

参考：<https://www.kancloud.cn/chandler/bc-linux/52710>

- 安装 yum -y install vsftpd

  > - 检查是否已安装 rpm -qa|grep vsftpd
  > - 配置文件默认在/etc/vsftpd/vsftpd.conf

- 创建虚拟用户

  ```
  #创建虚拟用户要使用的文件夹，在根目录/用户目录创建
  mkdir ftpfile
  #添加匿名用户(没用登录该linux系统权限)(有警告，忽略即可)
  useradd -d /ftpfile -s /sbin/nologin ftpuser
  #修改ftpfile权限,将文件夹所有者改为该用户(如果不是的话)
  chown -R ftpuser.ftpuser /ftpfile
  #重置用户密码
  passwd ftpuser
  ```

- 配置

  ```
  cd /etc/vsftpd
  vim chroot_list #添加一个文件，将用户名单放进去，只需要写入一个ftpuser即可
  vim /etc/selinux/config #编辑配置，修改SELINUX=disabled
  #目的是防止验证出现550拒绝访问，如果出现请执行setsebool -P ftp_home_dir 1
  #重启linux服务器 reboot
  vim /etc/vsftpd/vsftpd.conf #配置配置文件
  ```

  > - 这里实际上是关闭selinux服务，防止匿名用户无法创建文件/文件夹
  >
  > - 除了关闭，还可以通过放行的方式
  >
  >   ```
  >   getsebool -a | grep ftp  #查询权限，来方便选择开放的权限
  >   
  >   setsebool ftpd_full_access 1 //或者  setsebool -P allow_ftpd_full_access 1
  >   setsebool tftp_home_dir 1  //setsebool -P tftp_home_dir 1
  >   #-P指保存选项，不用重复设置
  >   ```
  >
  > - 临时关闭selinux：setenforce 0 

  ```
  #vsftpd.conf配置内容
  local_root=/ftpfile(当本地用户登入时，将被更换到定义的目录下，默认值为各用户的家目录)
  anon_root=/ftpfile(使用匿名登入时，所登入的目录)
  use_localtime=YES(默认是GMT时间，改成使用本机系统时间)
  anonymous_enable=YES(允许匿名用户登录,设为NO时访问需要登录)
  local_enable=YES(允许本地用户登录)
  write_enable=YES(本地用户可以在自己家目录中进行读写操作)
  local_umask=022(本地用户新增档案时的umask值)
  connect_from_port_20=YES(指定FTP使用20端口进行数据传输，默认值为YES)
  ftpd_banner=Welcome to mmall FTP Server(这里用来定义欢迎话语的字符串)
  chroot_local_user=NO（防止进入其他目录/上级目录）
  chroot_list_enable=YES(设置是否启用chroot_list_file配置项指定的用户列表文件)
  chroot_list_file=/etc/vsftpd/chroot_list(用于指定用户列表文件)
  listen=YES(设置vsftpd服务器是否以standalone模式运行，以standalone模式运行是一种较好的方式，此时listen必须设置为YES，此为默认值。建议不要更改，有很多与服务器运行相关的配置命令，需要在此模式下才有效，若设置为NO，则vsftpd不是以独立的服务运行，要受到xinetd服务的管控，功能上会受到限制)
  #pasv_enable=YES（若设置为YES，则使用PASV工作模式；若设置为NO，则使用PORT模式。）
  pasv_min_port=61001(被动模式使用端口范围最小值)
  pasv_max_port=62000(被动模式使用端口范围最大值)
  ```

  > - centos7配置后启动依然报错，可能是因为网络环境还不支持ipv6，解决方法是将 listen_ipv6=YES更改为：listen_ipv6=NO或者注释掉
  >
  > - 新版本情况下要让chroot名单生效，还需要加一行配置**allow_writeable_chroot=YES**
  >
  >   解释是：2.3.5之后，如果用户被限定在了其主目录下，则该用户的主目录不能再具有写权限了，需新增此配置
  >
  > - 云服务器参考这个 <https://cloud.tencent.com/document/product/213/10912#config>
  >
  >   增加了部分配置
  >
  >   ```
  >   pasv_enable=YES
  >   pasv_address=xxx.xx.xxx.xx #请修改为您的 Linux 云服务器公网 IP
  >   #以及安全组策略添加端口号21  和pasv的端口号
  >   ```

- 关闭防火墙或者防火墙配置

  ```
  systemctl stop firewalld #关闭防火墙
  firewall-cmd --list-services //查看防火墙允许的服务
  firewall-cmd --add-service=ftp #临时开放ftp服务
  firewall-cmd --add-service=ftp --permanent #永久开放ftp服务
  firewall-cmd --remove-service=ftp --permanent #永久关闭ftp服务
  firewall-cmd --add-port=20/tcp --permanent
  firewall-cmd --add-port=21/tcp --permanent //允许外网访问
  firewall-cmd --reload //重新载入配置
  ```

- 验证启动

  ```
  service vsftpd restart #重启，stop关闭，start启动
  chkconfig vsftpd on #开机自启
  ```

  访问ftp://对应ip即可，因为没有阻止匿名用户放登入，所以不需要输入账号密码

  >- 如果通过账号密码登录失败：
  >
  >  - 方案一：
  >
  >    ```
  >    vim /etc/pam.d/vsftpd
  >    注释掉#auth    required pam_shells.so这一行，或者改为pam_nologin.so
  >    ```
  >
  >  - 方案二：
  >
  >    ```
  >    vim /etc/vsftpd/vsftpd.conf
  >    检查
  >    local_enable=YES  
  >    pam_service_name=vsftpd
  >    userlist_enable=YES 
  >    ```
  >
  >  - 方案三：
  >
  >    检查登录用户的工作目录的权限是否开放r权限

### 3.2 Mysql 部分配置

```mysql
#字符集配置
vim /etc/my.cnf
#添加内容
[mysql]
default-character-set=utf8
[client]
default-character-set=utf8
[mysqld]
character-set-server=utf8
```

```mysql
#登录mysql
mysql -u root -p
#重置root用户密码，重置后需要重新登录
update user set password=password('root') where user='root';
flush privileges;
#删除匿名用户
select user,host,password from mysql.user;  
delete from mysql.user where user='';
flush privileges; #刷新生效
#插入新用户（本地）
create user username@localhost identified by 'password';
flush privileges; #刷新生效
#创建数据库
create database `mmal` default character set utf8 collate utf8_general_ci;
#数据库赋权给本地用户（如果创建时没有设定@）
grant all privileges on mmal.* to test@'localhost' identified by '123456' WITH GRANT OPTION;
#设置允许root用户远程连接，访问所有数据库
#grant all privileges on *.* to root@'%'identified by 'password‘ WITH GRANT OPTION;

#如果遇到数据没有及时更新的情况
flush privileges;

#导入sql文件
use mmal;
source /ftpfile/mmal.sql;
#查看数据并格式化显示
use mmal;
select * from mmall_user\G;
```

> - 有时候存在sql文件无法通过指令导入的情况（显示的是找不到这个文件，但实测可以导入从数据库导出的sql文件，说明sql文件本身存在问题），此时可以通过navicat连接执行sql文件，原因可能是数据库版本问题，不用多想
>
> - 通过外部命令导入sql文件暂时没出现这种问题
>
>   mysql -u root -p 库名<a.sql

### 3.3 nginx配置文件

位置：/usr/local/nginx/conf/nginx.conf

```

```



### 3.4 log4j日志配置

- 引入依赖

  ```xml
  	<dependency>
        <groupId>log4j</groupId>
        <artifactId>log4j</artifactId>
        <version>1.2.12</version>
      </dependency>
      <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-api</artifactId>
        <version>1.6.6</version>
      </dependency>
      <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-log4j12</artifactId>
        <version>1.6.6</version>
      </dependency>
  ```
  
- 配置文件 log4j.properties

  ```properties
  log4j.rootLogger=DEBUG,CONSOLE
  
  #暂时只配置控制台日志
  
  log4j.appender.CONSOLE=org.apache.log4j.ConsoleAppender
  log4j.appender.CONSOLE.layout=org.apache.log4j.PatternLayout
  log4j.appender.CONSOLE.layout.ConversionPattern=%d{ISO8601} %-6r [%15.15t] %-5p %30.30c %x - %m\n
  ```

- 如果配合druid数据库进行配置

  ```xml
  <bean id="dataSource" class="com.alibaba.druid.pool.DruidDataSource" init-method="init" destroy-method="close">
          ...
          <property name="filters" value="stat, log4j" />
          <property name="proxyFilters">
              <list>
                  <ref bean="log-filter"/>
              </list>
          </property>
  </bean>
  <bean id="log-filter" class="com.alibaba.druid.filter.logging.Log4jFilter">
          <property name="resultSetLogEnabled" value="false" />
          <property name="statementLogEnabled" value="false" />
          <property name="connectionLogEnabled" value="false" />
  </bean>
  ```

  > - 针对控制台日志出现的日志重复打印问题：
  >
  >   现象：同一条日志记录打印两行，指resultSet、statement等打印，但关闭日志打印又会一条不出
  >
  >   所以进行了配置
  >
  >   ```xml
  >   <property name="resultSetLogEnabled" value="false" />
  >   <property name="statementLogEnabled" value="false" />
  >   <property name="connectionLogEnabled" value="false" />
  >   ```
  >
  >   如果没出现这些问题可以不配置这个内容


## 4.数据库相关

### 4.1 数据表结构

#### mmall_user  用户表

```mysql
DROP TABLE IF EXISTS `mmall_user`;
CREATE TABLE `mmall_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '用户表id',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(50) NOT NULL COMMENT '用户密码，MD5加密',
  `email` varchar(50) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `question` varchar(100) DEFAULT NULL COMMENT '找回密码问题',
  `answer` varchar(100) DEFAULT NULL COMMENT '找回密码答案',
  `role` int(4) NOT NULL COMMENT '角色0-管理员,1-普通用户',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '最后一次更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_name_unique` (`username`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8;
```

> - 用户名通过btree方式设置唯一索引
> - 密码 为通过md5盐值加密后值，非对称加密无法解密
> - role角色int类型 0代表管理员 1代表普通用户

#### mmall_category 分类表

```mysql
DROP TABLE IF EXISTS `mmall_category`;
CREATE TABLE `mmall_category` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '类别Id',
  `parent_id` int(11) DEFAULT NULL COMMENT '父类别id当id=0时说明是根节点,一级类别',
  `name` varchar(50) DEFAULT NULL COMMENT '类别名称',
  `status` tinyint(1) DEFAULT '1' COMMENT '类别状态1-正常,2-已废弃',
  `sort_order` int(4) DEFAULT NULL COMMENT '排序编号,同类展示顺序,数值相等则自然排序',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100032 DEFAULT CHARSET=utf8;
```

>- 从递归角度，以及可无限扩展的角度考虑设计分类表
>- 递归需要设定结束条件，这里parent_id父id中为0表示根节点
>- status表示类别状态，tinyint类型，设置默认值1，使得插入时即使不设置，默认就是可用状态
>- sort_order排序编号，后续扩展用，用来影响展示顺序，相同顺序则自然排序
>- 这里的创建更新时间默认为null

#### mmall_product 产品表

```mysql
DROP TABLE IF EXISTS `mmall_product`;
CREATE TABLE `mmall_product` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '商品id',
  `category_id` int(11) NOT NULL COMMENT '分类id,对应mmall_category表的主键',
  `name` varchar(100) NOT NULL COMMENT '商品名称',
  `subtitle` varchar(200) DEFAULT NULL COMMENT '商品副标题',
  `main_image` varchar(500) DEFAULT NULL COMMENT '产品主图,url相对地址',
  `sub_images` text COMMENT '图片地址,json格式,扩展用',
  `detail` text COMMENT '商品详情',
  `price` decimal(20,2) NOT NULL COMMENT '价格,单位-元保留两位小数',
  `stock` int(11) NOT NULL COMMENT '库存数量',
  `status` int(6) DEFAULT '1' COMMENT '商品状态.1-在售 2-下架 3-删除',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8;
```

> - category_id分类id，不可以为null
>
> - main_image存储产品主图，使用url相对地址，
>
>   在服务器在拿到图片服务器地址后，与这个地址进行拼接。保证图片服务器迁移/域名修改时，只需要改配置，而不需要修改数据库内容
>
> - sub_images对应产品子图，多图采用json格式，同时主图为子图中的第一个地址
>
> - 产品详情detail采用text格式，因为存在富文本
>
> - price价格使用decimal格式，（20，2）表示整体位数20位，小数点2位（整数18位），对应java的BigDecimal类

#### mmall_cart 购物车表

```mysql
DROP TABLE IF EXISTS `mmall_cart`;
CREATE TABLE `mmall_cart` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `product_id` int(11) DEFAULT NULL COMMENT '商品id',
  `quantity` int(11) DEFAULT NULL COMMENT '数量',
  `checked` int(11) DEFAULT NULL COMMENT '是否选择,1=已勾选,0=未勾选',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `user_id_index` (`user_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=146 DEFAULT CHARSET=utf8;
```

> - 创建了一个user_id的索引，因为会经常调用非主键查询，提高查询效率

#### mmall_pay_info 支付信息表

```mysql
DROP TABLE IF EXISTS `mmall_pay_info`;
CREATE TABLE `mmall_pay_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) DEFAULT NULL COMMENT '用户id',
  `order_no` bigint(20) DEFAULT NULL COMMENT '订单号',
  `pay_platform` int(10) DEFAULT NULL COMMENT '支付平台:1-支付宝,2-微信',
  `platform_number` varchar(200) DEFAULT NULL COMMENT '支付宝支付流水号',
  `platform_status` varchar(20) DEFAULT NULL COMMENT '支付宝支付状态',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=61 DEFAULT CHARSET=utf8;
```

> - platform_status支付状态，记录支付的时间点，用原生的回调支付状态（支付宝提供的）

#### mmall_order 订单表

```mysql
DROP TABLE IF EXISTS `mmall_order`;
CREATE TABLE `mmall_order` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '订单id',
  `order_no` bigint(20) DEFAULT NULL COMMENT '订单号',
  `user_id` int(11) DEFAULT NULL COMMENT '用户id',
  `shipping_id` int(11) DEFAULT NULL,
  `payment` decimal(20,2) DEFAULT NULL COMMENT '实际付款金额,单位是元,保留两位小数',
  `payment_type` int(4) DEFAULT NULL COMMENT '支付类型,1-在线支付',
  `postage` int(10) DEFAULT NULL COMMENT '运费,单位是元',
  `status` int(10) DEFAULT NULL COMMENT '订单状态:0-已取消-10-未付款，20-已付款，40-已发货，50-交易成功，60-交易关闭',
  `payment_time` datetime DEFAULT NULL COMMENT '支付时间',
  `send_time` datetime DEFAULT NULL COMMENT '发货时间',
  `end_time` datetime DEFAULT NULL COMMENT '交易完成时间',
  `close_time` datetime DEFAULT NULL COMMENT '交易关闭时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `order_no_index` (`order_no`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=118 DEFAULT CHARSET=utf8;
```

> - order_no订单号添加唯一索引，不能生成多个相同订单号的订单，格式bigint
> - shipping_id对应订单收货地址表id
> - payment实际付款金额
> - postage运费保留，目前包邮
> - status订单状态，通过数字标识
> - payment_time支付时间，通过支付宝回调获取
> - close_time关闭时间，对应下单未支付，订单超时关闭事件

#### mmall_order_item 订单明细表

```mysql
DROP TABLE IF EXISTS `mmall_order_item`;
CREATE TABLE `mmall_order_item` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '订单子表id',
  `user_id` int(11) DEFAULT NULL,
  `order_no` bigint(20) DEFAULT NULL,
  `product_id` int(11) DEFAULT NULL COMMENT '商品id',
  `product_name` varchar(100) DEFAULT NULL COMMENT '商品名称',
  `product_image` varchar(500) DEFAULT NULL COMMENT '商品图片地址',
  `current_unit_price` decimal(20,2) DEFAULT NULL COMMENT '生成订单时的商品单价，单位是元,保留两位小数',
  `quantity` int(10) DEFAULT NULL COMMENT '商品数量',
  `total_price` decimal(20,2) DEFAULT NULL COMMENT '商品总价,单位是元,保留两位小数',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `order_no_index` (`order_no`) USING BTREE,
  KEY `order_no_user_id_index` (`user_id`,`order_no`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=135 DEFAULT CHARSET=utf8;
```

> - 冗余项user_id，可以查询出，但直接存入能提高查询效率
> - product_id商品id与商品表关联，但依然存入商品名和商品地址，起一个快照作用
> - current_unit_price商品单价，依然是对应生成订单时单价，而不通过查询获取
> - 两个索引提高效率，分别是order_no的订单号索引，以及order_no与user_id的组合索引，用于组合查询

#### mmall_shipping 收货地址表

```mysql
DROP TABLE IF EXISTS `mmall_shipping`;
CREATE TABLE `mmall_shipping` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) DEFAULT NULL COMMENT '用户id',
  `receiver_name` varchar(20) DEFAULT NULL COMMENT '收货姓名',
  `receiver_phone` varchar(20) DEFAULT NULL COMMENT '收货固定电话',
  `receiver_mobile` varchar(20) DEFAULT NULL COMMENT '收货移动电话',
  `receiver_province` varchar(20) DEFAULT NULL COMMENT '省份',
  `receiver_city` varchar(20) DEFAULT NULL COMMENT '城市',
  `receiver_district` varchar(20) DEFAULT NULL COMMENT '区/县',
  `receiver_address` varchar(200) DEFAULT NULL COMMENT '详细地址',
  `receiver_zip` varchar(6) DEFAULT NULL COMMENT '邮编',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8;
```



### 4.2 表关系

- 登录读取user表
- 搜索产品使用分类表与商品表，通过关键字/分类id
- 商品添加购物车时，商品id，用户id插入到购物车表
- 购物车下单，订单确认时填写收货地址用到shipping表
- 生成订单，使用订单/订单明细表，其中order_item与product一一对应，order与order_item为一对多关系
- 付款订单，接收回调存储在pay_info表

> - 没有使用外键，减少在分库分表时的麻烦，以及清洗数据的麻烦
> - 数据字段的冗余用来应对流量



### 4.3 数据库问题处理

#### 4.3.1 程序运行一段时间后数据库连接花费超出预期的时间，druid报Communications link failure

过程中日志打印skip not validate connection，表明存在数据库连接无效

原因：mysql服务长时间未连接自动断开，应用从数据库获取到的连接是数据库单方面断开的失效连接。

解决方式（平行策略，不关联）：

- 数据库地址添加&autoReconnect=true

  > 据说只对4.x版本有效

- 项目配置timeBetweenEvictionRunsMillis （检查超时连接的检查间隔，配置为60000，即1分钟）

  minEvictableIdleTimeMillis=300000（连接最少存活时长，30分钟）

  maxEvictableIdleTimeMillis=xxx（连接最大存活时长，默认25200000）

  ```xml
     <!-- #连接的最小超时时间，默认为半小时。 -->
  <property name="minEvictableIdleTimeMillis" value="300000"/>
     <!--# 失效检查线程运行时间间隔     配置60000对应1分钟-->
  <property name="timeBetweenEvictionRunsMillis" value="60000"/>
  ```

     保证max-min要大于time，且mysql的wait_time也大于time
  
     目的是保证连接池检测到超时连接进行回收周期内，连接没有先被mysql断开
  
     - 另外一种配置：
       
       ```xml
       <property name="timeBetweenEvictionRunsMillis" value="2000"/>
       <property name="minEvictableIdleTimeMillis" value="600000"/>
       <property name="maxEvictableIdleTimeMillis" value="900000"/>
       <property name="keepAlive" value="true"/>
       ```

   - 修改数据库闲置连接的超时时间（尽量大），这样也可以避免项目运行时间太长导致数据库连接断开
  
     查询数据库的最大超时时间  show global variables like "wait_timeout";   （我这里查的是28800，单位应该是秒）
  
     vim /etc/my.cnf
  
     配置 （单位是ms）
  
     [mysqld]
     
  
   wait_timeout = 172800
     interactive-timeout = 172800
  
     或者直接修改数据库 （重启失效）
  
     set global wait_timeout=604800;
  
     set global interactive_timeout=604800;
  
     （数值需要再次确认）
  
   默认是28800，指8小时
  
   - 有时候是配置未生效，重启mysql服务试试
  
   - 使用druid的时候推荐使用1.1.5版本及以上，因为1.1.5版本修复了一个重大的bug：testWhileIdle在某些情况下会失效。
  
   - 开启druid的超时回收机制
  
     配置
  
     ```xml
     <!-- 回收被遗弃的   超时的（一般是忘了释放的），空闲的数据库连接到连接池中 -->		
     <property name="removeAbandoned" value="true" />
     <!-- 数据库连接过多长时间不用将被视为被遗弃而收回连接池中  单位为s -->
     <property name="removeAbandonedTimeout" value="1800" />
     <!--回收时打印日志，正式运行时关闭-->
     <property name="logAbandoned" value="false" />
     ```
  
     回收未被释放的连接，避免连接池泄露。
  
     因为生产环境存在长事务，所以生产环境一般不设置，用于开发阶段定位连接泄露位置。
  
   - 配置druid保活
  
     ```xml
   <property name="keepAlive" value="true"/>
     <property name="timeBetweenEvictionRunsMillis" value="60000"/>
   <property name="minIdle" value="10"/>
     ```
     
     keepAlive 为 true
     
     效果为连接池中的minIdle数量以内的连接，空闲时间超过minEvictableIdleTimeMillis，则会执行keepAlive操作。
     
  
   这个时候把minEvictableIdleTimeMillis设置短一些
  
   

## 5.项目初始化

### 5.1 git配置

- 配置忽略列表

  ```
  #编辑 .gitignore文件
  
  *.class
  
  #package file  包文件
  #没有忽略.jar文件，因为用到支付宝jar包需要存在仓库，而不存在于maven仓库
  
  *.war
  *.ear
  
  #kdiff3 ignore
  *.orig
  
  #maven ignore
  target/
  
  #eclipse ignore
  .settings/
  .project
  .classpatch
  
  #idea
  .idea/
  /idea/
  *.ipr
  *.iml
  *.iws
  
  # temp file  临时文件
  
  *.log   #日志文件
  *.cache
  *.diff
  *.patch
  *.tmp
  
  # system ignore  系统级忽略
  .DS_Store
  Thumbs.db
  ```

- 同步到git仓库

  ```
  git add . #全部添加
  git commit -am '注释' #提交到本地仓库
  git remote add origin https://github.com/strangePix/warest_mall.git #关联远程仓库，起别名origin
  git branch #查看分支，第一次提交的分支默认主分支master
  git push -u origin master #推送到远程仓库
  git branch -r #查看远程分支  目前应该是origin/master即主分支
  
  git checkout -b v1.0 origin/master #检出，开分支v1.0，基于主分支
  git push origin HEAD -u #分支推送到远程
  ```

  > - 此次开发为分支开发，主干发布模式，主干为master



### 5.2 pom.xml配置解释

```xml
<build>
    <finalName>mmall</finalName>
    <plugins>
        <!-- 根据数据库生成dao -->
      <plugin>
        <groupId>org.mybatis.generator</groupId>
        <artifactId>mybatis-generator-maven-plugin</artifactId>
        <version>1.3.2</version>
        <configuration>
          <verbose>true</verbose>
          <overwrite>true</overwrite>
        </configuration>
      </plugin>

      <!-- geelynote maven的核心插件之-complier插件默认只支持编译Java 1.4，因此需要加上支持高版本jre的配置，在pom.xml里面加上 增加编译插件 -->
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.8.0</version>
        <configuration>
          <source>1.7</source>
          <target>1.7</target>
          <encoding>UTF-8</encoding>
          <compilerArguments>
              <!--引入目录 为项目根目录/src/main/webapp/WEB-INF/lib,用于集成支付宝额外sdk，编译时引入 -->
            <extdirs>${project.basedir}/src/main/webapp/WEB-INF/lib</extdirs>
          </compilerArguments>
        </configuration>
      </plugin>
    </plugins>

  </build>
```



### 5.3 mybatis-generator配置

- pom.xml配置

  ```xml
  <plugin>
  	<groupId>org.mybatis.generator</groupId>
  	<artifactId>mybatis-generator-maven-plugin</artifactId>
  	<version>1.3.2</version>
  	<configuration>
  		<verbose>true</verbose>
  		<overwrite>true</overwrite>
  	</configuration>
  </plugin>
  ```

- resources/generatorConfig.xml

  ```xml
  <?xml version="1.0" encoding="UTF-8"?>
  <!DOCTYPE generatorConfiguration
        PUBLIC "-//mybatis.org//DTD MyBatis Generator Configuration 1.0//EN"
          "http://mybatis.org/dtd/mybatis-generator-config_1_0.dtd">

  <generatorConfiguration>
      <!--导入属性配置-->
      <properties resource="datasource.properties"></properties>
  
      <!--指定特定数据库的jdbc驱动jar包的位置-->
      <classPathEntry location="${db.driverLocation}"/>
  
      <context id="default" targetRuntime="MyBatis3">
  
          <!-- optional，旨在创建class时，对注释进行控制 -->
          <commentGenerator>
              <property name="suppressDate" value="true"/>
              <property name="suppressAllComments" value="true"/>
          </commentGenerator>
  
          <!--jdbc的数据库连接 -->
          <jdbcConnection
                driverClass="${db.driverClassName}"
                  connectionURL="${db.url}"
                  userId="${db.username}"
                  password="${db.password}">
          </jdbcConnection>
  
  
          <!-- 非必需，类型处理器，在数据库类型和java类型之间的转换控制-->
          <javaTypeResolver>
              <property name="forceBigDecimals" value="false"/>
          </javaTypeResolver>
  
  
          <!-- Model模型生成器,用来生成含有主键key的类，记录类 以及查询Example类
              targetPackage     指定生成的model生成所在的包名
              targetProject     指定在该项目下所在的路径
          -->
          <javaModelGenerator targetPackage="com.warest.mall.domain" targetProject=".\src\main\java">
              <!-- 是否允许子包，即targetPackage.schemaName.tableName -->
              <property name="enableSubPackages" value="false"/>
              <!-- 是否对model添加 构造函数 -->
              <property name="constructorBased" value="true"/>
              <!-- 是否对类CHAR类型的列的数据进行trim操作,去除空格 -->
              <property name="trimStrings" value="true"/>
              <!-- 建立的Model对象是否 不可改变  即生成的Model对象不会有 setter方法，只有构造方法(不可变) -->
              <property name="immutable" value="false"/>
          </javaModelGenerator>
  
          <!--mapper映射文件生成所在的目录 为每一个数据库的表生成对应的SqlMap文件 -->
          <sqlMapGenerator targetPackage="mappers" targetProject=".\src\main\resources">
              <property name="enableSubPackages" value="false"/>
          </sqlMapGenerator>
  
          <!-- 客户端代码，生成易于使用的针对Model对象和XML配置文件 的代码
                  type="ANNOTATEDMAPPER",生成Java Model 和基于注解的Mapper对象
                  type="MIXEDMAPPER",生成基于注解的Java Model 和相应的Mapper对象
                  type="XMLMAPPER",生成SQLMap XML文件和独立的Mapper接口
          -->
  
          <!-- targetPackage：mapper接口dao生成的位置 -->
          <javaClientGenerator type="XMLMAPPER" targetPackage="com.warest.mall.dao" targetProject=".\src\main\java">
              <!-- enableSubPackages:是否让schema作为包的后缀 -->
              <property name="enableSubPackages" value="false" />
          </javaClientGenerator>
  
          <!-- tableName表名 domainObjectName类名 enableCountByExample通过对象查数量 enableUpdateByExample是否通过对象update 等，采用默认配置-->
          <table tableName="mmall_shipping" domainObjectName="Shipping" enableCountByExample="false" enableUpdateByExample="false" enableDeleteByExample="false" enableSelectByExample="false" selectByExampleQueryId="false"></table>
          <table tableName="mmall_cart" domainObjectName="Cart" enableCountByExample="false" enableUpdateByExample="false" enableDeleteByExample="false" enableSelectByExample="false" selectByExampleQueryId="false"></table>
          <!--<table tableName="mmall_cart_item" domainObjectName="CartItem" enableCountByExample="false" enableUpdateByExample="false" enableDeleteByExample="false" enableSelectByExample="false" selectByExampleQueryId="false"></table> -->
          <table tableName="mmall_category" domainObjectName="Category" enableCountByExample="false" enableUpdateByExample="false" enableDeleteByExample="false" enableSelectByExample="false" selectByExampleQueryId="false"></table>
          <table tableName="mmall_order" domainObjectName="Order" enableCountByExample="false" enableUpdateByExample="false" enableDeleteByExample="false" enableSelectByExample="false" selectByExampleQueryId="false"></table>
          <table tableName="mmall_order_item" domainObjectName="OrderItem" enableCountByExample="false" enableUpdateByExample="false" enableDeleteByExample="false" enableSelectByExample="false" selectByExampleQueryId="false"></table>
          <table tableName="mmall_pay_info" domainObjectName="PayInfo" enableCountByExample="false" enableUpdateByExample="false" enableDeleteByExample="false" enableSelectByExample="false" selectByExampleQueryId="false"></table>
          <!-- 指定特定字段生成的java类型 -->
          <table tableName="mmall_product" domainObjectName="Product" enableCountByExample="false" enableUpdateByExample="false" enableDeleteByExample="false" enableSelectByExample="false" selectByExampleQueryId="false">
              <columnOverride column="detail" jdbcType="VARCHAR" />
              <columnOverride column="sub_images" jdbcType="VARCHAR" />
          </table>
          <table tableName="mmall_user" domainObjectName="User" enableCountByExample="false" enableUpdateByExample="false" enableDeleteByExample="false" enableSelectByExample="false" selectByExampleQueryId="false"></table>
  
  
          <!-- geelynote mybatis插件的搭建 -->
      </context>
  </generatorConfiguration>
  
  ```
  
  根据配置内容添加文件resources/datasource.properties
  
  ```xml
  db.driverLocation=D:/JavaWeb/localRepository/mysql/mysql-connector-java/5.1.6/mysql-connector-java-5.1.6.jar   
  #与使用mysql驱动版本一致，是驱动文件在本机的真实位置
  db.driverClassName=com.mysql.jdbc.Driver
  db.url=jdbc:mysql://8.210.35.67:3306/mmal?characterEncoding=utf8
  db.username=test
  db.password=123456
  
  
  db.initialSize = 20
  db.maxActive = 50
  db.maxIdle = 20
  db.minIdle = 10
  db.maxWait = 10git
  db.defaultAutoCommit = true
  db.minEvictableIdleTimeMillis = 3600000
  ```
  
- 根据配置生成dao层代码

  ![image-20200707221446382](C:\Users\ALIENWARE\Desktop\md\项目搭建补充.assets\image-20200707221446382.png)

  执行插件的generate即可

  效果是生成 实体类domain、dao层基础代码和mapper.xml配置文件

- dao层代码基本方法

  ```java
  public interface CartMapper {
      int deleteByPrimaryKey(Integer id);  //根据主键删除
  
      int insert(Cart record); //插入购物车
  
      int insertSelective(Cart record);  //根据选择插入，不传的参数不会传递null值（空值判断）
  
      Cart selectByPrimaryKey(Integer id);
  
      int updateByPrimaryKeySelective(Cart record); //更新（有空值判断）
  
      int updateByPrimaryKey(Cart record);
  }
  ```

  

### 5.4 修改mapper.xml

create_time和update_time交由代码实现更新，减少业务代码复杂度。

```java
<insert id="insert" parameterType="com.warest.mall.domain.User" >
    insert into mmall_user (id, username, password, 
      email, phone, question, 
      answer, role, create_time, 
      update_time)
    values (#{id,jdbcType=INTEGER}, #{username,jdbcType=VARCHAR}, #{password,jdbcType=VARCHAR}, 
      #{email,jdbcType=VARCHAR}, #{phone,jdbcType=VARCHAR}, #{question,jdbcType=VARCHAR}, 
      #{answer,jdbcType=VARCHAR}, #{role,jdbcType=INTEGER}, #{createTime,jdbcType=TIMESTAMP}, 
      #{updateTime,jdbcType=TIMESTAMP})
  </insert>
```

其中的time赋值调用mysql函数now()，即#{updateTime,jdbcType=TIMESTAMP}或者#{createTime,jdbcType=TIMESTAMP}直接替换为now()即可

> - 因为这个数据库的原因，所以没有要求create_time与update_time非空，所以这里可以将xml中如果时间非空才赋值的判断删除，如
>
>   ```xml
>   <update id="updateByPrimaryKeySelective" parameterType="com.warest.mall.domain.Cart" >
>       update mmall_cart
>       <set >
>         <if test="userId != null" >
>           user_id = #{userId,jdbcType=INTEGER},
>         </if>
>         <if test="productId != null" >
>           product_id = #{productId,jdbcType=INTEGER},
>         </if>
>         <if test="quantity != null" >
>           quantity = #{quantity,jdbcType=INTEGER},
>         </if>
>         <if test="checked != null" >
>           checked = #{checked,jdbcType=INTEGER},
>         </if>
>         <if test="createTime != null" >
>           create_time = #{createTime,jdbcType=TIMESTAMP},
>         </if>
>         <!--<if test="updateTime != null" > -->
>           update_time = now(),
>         <!--</if> -->
>       </set>
>       where id = #{id,jdbcType=INTEGER}
>     </update>
>   ```
>
> - 另外，insert需要给create_time与update_time都修改为now()，而update只需要修改update_time

  

### 5.5 mybatis-plugin

可以快速建立mapper.xml与dao层的关联，但只是辅助开发，不是必须

- settings-plugins-browse repositories

  ![image-20200708010206897](C:\Users\ALIENWARE\Desktop\md\项目搭建补充.assets\image-20200708010206897.png)

- 搜索mybatis plugin下载安装，重启即可



### 5.6 mybatis-pagehelper 分页插件

- 引入依赖

  ```xml
  
  ```

- 配置

### 5.7 logback.xml 日志记录

### 5.8 ftp服务器配置

### 5.9 idea自动注入/编译配置

- settings-compiler-设置build/make project automatically 设置实时编译

  ![image-20200708131657006](C:\Users\ALIENWARE\Desktop\md\项目搭建补充.assets\image-20200708131657006.png)

- settings-inspections-spring-spring core-code-autowiring for bean class级别从error改为warning

  ![image-20200708133553835](C:\Users\ALIENWARE\Desktop\md\项目搭建补充.assets\image-20200708133553835.png)

> 目的是阻止mapper自动引入时的报错

### 5.10 git提交

```
git status #检查状态
git add . #添加
git commit -am '提交内容'   #提交
git push #推送
```

### 5.11 其他问题补充

#### 1.启动问题

- 启动时提示：org.apache.jasper.servlet.TldScanner.scanJars 至少有一个JAR被扫描用于TLD但尚未包含TLD。 为此记录器启用调试日志记录，以获取已扫描但未在其中找到TLD的完整JAR列表。 在扫描期间跳过不需要的JAR可以缩短启动时间和JSP编译时间。

  有jar没被用上，让IDEA控制台提示出项目没用上哪些jar，只需要在tomcat的目录下的conf文件夹内的logging.properties文件里加上一句org.apache.jasper.servlet.TldScanner.level = FINE就可以了

  ![img](https://img2018.cnblogs.com/blog/1756615/201908/1756615-20190813184226982-512898079.png)

  可以看到未被用到的jar包被列了出来,可以根据这个删去没用到的jar

  还有一种方式是在tomcat的目录下的conf文件夹内的catalina.properties中tomcat.util.scan.StandardJarScanFilter.jarsToSkip=改为*.jar，做好备份

- 

  

## 6.用户模块

### 6.1 功能

- 登录
- 用户名验证
- 注册
- 忘记密码
- 提交问题答案
- 重置密码
- 获取用户信息
- 更新用户信息
- 退出登录

### 6.2 相关技术/概念

#### 1. 横向越权、纵向越权安全漏洞

横向越权：攻击者访问有相同权限的用户资源

纵向越权：低级别尝试访问高级别资源

> - 横向越权：A用户恶意获取B用户订单详情
> - 纵向越权：普通用户获取管理员权限

```java
//已登录情况下重设密码时，需要验证旧密码才能修改，防止通过已登录用户修改其他用户密码
//验证密码的同时需要确认用户归属
public ResponseEntity<String> resetPassword(String passwordOld, String passwordNew, User user){
        //防止横向越权,要校验一下这个用户的旧密码,一定要指定是这个用户.因为我们会查询一个count(1),如果不指定id,那么结果就是true啦count>0;
        int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld),user.getId());
        if(resultCount == 0){
            return ResponseEntity.createByErrorMessage("旧密码错误");
        }
        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if(updateCount > 0){
            return ResponseEntity.createBySuccessMessage("密码更新成功");
        }
        return ResponseEntity.createByErrorMessage("密码更新失败");
}
```

#### 2.高复用服务响应对象的设计思想和抽象封装

```java
@JsonInclude(JsonInclude.Include.NON_NULL)
//保证序列化json的时候,如果是null的属性不会序列化
public class ResponseEntity<T> implements Serializable {

//    状态码   1表示失败  0表示成功  get_information有一个10
    private int status;
//    状态信息  一般为1时才有
    private String msg;
//    数据  一般为0时才有
    private T data;

    //方便调用，简明通用
    private ResponseEntity(int status){
        this.status = status;
    }
    //如果泛型为String类型，可能与下一个构造方法冲突，通过公共方法规避
    private ResponseEntity(int status, T data){
        this.status = status;
        this.data = data;
    }
    private ResponseEntity(int status, String msg){
        this.status = status;
        this.msg = msg;
    }
    private ResponseEntity(int status, String msg, T data){
        this.status = status;
        this.msg = msg;
        this.data = data;
    }


    @JsonIgnore
    //使之不在json序列化结果当中（不会返回给前端）  
    //判断是否状态成功，用枚举类包含常量
    public boolean isSuccess(){
        return this.status == ResponseCode.SUCCESS.getCode();
    }

    public int getStatus(){
        return status;
    }
    public T getData(){
        return data;
    }
    public String getMsg(){
        return msg;
    }


    //    静态开放方法
    //一个成功的响应，不带数据data
    public static <T> ResponseEntity<T> createBySuccess(){
        return new ResponseEntity<T>(ResponseCode.SUCCESS.getCode());
    }
    //一个成功响应，带消息msg
    public static <T> ResponseEntity<T> createBySuccessMessage(String msg){
        return new ResponseEntity<T>(ResponseCode.SUCCESS.getCode(),msg);
    }
    //一个成功的响应，带数据，传的是T泛型，只会调用T的构造方法，避免传递String类型数据时与上一个搞混
    public static <T> ResponseEntity<T> createBySuccess(T data){
        return new ResponseEntity<T>(ResponseCode.SUCCESS.getCode(),data);
    }
    //一个成功响应，带数据和消息
    public static <T> ResponseEntity<T> createBySuccess(String msg, T data){
        return new ResponseEntity<T>(ResponseCode.SUCCESS.getCode(),msg,data);
    }

    //失败响应，自带响应码消息，基本上就是error
    public static <T> ResponseEntity<T> createByError(){
        return new ResponseEntity<T>(ResponseCode.ERROR.getCode(),ResponseCode.ERROR.getDesc());
    }

    //带注明失败消息的失败响应
    public static <T> ResponseEntity<T> createByErrorMessage(String errorMessage){
        return new ResponseEntity<T>(ResponseCode.ERROR.getCode(),errorMessage);
    }
    //失败响应，响应码自定义
    public static <T> ResponseEntity<T> createByErrorCodeMessage(int errorCode, String errorMessage){
        return new ResponseEntity<T>(errorCode,errorMessage);
    }


}
//枚举类解释常量
public enum ResponseCode {

    SUCCESS(0,"SUCCESS"),
    ERROR(1,"ERROR"),
//    需要登录
    NEED_LOGIN(10,"NEED_LOGIN"),
//    参数错误
    ILLEGAL_ARGUMENT(2,"ILLEGAL_ARGUMENT");

    private final int code;
    private final String desc;


    ResponseCode(int code,String desc){
        this.code = code;
        this.desc = desc;
    }

    public int getCode(){
        return code;
    }
    public String getDesc(){
        return desc;
    }

}

```

#### 3.md5加密/加盐

非对称加密，设置工具类MD5Util.java进行调用加密

```java
import java.security.MessageDigest;


public class MD5Util {
	//内部加密方法
    private static String byteArrayToHexString(byte b[]) {
        StringBuffer resultSb = new StringBuffer();
        for (int i = 0; i < b.length; i++)
            resultSb.append(byteToHexString(b[i]));

        return resultSb.toString();
    }

    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0)
            n += 256;
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }

    /**
     * 返回大写MD5 不对外开放
     *
     * @param origin  被加密的字符串
     * @param charsetname  加密用字符集，为null时使用默认
     * @return 返回为全部大写
     */
    private static String MD5Encode(String origin, String charsetname) {
        String resultString = null;
        try {
            resultString = new String(origin);
            MessageDigest md = MessageDigest.getInstance("MD5");
            if (charsetname == null || "".equals(charsetname))
                resultString = byteArrayToHexString(md.digest(resultString.getBytes()));
            else
                resultString = byteArrayToHexString(md.digest(resultString.getBytes(charsetname)));
        } catch (Exception exception) {
        }
        return resultString.toUpperCase();
    }
	


    private static final String hexDigits[] = {"0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};

}

```

```java
//调用
//MD5加密
user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
//不能解密，验证时将提供的密码用同样方式加密，进行比较
```

- 加盐加密

  增加机密的复杂度，减少被字典解密的记录

  1. 设置盐值在配置文件

     ```properties
     #位于mmall.properties
     password.salt = geelysdafaqj23ou89ZXcj@#$@#$#@KJdjklj;D../dSF.,
     ```

  2. 加密时取出盐拼接在密码处，再调用md5加密

     ```java
     //统一，全部使用utf-8字符集加密
         public static String MD5EncodeUtf8(String origin) {
             origin = origin + PropertiesUtil.getProperty("password.salt", "");
             return MD5Encode(origin, "utf-8");
         }
     ```

  > 很明显，固定盐值在长期运行中安全性也不能保证，后续改进的话，将在设置密码时随机生成盐值，保存在数据库，用于解密即可。

#### 4.通过内部接口，实现对常量的分组

  - 用途是一个类保存多个同级别的常量，但因为还要保存其他常量，不适合完全分在一起

  - 好处是相较于枚举，不会那么繁重；同时依然保存的是常量

  ```java
  public class Const{
      public static final String CURRENT_USER = "currentUser";//普通常量
      public interface Role{
          int ROLE_CUSTOMER = 0; //普通用户
          int ROLE_ADMIN = 0; //管理员
      }
  }
  
  //调用
  public class Test{
      public static void main(){
          System.out.print(Const.Role.ROLE_CUSTOMER);
      }
  }
  ```

#### 5.工具类TokenCache.java，提供token的存储，加密，有效期设置（Guava缓存使用）

```java
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class TokenCache {

//    声明日志
    private static Logger logger = LoggerFactory.getLogger(TokenCache.class);

    //前缀  命名空间  用于区分
    public static final String TOKEN_PREFIX = "token_";

    //本地缓存      静态代码块  guava中
    private static LoadingCache<String,String> localCache =
            CacheBuilder.newBuilder()
                    .initialCapacity(1000) //缓存初始化容量
                    .maximumSize(10000) //缓存最大容量，超过用LRU算法（缓存淘汰）移除缓存项
                    .expireAfterAccess(12, TimeUnit.HOURS) //有效期 12小时
                    .build(new CacheLoader<String, String>() { //匿名实现类
                        //默认的数据加载实现,当调用get取值的时候,如果key没有对应的值,就调用这个方法进行加载.
                        @Override
                        public String load(String s) throws Exception {
                            return "null";  //避免空指针，设定没命中返回特定字符串
                        }
                    });

    //存储token
    public static void setKey(String key,String value){
        localCache.put(key,value);
    }

    //获取token
    public static String getKey(String key){
        String value = null; //初始化
        try {
            value = localCache.get(key);
            if("null".equals(value)){
                return null;
            }
            return value;
        }catch (Exception e){
            logger.error("localCache get error",e);  //打印异常
      }
        return null;
    }
}

```

生成token、存储、取出的方式

```java
String forgetToken = UUID.randomUUID().toString();  //生成一个token
TokenCache.setKey(TokenCache.TOKEN_PREFIX+username,forgetToken);  //本地存储token
String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX+username); //取出token 过期也会返回null
```



### 6.3 接口设计

前台提供11个接口，对应 门户_用户接口

### 6.4 业务补充

- 

### 6.5 补充操作

- 数据库查询避免select * ，会随着数据库的发展查到冗余数据，泄露信息的风险

- 


## 7.分类管理模块

### 7.1 功能

- 获取节点
- 增加节点
- 修改名字
- 获取分类id
- 递归子节点id

### 7.2 相关概念/技术

#### 1.设计封装无限层级树状数据结构

#### 2.递归算法设计思想

#### 3.复杂对象排重处理

#### 4.重写hashcode/equal注意事项



## 8.商品管理模块

### 8.1 功能

- 加入商品
- 更新商品数
- 查询商品数
- 移除商品
- 单选/全选/取消
- 购物车列表

### 8.2 相关概念/技术

#### 1.对接ftp

#### 2.文件上传

#### 3.pagehelper使用细节

- 因为查询语句要交给分页插件扩展，所以mybatis中书写的查询语句结尾不要带分号;

#### 4.文件上传细节

- 因为上传的文件有时候是传到根路径文件夹，要保证文件夹有写入权限（创建者）

#### 5.其他

- 如果用集合装固定数量的元素，然后调用contain方法判断是否包含某关键字，可以选用set集合而非list，时间复杂度是O(1)好过O(n)

## 9.购物车模块

### 9.1 功能

### 9.2 相关概念/技术

- 购物车模块设计思想
- 高复用购物车核心方法
- 浮点型商业运算精度丢失问题

1. guava工具类方法分隔字符串为集合

   ```
   String productIds = “1,2,3,4,5”;
   List<String> productList = Splitter.on(",").splitToList(productIds);
   ```

2. 数据库函数IFNULL

   ```
   数据库函数ifnull可以再查询为null时提供默认值
   select IFNULL(sum(quantity),0) as count from mmall_cart where user_id = #{userId}
   用于返回为null时java端无法用基本数据类型（如int）接收，要么改成包装类，要么防止为空
   ```

## 10.收货地址管理模块

### 10.1  功能

- 添加地址
- 删除地址
- 更新地址
- 地址列表
- 地址分页
- 地址详情

### 10.2 相关概念/技术

- springMVC数据绑定
- mybatis自动生成主键
- 避免横向越权

## 11.支付模块

蚂蚁沙箱环境

- 同步请求的加签和验证签名
- 回调验证（签名、金额、订单号...）
- 过滤重复通知（保证幂等性）
- 验证确保异步通知来自支付宝
- 回调请求的返回（响应支付宝的通知）

支付对接技巧：

- 回调的调试方法
  - 外网远程debug：1.保持远端代码版本与本地一致 2.及时关闭远程debug端口 （不推荐）、
  - 内网穿透



