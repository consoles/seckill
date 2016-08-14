-- 秒杀执行的存储过程,存储过程优化的是事务行级锁持有的时间,不要过度依赖存储过程,简单的逻辑可以使用存储过程
-- 存储过程一般用于银行系统,因为它们购买了大型的Oracle或者DB2数据库,使用存储过程将SQL语句放到MySQL服务端可以提高性能到一个商品6000QPS

DELIMITER $$
-- in 输入参数,out输出参数
-- ROW_COUNT():返回上一条修改类型(delete,insert,update)的SQL影响的行数,0未修改 >0修改行数,<0sql错误/未执行修改SQL
CREATE PROCEDURE seckill.execute_seckill
    (IN v_seckill_id BIGINT, IN v_phone BIGINT,
     IN v_kill_time  TIMESTAMP, OUT r_result INT)
    BEGIN
        DECLARE insert_count INT DEFAULT 0;
        START TRANSACTION;
        INSERT IGNORE success_killed
        (seckill_id, user_phone, create_time)
        VALUES (v_seckill_id, v_phone, v_kill_time);
        SELECT row_count()
        INTO insert_count;
        IF (insert_count = 0)
        THEN
            ROLLBACK;
            SET r_result = -1;
        ELSEIF (insert_count < 0)
            THEN
                ROLLBACK;
                SET r_result = -2;
        ELSE
            UPDATE seckill
            SET number = number - 1
            WHERE seckill_id = v_seckill_id
                  AND end_time > v_kill_time
                  AND start_time < v_kill_time
                  AND number > 0;
            SELECT row_count()
            INTO insert_count;
            IF (insert_count = 0)
            THEN
                ROLLBACK;
                SET r_result = 0;
            ELSEIF (insert_count < 0)
                THEN
                    ROLLBACK;
                    SET r_result = -2;
            ELSE
                COMMIT;
                SET r_result = 1;
            END IF;
        END IF;
    END;
$$

-- 测试存储过程
DELIMITER ;
SHOW CREATE PROCEDURE seckill.execute_seckill\G;
SET @r_result = -3;
CALL execute_seckill(1003, 18978675643, now(), @r_result);
SELECT @r_result;
