package cn.jeesns.web.manage.common;

import cn.jeesns.interceptor.AdminLoginInterceptor;
import cn.jeesns.model.common.Link;
import cn.jeesns.service.common.LinkService;
import cn.jeesns.core.annotation.Before;
import cn.jeesns.core.controller.BaseController;
import cn.jeesns.core.dto.Result;
import cn.jeesns.core.model.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * Created by zchuanzhao on 2017/10/13.
 */
@Controller
@RequestMapping("/${jeesns.managePath}/link")
@Before(AdminLoginInterceptor.class)
public class LinkController extends BaseController {
    private static final String MANAGE_FTL_PATH = "/manage/link/";
    @Resource
    private LinkService linkService;

    @RequestMapping("/list")
    public String list(Model model){
        Page page = new Page(request);
        Result result = linkService.listByPage(page);
        model.addAttribute("model", result);
        return MANAGE_FTL_PATH + "list";
    }

    @RequestMapping("/add")
    public String add(){
        return MANAGE_FTL_PATH + "add";
    }

    @RequestMapping("/save")
    @ResponseBody
    public Result save(Link link){
        return new Result(linkService.save(link));
    }


    @RequestMapping("/edit/{id}")
    public String edit(Model model, @PathVariable("id") Integer id){
        Link link = linkService.findById(id);
        model.addAttribute("link",link);
        return MANAGE_FTL_PATH + "edit";
    }

    @RequestMapping("/update")
    @ResponseBody
    public Result update(Link link){
        return new Result(linkService.update(link));
    }

    @RequestMapping("/delete/{id}")
    @ResponseBody
    public Result delete(@PathVariable("id") Integer id){
        return new Result(linkService.deleteById(id));
    }

    @RequestMapping("/enable/{id}")
    @ResponseBody
    public Result enable(@PathVariable("id") Integer id){
        return new Result(linkService.enable(id));
    }


}
