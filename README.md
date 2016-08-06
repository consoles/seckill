# SSM高并发秒杀、红包API设计

## maven初始化项目

```bash
# 其中archetypeCatalog=internal表示不从远程获取archetype的分类
# groudId对应包名,artifactId对应项目名
# -X指定maven以DEBUG方式运行，输出详细信息。之前的没有加这个参数执行项目生成的时候输出Generating project in Interactive mode就卡在那里，加上-X参数后定位到原来是从远程获取archetypeCatalog了
$ mvn archetype:generate -DarchetypeCatalog=internal -DgroudId=io.github.consoles -DartifactId=seckill -DarchetypeArtifactId=maven-archetype-webapp -X
```

上面的命令生成的webapp默认使用的是servlet2.3已经太老了,我们可以在tomcat的实例目录中拷贝一个`web.xml`的头部过来(tomcat目录下的`webapps/examples/WEB-INF/web.xml`)

##  秒杀业务分析

![秒杀业务分析](http://7xlan5.com1.z0.glb.clouddn.com/miaosha.png)

如上图所示,秒杀系统的核心其实是对库存的处理。当用户成功秒杀了一个商品的时候会做2件事:

1. 减库存
2. 记录购买明细

以上的2件事组成了一个完整的事务,需要准确的数据落地。以上之所以需要事务就是因为任何一个方面不一致就会出现超卖或者少卖的情况。用户的*购买行为*包括以下的3个方面:1.谁购买成功了;2.成功的时间和有效期;3.付款和发货信息。

**事务机制依然是当前最有效的数据落地方案。**

## 秒杀业务难点

当多个用户同时秒杀同一件商品的时候,就会出现*竞争*。反映到数据库就是事务和行级锁。

1. start transaction
2. update 库存数量(竞争出现在这个阶段)
3. insert 购买明细
4. commit

![使用行级锁保证事务](http://7xlan5.com1.z0.glb.clouddn.com/mysql-row-lock.png)

当一个人秒杀成功的时候其他人会等待,直到该事务commit。从上面可以看出秒杀的难点在于*如何高效处理竞争*。

## DAO层

### DB设计与编码

在设计表的时候最好有一个类型为`timestamp`的`create_time`字段,用来表示该行数据的创建时间。Mybatis可以通过注解和xml编写sql,但是本质上注解是java源码,改动需要重新编译,建议使用*xml编写sql*。

使用mybatis只需要写dao接口,不写实现类。

### Junit单元测试

生成dao的测试用例。

`org.mybatis.spring.MyBatisSystemException: nested exception is org.apache.ibatis.binding.BindingException: Parameter 'offset' not found. Available parameters are [0, 1, param1, param2]`

```java
List<Seckill> queryAll(int offset, int limit);
```

以上的方法在运行的时候会变成下面的形式`queryAll(arg0,arg1);`当一个参数的时候没有问题,当有多个参数的时候需要高度mybatis哪个位置是什么参数,解决方法是修改dao接口,参数增加`@Param`注解。