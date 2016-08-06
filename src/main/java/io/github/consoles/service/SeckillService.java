package io.github.consoles.service;

import io.github.consoles.dto.Exposer;
import io.github.consoles.dto.SeckillExcution;
import io.github.consoles.entity.Seckill;
import io.github.consoles.exception.RepeatKillException;
import io.github.consoles.exception.SeckillCloseException;
import io.github.consoles.exception.SeckillException;

import java.util.List;

/**
 * Created by yiihua-013 on 16/8/6.
 */
public interface SeckillService {
    /**
     * 查询所有秒杀记录
     * @return
     */
    List<Seckill> getSeckillList();

    /**
     * 查询单个秒杀记录
     * @param seckillId
     * @return
     */
    Seckill getById(long seckillId);

    /**
     * 秒杀开启时,输出秒杀接口地址;否则通输出系统时间和秒杀时间
     * 防止过浏览器插件破解得到地址
     * @param seckillId
     */
    Exposer exportSeckillUrl(long seckillId);

    /**
     * 执行秒杀
     * @param seckillId
     * @param userPhone
     * @param md5
     */
    SeckillExcution executeSeckill(long seckillId, long userPhone, String md5) throws SeckillException,RepeatKillException,SeckillCloseException;


}
