package io.github.consoles.exception;

/**
 * 重复秒杀异常,运行时异常
 * Created by yiihua-013 on 16/8/6.
 */
public class RepeatKillException extends SeckillException{

    public RepeatKillException(String message) {
        super(message);
    }

    public RepeatKillException(String message, Throwable cause) {
        super(message, cause);
    }
}
