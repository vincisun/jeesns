package cn.jeesns.web.front;

import cn.jeesns.interceptor.UserLoginInterceptor;
import cn.jeesns.model.member.Member;
import cn.jeesns.model.question.Answer;
import cn.jeesns.model.question.Question;
import cn.jeesns.model.question.QuestionType;
import cn.jeesns.service.member.MemberService;
import cn.jeesns.utils.MemberUtil;
import cn.jeesns.core.annotation.Before;
import cn.jeesns.core.annotation.UsePage;
import cn.jeesns.core.controller.BaseController;
import cn.jeesns.core.dto.Result;
import cn.jeesns.core.utils.JeesnsConfig;
import cn.jeesns.service.question.AnswerService;
import cn.jeesns.service.question.QuestionService;
import cn.jeesns.service.question.QuestionTypeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 前台问答Controller
 * Created by zchuanzhao on 2018/12/21.
 */
@Controller("frontQuestionController")
@RequestMapping("/question/")
public class QuestionController extends BaseController {
    @Resource
    private JeesnsConfig jeesnsConfig;
    @Resource
    private QuestionTypeService questionTypeService;
    @Resource
    private QuestionService questionService;
    @Resource
    private AnswerService answerService;
    @Resource
    private MemberService memberService;

    @UsePage
    @RequestMapping(value={"/","list","list-{statusName}"},method = RequestMethod.GET)
    public String list(String key, @RequestParam(value = "tid",defaultValue = "0",required = false) Integer typeId,
                       @RequestParam(value = "memberId",defaultValue = "0",required = false) Integer memberId,
                       @PathVariable(value = "statusName", required = false) String statusName,
                       Model model) {
        Result<Question> result = questionService.list(typeId, statusName);
        List<QuestionType> questionTypeList = questionTypeService.listAll();
        model.addAttribute("model", result);
        model.addAttribute("questionTypeList", questionTypeList);
        model.addAttribute("statusName", statusName);
        return jeesnsConfig.getFrontTemplate() + "/question/list";
    }

    @UsePage
    @RequestMapping(value="detail/{id}",method = RequestMethod.GET)
    public String detail(@PathVariable("id") Integer id, Model model){
        Member loginMember = MemberUtil.getLoginMember(request);
        Question question = questionService.findById(id);
        question.setMember(memberService.findById(question.getMemberId()));
        Answer bestAnswer = answerService.findById(question.getAnswerId());
        Result answerModel = answerService.listByQuestion(id);
        List<QuestionType> questionTypeList = questionTypeService.listAll();
        //更新访问次数
        questionService.updateViewCount(id);
        model.addAttribute("question",question);
        model.addAttribute("loginUser",loginMember);
        model.addAttribute("bestAnswer",bestAnswer);
        model.addAttribute("answerModel",answerModel);
        model.addAttribute("questionTypeList", questionTypeList);
        return jeesnsConfig.getFrontTemplate() + "/question/detail";
    }

    @RequestMapping(value="ask",method = RequestMethod.GET)
    @Before(UserLoginInterceptor.class)
    public String ask(Model model) {
        List<QuestionType> list = questionTypeService.listAll();
        model.addAttribute("questionTypeList",list);
        return jeesnsConfig.getFrontTemplate() + "/question/ask";
    }

    @RequestMapping(value="ask",method = RequestMethod.POST)
    @ResponseBody
    @Before(UserLoginInterceptor.class)
    public Result ask(Question question) {
        Member loginMember = MemberUtil.getLoginMember(request);
        question.setMemberId(loginMember.getId());
        questionService.save(question);
        Result result = new Result(0);
        result.setData(question.getId());
        return result;
    }

    @RequestMapping(value="/edit/{id}",method = RequestMethod.GET)
    @Before(UserLoginInterceptor.class)
    public String edit(@PathVariable("id") int id, Model model){
        Question question = questionService.findById(id);
        model.addAttribute("question",question);
        return jeesnsConfig.getFrontTemplate() + "/question/edit";
    }

    @RequestMapping(value="/update",method = RequestMethod.POST)
    @ResponseBody
    @Before(UserLoginInterceptor.class)
    public Result update(Question question) {
        Member loginMember = MemberUtil.getLoginMember(request);
        Result result = new Result(questionService.update(loginMember,question));
        result.setData(question.getId());
        return result;
    }


    @RequestMapping(value="delete/{id}",method = RequestMethod.GET)
    @ResponseBody
    @Before(UserLoginInterceptor.class)
    public Result delete(@PathVariable("id") Integer id){
        Member loginMember = MemberUtil.getLoginMember(request);
        Result result = new Result(questionService.delete(loginMember, id));
        return result;
    }

    @RequestMapping(value="close/{id}",method = RequestMethod.GET)
    @ResponseBody
    @Before(UserLoginInterceptor.class)
    public Result close(@PathVariable("id") Integer id){
        Member loginMember = MemberUtil.getLoginMember(request);
        questionService.close(loginMember, id);
        return new Result(0);
    }

    @RequestMapping(value="bestAnswer/{id}/{answerId}",method = RequestMethod.GET)
    @ResponseBody
    @Before(UserLoginInterceptor.class)
    public Result bestAnswer(@PathVariable("id") Integer id, @PathVariable("answerId") Integer answerId){
        Member loginMember = MemberUtil.getLoginMember(request);
        questionService.bestAnswer(loginMember, answerId, id);
        return new Result(0);
    }

}
