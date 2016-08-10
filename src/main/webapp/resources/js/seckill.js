/**
 * Created by yiihua-013 on 16/8/10.
 */

'use strict';

var seckill = {
    // 封装秒杀相关ajax的url
    URL: {
        now: function () {
            return '/seckill/time/now';
        },
        exposer: function (seckillId) {
            return `/seckill/${seckillId}/exposer`;
        },
        execution: function (seckillId, md5) {
            return `/seckill/${seckillId}/${md5}/execution`;
        }
    },
    // 处理秒杀逻辑
    handleSeckill: function (seckillId, jQElement) {
        // 获取秒杀地址,控制显示逻辑,执行秒杀
        jQElement.hide().html('<button class="btn btn-primary btn-lg" id="killBtn">开始秒杀</button>');
        $.post(seckill.URL.exposer(seckillId), {}, function (result) {
            // 回调函数中执行交互流程
            if (result && result['success']) {
                var exposer = result['data'];
                if (exposer.exposed) {
                    // 获取秒杀地址
                    var killUrl = seckill.URL.execution(seckillId, exposer.md5);
                    console.log('killUrl:', killUrl);
                    // 绑定一次点击事件,防止重复点击
                    $('#killBtn').one('click', function () {
                        // 执行秒杀请求:先禁用按钮再发送秒杀请求,最后显示秒杀结果
                        $(this).addClass('disabled');
                        $.post(killUrl, {}, function (result) {
                            if (result && result.success) {
                                var killResult = result.data;
                                var state = killResult.state;
                                var stateInfo = killResult.stateInfo;
                                jQElement.html(`<span class="label label-success">${stateInfo}</span>`);
                            }
                        })
                    });
                    jQElement.show();
                } else {
                    // 未开启秒杀
                    var now = exposer.now,
                        start = exposer.start,
                        end = exposer.end;
                    // 每个客户端的机器当运行足够长时间的时候会有误差,所以需要重新计算计时逻辑
                    seckill.countdown(seckillId, now, start, end);
                }
            } else {
                console.error('result:', result);
            }
        });
    },
    // 验证手机号
    validatePhone: function (phone) {
        return phone && phone.length == 11 && !isNaN(phone);
    },
    countdown: function (seckillId, nowTime, startTime, endTime) {
        // 时间判断
        var $seckillBox = $('#seckill_box');
        if (nowTime > endTime) {
            $seckillBox.html('秒杀结束');
        } else if (nowTime < startTime) {
            // 秒杀未开始,计时事件绑定
            var killTime = new Date(startTime + 1000); // 加1s防止计时时间偏移,不加也行
            $seckillBox.countdown(killTime, function (event) {
                var format = event.strftime('秒杀倒计时:%D天 %H时 %M分 %S秒')
                $seckillBox.html(format);
            }).on('finish.countdown', function () {
                seckill.handleSeckill(seckillId, $seckillBox);
            });
        } else {
            // 执行秒杀
            seckill.handleSeckill(seckillId, $seckillBox);
        }
    },
    // 详情页秒杀逻辑
    detail: {
        // 详情页初始化
        init: function (params) {
            // 用户手机验证和登陆,计时交互
            // 在cookie中查找手机号
            var killPhone = $.cookie('killPhone');

            if (!seckill.validatePhone(killPhone)) {
                // 绑定phone,控制输出
                var $killPhoneModal = $('#killPhoneModal');
                $killPhoneModal.modal({
                    show: true,
                    backdrop: 'static', // 禁止位置关闭
                    keyboard: false, // 禁止键盘事件
                });
                $('#killPhoneBtn').click(function () {
                    var inputPhone = $('#killPhoneKey').val();
                    if (seckill.validatePhone(inputPhone)) {
                        // 电话写入cookie
                        $.cookie('killPhone', inputPhone, {expires: 7, path: '/seckill'});
                        location.reload();
                    } else {
                        $('#killPhoneMessage').hide().html('<label class="label label-danger">手机号错误!</label>').show(300);
                    }
                });
            }
            // 已经登录
            // 计时交互
            var startTime = params['startTime'];
            var endTime = params['endTime'];
            var seckillId = params['seckillId'];
            $.get(seckill.URL.now(), {}, function (result) {
                if (result && result['success']) {
                    var nowTime = result['data'];
                    // 时间判断,计时交互
                    seckill.countdown(seckillId, nowTime, startTime, endTime);
                } else {
                    console.error('result:', result);
                }
            });
        }
    }
};
