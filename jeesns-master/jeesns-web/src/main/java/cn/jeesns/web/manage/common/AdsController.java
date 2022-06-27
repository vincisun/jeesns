package cn.jeesns.web.manage.common;

import cn.jeesns.interceptor.AdminLoginInterceptor;
import cn.jeesns.model.common.Ads;
import cn.jeesns.service.common.AdsService;
import cn.jeesns.core.annotation.Before;
import cn.jeesns.core.controller.BaseController;
import cn.jeesns.core.dto.Result;
import cn.jeesns.core.model.Page;
import cn.jeesns.core.utils.DateFormatUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * Created by zchuanzhao on 2017/9/07.
 */
@Controller
@RequestMapping("/${jeesns.managePath}/ads")
@Before(AdminLoginInterceptor.class)
public class AdsController extends BaseController {
    private static final String MANAGE_FTL_PATH = "/manage/ads/";
    @Resource
    private AdsService adsService;

    @RequestMapping("/list")
    public String list(Model model){
        Page page = new Page(request);
        Result result = adsService.listByPage(page);
        model.addAttribute("model", result);
        return MANAGE_FTL_PATH + "list";
    }

    @RequestMapping("/add")
    public String add(){
        return MANAGE_FTL_PATH + "add";
    }

    @RequestMapping("/save")
    @ResponseBody
    public Result save(Ads ads){
        String startTimeStr = getParam("startDateTime");
        String endTimeStr = getParam("endDateTime");
        ads.setStartTime(DateFormatUtil.formatDateTime(startTimeStr));
        ads.setEndTime(DateFormatUtil.formatDateTime(endTimeStr));
        ads.setContent(ads.getContent().replace("&lt;","<").replace("&gt;",">").replace("&#47;","/"));
        return new Result(adsService.save(ads));
    }


    @RequestMapping("/edit/{id}")
    public String edit(Model model, @PathVariable("id") Integer id){
        Ads ads = adsService.findById(id);
        model.addAttribute("ads",ads);
        return MANAGE_FTL_PATH + "edit";
    }

    @RequestMapping("/update")
    @ResponseBody
    public Result update(Ads ads){
        String startTimeStr = getParam("startDateTime");
        String endTimeStr = getParam("endDateTime");
        ads.setStartTime(DateFormatUtil.formatDateTime(startTimeStr));
        ads.setEndTime(DateFormatUtil.formatDateTime(endTimeStr));
        ads.setContent(ads.getContent().replace("&lt;","<").replace("&gt;",">").replace("&#47;","/"));
        return new Result(adsService.update(ads));
    }

    @RequestMapping("/delete/{id}")
    @ResponseBody
    public Result delete(@PathVariable("id") Integer id){
        return new Result(adsService.deleteById(id));
    }

    @RequestMapping("/enable/{id}")
    @ResponseBody
    public Result enable(@PathVariable("id") Integer id){
        return new Result(adsService.enable(id));
    }


}
