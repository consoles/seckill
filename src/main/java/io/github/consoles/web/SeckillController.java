package io.github.consoles.web;

import io.github.consoles.dto.Exposer;
import io.github.consoles.dto.SeckillExcution;
import io.github.consoles.dto.SeckillResult;
import io.github.consoles.entity.Seckill;
import io.github.consoles.enums.SeckillStateEnum;
import io.github.consoles.exception.RepeatKillException;
import io.github.consoles.exception.SeckillCloseException;
import io.github.consoles.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * Created by yiihua-013 on 16/8/8.
 */

@Controller
@RequestMapping("/seckill")
public class SeckillController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillService seckillService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String list(Model model) {
        List<Seckill> seckillList = seckillService.getSeckillList();
        model.addAttribute("list", seckillList);
        return "list";
    }

    @RequestMapping(value = "/{seckillId}/detail", method = RequestMethod.GET)
    public String detail(@PathVariable("seckillId") Long seckillId, Model model) {

        if (seckillId == null) {
            return "redirect:/seckill/list";
        }
        Seckill seckill = seckillService.getById(seckillId);
        if (seckill == null) {
            return "forward:/seckill/list";
        }
        model.addAttribute("seckill", seckill);
        return "detail";
    }

    /**
     * ajax json
     *
     * @param seckillId
     */
    @ResponseBody
    @RequestMapping(value = "/{seckillId}/exposer", method = RequestMethod.POST, produces = {"application/json;charset=utf-8"})
    public SeckillResult<Exposer> exposer(@PathVariable("seckillId") Long seckillId) {

        SeckillResult<Exposer> result;
        try {
            Exposer exposer = seckillService.exportSeckillUrl(seckillId);
            result = new SeckillResult<Exposer>(true, exposer);
        } catch (Exception e) {
            logger.error(e.getMessage());
            result = new SeckillResult<Exposer>(false, e.getMessage());
        }
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/{seckillId}/{md5}/execution", method = RequestMethod.POST, produces = {"application/json;charset=utf-8"})
    public SeckillResult<SeckillExcution> execute(@PathVariable("seckillId") Long seckillId, @PathVariable("md5") String md5, @CookieValue(value = "killPhone", required = false) Long phone) {

        // 可使用SpringMVC valid
        if (phone == null) {
            return new SeckillResult<SeckillExcution>(false, "未注册");
        }
        try {
            SeckillExcution excution = seckillService.executeSeckill(seckillId, phone, md5);
            return new SeckillResult<SeckillExcution>(true, excution);
        } catch (RepeatKillException e1) {
            SeckillExcution excution = new SeckillExcution(seckillId, SeckillStateEnum.REPEAT_KILL);
            return new SeckillResult<SeckillExcution>(true, excution);
        } catch (SeckillCloseException e2) {
            SeckillExcution excution = new SeckillExcution(seckillId, SeckillStateEnum.END);
            return new SeckillResult<SeckillExcution>(true, excution);
        } catch (Exception e) {
            logger.error(e.getMessage());
            SeckillExcution excution = new SeckillExcution(seckillId, SeckillStateEnum.INNER_ERROR);
            return new SeckillResult<SeckillExcution>(true, excution);
        }
    }

    @ResponseBody
    @RequestMapping(value = "/time/now", method = RequestMethod.GET)
    public SeckillResult<Long> time() {
        return new SeckillResult<Long>(true, new Date().getTime());
    }
}
