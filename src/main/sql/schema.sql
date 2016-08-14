CREATE DATABASE seckill;
USE seckill;

CREATE TABLE seckill (
    seckill_id  BIGINT      NOT NULL AUTO_INCREMENT
    COMMENT '库存id',
    name        VARCHAR(20) NOT NULL
    COMMENT '商品名称',
    number      INT         NOT NULL
    COMMENT '库存数量',
    start_time  TIMESTAMP   NOT NULL
    COMMENT '秒杀开启时间',
    end_time    TIMESTAMP   NOT NULL
    COMMENT '秒杀结束时间',
    create_time TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
    COMMENT '创建时间',
    PRIMARY KEY (seckill_id),
    KEY idx_start_time(start_time), -- 索引
    KEY idx_end_time(end_time),
    KEY idx_create_time(create_time)
)
    ENGINE = InnoDB
    AUTO_INCREMENT = 1000 -- id从1000开始自增
    DEFAULT CHARSET = utf8
    COMMENT = '秒杀库存';

-- init data
INSERT seckill (name, number, start_time, end_time)
VALUES ('1000元秒杀iPhone6', 100, '2015-11-01 00:00:00', '2015-11-02 00:00:00'),
    ('500元秒杀iPad2', 200, '2015-11-01 00:00:00', '2015-11-02 00:00:00'),
    ('300元秒杀小米4', 300, '2015-11-01 00:00:00', '2015-11-02 00:00:00'),
    ('200元秒杀红米note', 400, '2015-11-01 00:00:00', '2015-11-02 00:00:00');

-- 秒杀成功信息表
-- 用户登陆认证相关信息
CREATE TABLE success_killed (
    seckill_id  BIGINT    NOT NULL
    COMMENT '秒杀商品id',
    user_phone  BIGINT    NOT NULL
    COMMENT '手机号',
    state       TINYINT   NOT NULL DEFAULT -1
    COMMENT '状态标识 -1:无效 0:成功 1:已付款 2:已发货',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    COMMENT '创建时间',
    PRIMARY KEY (seckill_id, user_phone), -- 联合主键,唯一性:一个用户对同一个商品不能做重复秒杀
    KEY idx_create_time(create_time)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8
    COMMENT = '秒杀成功明细';
