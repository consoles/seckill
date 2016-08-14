package io.github.consoles.service.impl;

import io.github.consoles.dao.SeckillDao;
import io.github.consoles.dao.SuccessKilledDao;
import io.github.consoles.dao.cache.RedisDao;
import io.github.consoles.dto.Exposer;
import io.github.consoles.dto.SeckillExcution;
import io.github.consoles.entity.Seckill;
import io.github.consoles.entity.SuccessKilled;
import io.github.consoles.enums.SeckillStateEnum;
import io.github.consoles.exception.RepeatKillException;
import io.github.consoles.exception.SeckillCloseException;
import io.github.consoles.exception.SeckillException;
import io.github.consoles.service.SeckillService;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yiihua-013 on 16/8/6.
 */
@Service
public class SeckillServiceImpl implements SeckillService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * MD5盐值字符串,用于混淆MD5
     */
    private final String salt = "dsjsdijfiosdnfsb;ds$%%$#%#$5345sds\\。;。;水电费\\;[[";

    @Autowired // 该注解是Spring注解,还可使用@Resuorce,@Inject等J2EE规范的注解
    private SeckillDao seckillDao;

    @Autowired
    private RedisDao redisDao;

    @Autowired
    private SuccessKilledDao successKilledDao;

    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0, 4);
    }

    public Seckill getById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    public Exposer exportSeckillUrl(long seckillId) {
        // 缓存优化,一致性维护建立在超时的基础上
        // 1.访问redis
        Seckill seckill = redisDao.getSeckill(seckillId);
        if (seckill == null) {
            // 2.访问DB
            seckill = seckillDao.queryById(seckillId);
            if (seckill == null) {
                return new Exposer(false, seckillId);
            } else {
                // 3.放入redis
                redisDao.putSeckill(seckill);
            }
        }

        Date startTime = seckill.getStartTime();
        Date endTime   = seckill.getEndTime();
        Date nowTime   = new Date();
        if (nowTime.getTime() < startTime.getTime() || nowTime.getTime() > endTime.getTime()) {
            return new Exposer(false, seckillId, nowTime.getTime(), startTime.getTime(), endTime.getTime());
        }
        String md5 = getMD5(seckillId);
        return new Exposer(true, md5, seckillId);
    }

    @Transactional
    public SeckillExcution executeSeckill(long seckillId, long userPhone, String md5) throws SeckillException, RepeatKillException, SeckillCloseException {
        if (md5 == null || !md5.equals(getMD5(seckillId))) {
            throw new SeckillException("seckill data rewrite");
        }

        Date nowTime = new Date();
        try {
            // 记录购买行为
            int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone); // insert ignore
            // 重复秒杀
            if (insertCount <= 0) {
                throw new RepeatKillException("seckill repeat");
            } else {
                // 执行秒杀逻辑:减库存,热点商品竞争
                int updateCount = seckillDao.reduceNumber(seckillId, nowTime);
                // 没有更新到记录意味着秒杀结束
                if (updateCount <= 0) {
                    // rollback
                    throw new SeckillCloseException("seckill closed");
                } else {
                    // commit
                    SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                    return new SeckillExcution(seckillId, SeckillStateEnum.SUCCESS, successKilled);
                }
            }
        } catch (SeckillCloseException e1) {
            throw e1;
        } catch (RepeatKillException e2) {
            throw e2;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new SeckillException("seckill inner error:" + e.getMessage()); // check exception -> runtime exception,spring tx会rollback
        }
    }

    public SeckillExcution executeSeckillProduce(long seckillId, long userPhone, String md5) {
        if (md5 == null || !md5.equals(getMD5(seckillId))) {
            return new SeckillExcution(seckillId, SeckillStateEnum.DATA_REWRITE);
        }
        Date                killTime = new Date();
        Map<String, Object> map      = new HashMap<String, Object>();
        map.put("seckillId", seckillId);
        map.put("phone", userPhone);
        map.put("killTime", killTime);
        map.put("result", null); // 存储过程被执行完后存储过程将被重新赋值
        try {
            seckillDao.killByProcedure(map);
            int result = MapUtils.getInteger(map, "result", -2);
            if (result == 1) {
                SuccessKilled sk = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                return new SeckillExcution(seckillId, SeckillStateEnum.SUCCESS, sk);
            } else {
                return new SeckillExcution(seckillId, SeckillStateEnum.stateOf(result));
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new SeckillExcution(seckillId, SeckillStateEnum.INNER_ERROR);
        }
    }

    private String getMD5(long seckillId) {
        String base = seckillId + "/" + salt;
        return DigestUtils.md5DigestAsHex(base.getBytes());
    }
}
