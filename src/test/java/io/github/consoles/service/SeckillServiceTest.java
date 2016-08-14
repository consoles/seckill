package io.github.consoles.service;

import io.github.consoles.dto.Exposer;
import io.github.consoles.dto.SeckillExcution;
import io.github.consoles.entity.Seckill;
import io.github.consoles.exception.RepeatKillException;
import io.github.consoles.exception.SeckillCloseException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;


/**
 * Created by yiihua-013 on 16/8/7.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml", "classpath:spring/spring-service.xml"})
// 之所以加入2个是因为service依赖dao
public class SeckillServiceTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillService seckillService;

    @Test
    public void getSeckillList() throws Exception {
        List<Seckill> seckillList = seckillService.getSeckillList();
        logger.info("list={}", seckillList);
    }

    @Test
    public void getById() throws Exception {
        long id = 1000L;
        Seckill seckill = seckillService.getById(id);
        logger.info("seckill={}", seckill);
    }

    /**
     * 测试代码完整逻辑
     * 注意:可重复执行
     */
    @Test
    public void testSeckillLogic() {
        long id = 1001L;
        Exposer exposer = seckillService.exportSeckillUrl(id);

        if (exposer.isExposed()) {
            logger.info("exposer={}", exposer);
            long phone = 13456780968L;
            String md5 = exposer.getMd5();
            try {
                SeckillExcution excution = seckillService.executeSeckill(id, phone, md5);
                logger.info("result={}", excution);
            } catch (RepeatKillException e) {
                logger.error(e.getMessage());
            } catch (SeckillCloseException e) {
                logger.error(e.getMessage());
            }
        } else {
            // 秒杀未开启
            logger.warn("exposer={}", exposer);
        }
    }

    @Test
    public void testExecuteSeckillProduce(){
        long seckillId = 1001L;
        long phone = 14567890987L;
        Exposer exposer = seckillService.exportSeckillUrl(seckillId);
        if (exposer.isExposed()){
            String md5 = exposer.getMd5();
            SeckillExcution excution = seckillService.executeSeckillProduce(seckillId, phone, md5);
            logger.info(excution.getStateInfo());
        }
    }

}
