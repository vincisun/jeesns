package cn.jeesns.web.manage.member;

import cn.jeesns.interceptor.AdminLoginInterceptor;
import cn.jeesns.model.member.Checkin;
import cn.jeesns.service.member.CheckinService;
import cn.jeesns.core.annotation.Before;
import cn.jeesns.core.controller.BaseController;
import cn.jeesns.core.dto.Result;
import cn.jeesns.core.model.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.List;

/**
 * 签到
 * Created by zchuanzhao on 2018/8/20.
 */
@Controller("manageCheckinController")
@RequestMapping("/${jeesns.managePath}/checkin/")
@Before(AdminLoginInterceptor.class)
public class CheckinController extends BaseController {
    private static final String MANAGE_FTL_PATH = "/manage/checkin/";
    @Resource
    private CheckinService checkinService;

    @RequestMapping("list")
    public String list(Model model){
        Page page = new Page<Checkin>(request);
        List<Checkin> list = checkinService.list(page, 0);
        Result result = new Result(0, page);
        result.setData(list);
        model.addAttribute("model",result);
        return MANAGE_FTL_PATH + "list";
    }
}
