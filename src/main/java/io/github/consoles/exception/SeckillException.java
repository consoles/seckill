package io.github.consoles.exception;

/**
 * 秒杀相关异常基类
 * Created by yiihua-013 on 16/8/6.
 */
public class SeckillException extends RuntimeException{

    public SeckillException(String message) {
        super(message);
    }

    public SeckillException(String message, Throwable cause) {
        super(message, cause);
    }
}
