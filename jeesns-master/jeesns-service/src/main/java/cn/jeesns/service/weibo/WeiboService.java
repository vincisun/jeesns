package cn.jeesns.service.weibo;

import cn.jeesns.service.member.MemberService;
import cn.jeesns.service.member.MessageService;
import cn.jeesns.service.member.ScoreDetailService;
import cn.jeesns.service.picture.PictureService;
import cn.jeesns.service.system.ActionLogService;
import cn.jeesns.utils.ActionLogType;
import cn.jeesns.utils.ActionUtil;
import cn.jeesns.utils.ConfigUtil;
import cn.jeesns.utils.ScoreRuleConsts;
import cn.jeesns.core.consts.AppTag;
import cn.jeesns.core.enums.MessageType;
import cn.jeesns.core.dto.Result;
import cn.jeesns.core.exception.OpeErrorException;
import cn.jeesns.core.exception.ParamException;
import cn.jeesns.core.model.Page;
import cn.jeesns.core.service.BaseService;
import cn.jeesns.core.utils.*;
import cn.jeesns.model.member.Member;
import cn.jeesns.model.weibo.Weibo;
import cn.jeesns.model.weibo.WeiboTopic;
import cn.jeesns.dao.weibo.IWeiboDao;
import cn.jeesns.utils.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zchuanzhao on 2016/11/25.
 */
@Service("weiboService")
public class WeiboService extends BaseService<Weibo> {
    @Resource
    private IWeiboDao weiboDao;
    @Resource
    private WeiboFavorService weiboFavorService;
    @Resource
    private ActionLogService actionLogService;
    @Resource
    private PictureService pictureService;
    @Resource
    private ScoreDetailService scoreDetailService;
    @Resource
    private MessageService messageService;
    @Resource
    private MemberService memberService;
    @Resource
    private WeiboTopicService weiboTopicService;

    public Weibo findById(int id, int memberId) {
        Weibo weibo = weiboDao.findById(id,memberId);
        return weibo;
    }

    @Transactional
    public boolean save(HttpServletRequest request, Member loginMember, String content, String pictures) {
        if("0".equals(request.getServletContext().getAttribute(ConfigUtil.WEIBO_POST.toUpperCase()))){
            throw new OpeErrorException("微博已关闭");
        }
        ValidUtill.checkIsBlank(content, "内容不能为空");
        if(content.length() > Integer.parseInt((String) request.getServletContext().getAttribute(ConfigUtil.WEIBO_POST_MAXCONTENT.toUpperCase()))){
            throw new ParamException("内容不能超过"+request.getServletContext().getAttribute(ConfigUtil.WEIBO_POST_MAXCONTENT.toUpperCase())+"字");
        }
        //获取话题
        String topicName = TopicUtil.getTopicName(content);
        WeiboTopic weiboTopic = null;
        if (StringUtils.isNotBlank(topicName)){
            weiboTopic = weiboTopicService.findByName(topicName);
            if (weiboTopic == null){
                weiboTopic = new WeiboTopic();
                weiboTopic.setName(topicName);
                weiboTopicService.save(weiboTopic);
            }
        }
        Weibo weibo = new Weibo();
        weibo.setMemberId(loginMember.getId());
        weibo.setContent(content);
        weibo.setStatus(1);
        if(StringUtils.isEmpty(pictures)){
            //普通文本
            weibo.setType(0);
        }else {
            //图片
            weibo.setType(1);
        }
        if (weiboTopic != null){
            weibo.setTopicId(weiboTopic.getId());
        }
        int result = weiboDao.saveObj(weibo);
        if(result == 1){
            //@会员处理并发送系统消息
            messageService.atDeal(loginMember.getId(),content, AppTag.WEIBO, MessageType.WEIBO_REFER,weibo.getId());
            pictureService.update(weibo.getId(),pictures, content);
            actionLogService.save(loginMember.getCurrLoginIp(),loginMember.getId(), ActionUtil.POST_WEIBO,"", ActionLogType.WEIBO.getValue(),weibo.getId());
            //发布微博奖励
            scoreDetailService.scoreBonus(loginMember.getId(), ScoreRuleConsts.RELEASE_WEIBO, weibo.getId());
        }
        return result == 1;
    }

    public Result<Weibo> listByPage(Page page, int memberId, int loginMemberId, String key) {
        if (StringUtils.isNotBlank(key)){
            key = "%"+key.trim()+"%";
        }
        List<Weibo> list = weiboDao.list(page, memberId,loginMemberId,key);
        list = this.formatWeibo(list);
        Result model = new Result(0,page);
        model.setData(list);
        return model;
    }

    @Transactional
    public boolean delete(HttpServletRequest request, Member loginMember, int id) {
        Weibo weibo = this.findById(id,loginMember.getId());
        ValidUtill.checkIsNull(weibo, "微博不存在");
        weiboDao.delete(id);
        //扣除积分
        scoreDetailService.scoreCancelBonus(loginMember.getId(),ScoreRuleConsts.RELEASE_WEIBO,id);
        pictureService.deleteByForeignId(request, id);
        actionLogService.save(loginMember.getCurrLoginIp(),loginMember.getId(), ActionUtil.DELETE_WEIBO, "ID："+weibo.getId()+"，内容："+weibo.getContent());
        return true;
    }

    @Transactional
    public boolean userDelete(HttpServletRequest request, Member loginMember, int id) {
        Weibo weibo = this.findById(id,loginMember.getId());
        ValidUtill.checkIsNull(weibo, "微博不存在");
        if(loginMember.getIsAdmin() == 0 && (loginMember.getId().intValue() != weibo.getMember().getId().intValue())){
            throw new OpeErrorException("没有权限");
        }
        return this.delete(request, loginMember,id);
    }

    public List<Weibo> hotList(int loginMemberId) {
        List<Weibo> hotList = weiboDao.hotList(loginMemberId);
        hotList = this.formatWeibo(hotList);
        return hotList;
    }

    @Transactional
    public Result favor(Member loginMember, int weiboId) {
        ValidUtill.checkParam(weiboId == 0);
        Weibo weibo = this.findById(weiboId,loginMember.getId());
        Result Result = new Result(0);
        if(weiboFavorService.find(weiboId,loginMember.getId()) == null){
            //增加
            weiboDao.favor(weiboId,1);
            weibo.setFavor(weibo.getFavor() + 1);
            weiboFavorService.save(weiboId,loginMember.getId());
            //发布微博奖励
            scoreDetailService.scoreBonus(loginMember.getId(), ScoreRuleConsts.WEIBO_RECEIVED_THUMBUP, weiboId);
            //点赞之后发送系统信息
            messageService.diggDeal(loginMember.getId(),weibo.getMemberId(),AppTag.WEIBO,MessageType.WEIBO_ZAN,weibo.getId());
        }else {
            //减少
            weiboDao.favor(weiboId,-1);
            weibo.setFavor(weibo.getFavor() - 1);
            weiboFavorService.delete(weiboId,loginMember.getId());
            //扣除积分
            scoreDetailService.scoreCancelBonus(loginMember.getId(),ScoreRuleConsts.WEIBO_RECEIVED_THUMBUP,weiboId);
            Result.setCode(1);
        }
        Result.setData(weibo.getFavor());
        return Result;
    }

    public List<Weibo> listByCustom(int loginMemberId, String sort, int num, int day) {
        List<Weibo> list = weiboDao.listByCustom(loginMemberId,sort,num,day);
        list = this.formatWeibo(list);
        return list;
    }

    public Result<Weibo> listByTopic(Page page, int loginMemberId, String topicName) {
        WeiboTopic weiboTopic = weiboTopicService.findByName(topicName);
        List<Weibo> list;
        if (weiboTopic == null){
            weiboTopic = new WeiboTopic();
            weiboTopic.setName(topicName);
            weiboTopicService.save(weiboTopic);
            list = new ArrayList<>();
        }else {
            list = weiboDao.listByTopic(page, loginMemberId, weiboTopic.getId());
            list = this.formatWeibo(list);
        }
        Result model = new Result(0,page);
        model.setData(list);
        return model;
    }

    public Weibo formatWeibo(Weibo weibo){
        if (weibo != null){
            weibo.setContent(memberService.atFormat(weibo.getContent()));
            weibo.setContent(TopicUtil.formatTopic(weibo.getContent()));
        }
        return weibo;
    }

    public List<Weibo> formatWeibo(List<Weibo> weiboList){
        if (weiboList != null && !weiboList.isEmpty()){
            for (Weibo weibo : weiboList){
                formatWeibo(weibo);
            }
        }
        return weiboList;
    }

}
