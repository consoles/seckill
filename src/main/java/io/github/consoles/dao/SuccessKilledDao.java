package io.github.consoles.dao;

import io.github.consoles.entity.SuccessKilled;
import org.apache.ibatis.annotations.Param;

/**
 * Created by yiihua-013 on 16/8/6.
 */
public interface SuccessKilledDao {

    /**
     * 插入购买明细,可过滤重复(参见表中的联合唯一主键)
     *
     * @param seckillId
     * @param userPhone
     * @return 插入行数
     */
    int insertSuccessKilled(@Param("seckillId") long seckillId, @Param("userPhone") long userPhone);

    /**
     * 根据id查询SuccessKilled并携带秒杀实体
     *
     * @param seckillId
     * @return
     */
    SuccessKilled queryByIdWithSeckill(@Param("seckillId") long seckillId, @Param("userPhone") long userPhone);
}
