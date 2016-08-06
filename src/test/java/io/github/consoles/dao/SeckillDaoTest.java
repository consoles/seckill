package io.github.consoles.dao;

import io.github.consoles.entity.Seckill;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by yiihua-013 on 16/8/6.
 */

/**
 * spring junit整合
 * spring-test,junit
 */

@RunWith(SpringJUnit4ClassRunner.class) // junit启动时加载spring IOC容器
@ContextConfiguration({"classpath:spring/spring-dao.xml"}) // 告诉junit spring的配置文件
public class SeckillDaoTest {

    // 注入dao实现类依赖
    @Resource
    private SeckillDao seckillDao;

    @Test
    public void queryById() throws Exception {
        long id = 1000;
        Seckill seckill = seckillDao.queryById(id);
        System.out.println(seckill.getName());
        System.out.println(seckill);
    }

    // List<Seckill> queryAll(int offset, int limit);
    // 以上的方法在运行的时候会变成下面的形式:
    // queryAll(arg0,arg1);
    // 当一个参数的时候没有问题,当有多个参数的时候需要高度mybatis哪个位置是什么参数,解决方法是修改dao接口,参数增加@Param注解
    @Test
    public void queryAll() throws Exception {
        List<Seckill> seckills = seckillDao.queryAll(0, 100);
        for (Seckill seckill:seckills){
            System.out.println(seckill);
        }
    }

    @Test
    public void reduceNumber() throws Exception {
        int updateCount = seckillDao.reduceNumber(1000L,new Date());
        System.out.println("updateCount = " + updateCount);
    }

}