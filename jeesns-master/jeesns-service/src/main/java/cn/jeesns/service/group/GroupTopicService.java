package cn.jeesns.service.group;

import cn.jeesns.service.member.MemberService;
import cn.jeesns.service.member.MessageService;
import cn.jeesns.service.member.ScoreDetailService;
import cn.jeesns.service.system.ActionLogService;
import cn.jeesns.core.service.BaseService;
import cn.jeesns.core.utils.HtmlUtil;
import cn.jeesns.utils.ActionLogType;
import cn.jeesns.utils.ActionUtil;
import cn.jeesns.utils.ScoreRuleConsts;
import cn.jeesns.core.utils.ValidUtill;
import cn.jeesns.core.consts.AppTag;
import cn.jeesns.core.enums.MessageType;
import cn.jeesns.core.dto.Result;
import cn.jeesns.core.exception.OpeErrorException;
import cn.jeesns.core.exception.ParamException;
import cn.jeesns.core.model.Page;
import cn.jeesns.core.utils.StringUtils;
import cn.jeesns.dao.group.IGroupTopicDao;
import cn.jeesns.model.group.Group;
import cn.jeesns.model.group.GroupTopic;
import cn.jeesns.model.member.Member;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by zchuanzhao on 2016/12/26.
 */
@Service("groupTopicService")
public class GroupTopicService extends BaseService<GroupTopic> {
    @Resource
    private IGroupTopicDao groupTopicDao;
    @Resource
    private GroupService groupService;
    @Resource
    private GroupTopicCommentService groupTopicCommentService;
    @Resource
    private GroupFansService groupFansService;
    @Resource
    private ActionLogService actionLogService;
    @Resource
    private ScoreDetailService scoreDetailService;
    @Resource
    private MessageService messageService;
    @Resource
    private MemberService memberService;
    @Resource
    private GroupTopicFavorService groupTopicFavorService;

    public GroupTopic findById(int id) {
        return this.findById(id,null);
    }

    public GroupTopic findById(int id,Member loginMember) {
        int loginMemberId = loginMember == null ? 0 : loginMember.getId();
        return this.atFormat(groupTopicDao.findById(id,loginMemberId));
    }

    @Transactional
    public boolean save(Member member, GroupTopic groupTopic) {
        if(groupTopic.getGroupId() == null || groupTopic.getGroupId() == 0){
            throw new ParamException();
        }
        Group group = groupService.findById(groupTopic.getGroupId());
        ValidUtill.checkIsNull(group, "???????????????");
        if(groupFansService.findByMemberAndGroup(group.getId(),member.getId()) == null){
            throw new OpeErrorException("????????????????????????????????????");
        }
        if(group.getCanPost() == 0){
            throw new OpeErrorException("???????????????????????????");
        }
        groupTopic.setMemberId(member.getId());
        //????????????
        groupTopic.setStatus(group.getTopicReview()==0?1:0);
        int result = groupTopicDao.saveObj(groupTopic);
        if(result == 1){
            //@?????????????????????????????????
            messageService.atDeal(member.getId(),groupTopic.getContent(), AppTag.GROUP, MessageType.GROUP_TOPIC_REFER,groupTopic.getId());
            //??????????????????
            scoreDetailService.scoreBonus(member.getId(), ScoreRuleConsts.GROUP_POST, groupTopic.getId());
            actionLogService.save(member.getCurrLoginIp(),member.getId(), ActionUtil.POST_GROUP_TOPIC,"", ActionLogType.GROUP_TOPIC.getValue(),groupTopic.getId());
        }
        return result == 1;
    }

    public Result listByPage(Page page, String key, int groupId, int status, int memberId, int typeId) {
        if (StringUtils.isNotBlank(key)){
            key = "%"+key+"%";
        }
        List<GroupTopic> list = groupTopicDao.list(page, key,groupId,status,memberId,typeId);
        Result model = new Result(0,page);
        model.setData(list);
        return model;
    }

    public boolean update(Member member, GroupTopic groupTopic) {
        GroupTopic findGroupTopic = this.findById(groupTopic.getId(),member);
        ValidUtill.checkIsNull(findGroupTopic, "???????????????");
        if(member.getId().intValue() != findGroupTopic.getMember().getId().intValue()){
            throw new OpeErrorException("????????????");
        }
        if (groupTopic.getTypeId() != null && !groupTopic.getTypeId().equals(findGroupTopic.getTypeId())){
            groupTopicDao.updateType(groupTopic.getId(),groupTopic.getTypeId());
        }
        findGroupTopic.setTitle(groupTopic.getTitle());
        findGroupTopic.setThumbnail(groupTopic.getThumbnail());
        findGroupTopic.setContent(groupTopic.getContent());
        findGroupTopic.setDescription(groupTopic.getDescription());
        findGroupTopic.setKeywords(groupTopic.getKeywords());
        //????????????
        if (member.getIsAdmin() == 0) {
            if (member.getId().intValue() != findGroupTopic.getMemberId().intValue()) {
                return false;
            }
        } else {
            //?????????
            findGroupTopic.setSource(groupTopic.getSource());
            if (groupTopic.getViewCount() != null && groupTopic.getViewCount() > 0){
                findGroupTopic.setViewCount(groupTopic.getViewCount());
            }
            findGroupTopic.setWriter(groupTopic.getWriter());
            if (groupTopic.getViewRank() != null && groupTopic.getViewRank() > 0){
                findGroupTopic.setViewRank(groupTopic.getViewRank());
            }
        }
        if (findGroupTopic.getViewCount() == null) {
            findGroupTopic.setViewCount(0);
        }
        if (findGroupTopic.getViewRank() == null) {
            findGroupTopic.setViewRank(0);
        }
        if (StringUtils.isEmpty(findGroupTopic.getDescription())) {
            String contentStr = HtmlUtil.delHTMLTag(findGroupTopic.getContent());
            if (contentStr.length() > 200) {
                findGroupTopic.setDescription(contentStr.substring(0, 200));
            } else {
                findGroupTopic.setDescription(contentStr);
            }
        }
        if (StringUtils.isEmpty(findGroupTopic.getThumbnail())) {
            Document doc = Jsoup.parseBodyFragment(findGroupTopic.getContent());
            Elements elements = doc.select("img[src]");
            if (elements.size() > 0) {
                String imgsrc = elements.get(0).attr("src");
                findGroupTopic.setThumbnail(imgsrc);
            }else {
                findGroupTopic.setThumbnail(null);
            }
        }
        return groupTopicDao.updateObj(findGroupTopic) == 1;
    }


    @Transactional
    public boolean delete(Member loginMember, int id) {
        GroupTopic groupTopic = this.findById(id);
        ValidUtill.checkIsNull(groupTopic, "???????????????");
        int result = groupTopicDao.delete(id);
        if(result == 1){
            //????????????
            scoreDetailService.scoreCancelBonus(loginMember.getId(),ScoreRuleConsts.GROUP_POST,id);
            groupTopicCommentService.deleteByTopic(id);
            actionLogService.save(loginMember.getCurrLoginIp(),loginMember.getId(), ActionUtil.DELETE_GROUP_TOPIC,"ID???"+groupTopic.getId()+"????????????"+groupTopic.getTitle());

        }
        return result == 1;
    }


    @Transactional
    public boolean indexDelete(HttpServletRequest request, Member loginMember, int id) {
        GroupTopic groupTopic = this.findById(id,loginMember);
        ValidUtill.checkIsNull(groupTopic, "???????????????");
        Group group = groupService.findById(groupTopic.getGroup().getId());
        ValidUtill.checkIsNull(group);
        String groupManagers = group.getManagers();
        String[] groupManagerArr = groupManagers.split(",");
        boolean isManager = false;
        for (String manager : groupManagerArr){
            if(loginMember.getId().intValue() == Integer.parseInt(manager)){
                isManager = true;
            }
        }
        if(loginMember.getId().intValue() == groupTopic.getMember().getId().intValue() || loginMember.getIsAdmin() > 0 ||
                isManager || loginMember.getId().intValue() == group.getCreator().intValue()){
            boolean flag = this.delete(loginMember,id);
            return flag;
        }
        throw new OpeErrorException("????????????");
    }

    public boolean audit(Member member, int id) {
        GroupTopic groupTopic = this.findById(id,member);
        ValidUtill.checkIsNull(groupTopic, "???????????????");
        Group group = groupService.findById(groupTopic.getGroup().getId());
        ValidUtill.checkIsNull(group);
        String groupManagers = group.getManagers();
        String[] groupManagerArr = groupManagers.split(",");
        boolean isManager = false;
        for (String manager : groupManagerArr){
            if(member.getId() == Integer.parseInt(manager)){
                isManager = true;
            }
        }
        if(member.getId().intValue() == groupTopic.getMember().getId().intValue() || member.getIsAdmin() > 0 ||
                isManager || member.getId().intValue() == group.getCreator().intValue()){
            return groupTopicDao.audit(id) == 1;
        }
        throw new OpeErrorException("????????????");
    }

    public boolean top(Member member, int id, int top) {
        GroupTopic groupTopic = this.findById(id,member);
        ValidUtill.checkIsNull(groupTopic, "???????????????");
        Group group = groupService.findById(groupTopic.getGroup().getId());
        ValidUtill.checkIsNull(group);
        String groupManagers = group.getManagers();
        String[] groupManagerArr = groupManagers.split(",");
        boolean isManager = false;
        for (String manager : groupManagerArr){
            if(member.getId() == Integer.parseInt(manager)){
                isManager = true;
            }
        }
        if(member.getId().intValue() == groupTopic.getMember().getId().intValue() || member.getIsAdmin() > 0 ||
                isManager || member.getId().intValue() == group.getCreator().intValue()){
            return groupTopicDao.top(id,top) == 1;
        }
        throw new OpeErrorException("????????????");
    }

    /**
     * ?????????????????????
     * @param member
     * @param id
     * @param essence
     * @return
     */
    public boolean essence(Member member, int id, int essence) {
        GroupTopic groupTopic = this.findById(id,member);
        ValidUtill.checkIsNull(groupTopic, "???????????????");
        Group group = groupService.findById(groupTopic.getGroup().getId());
        ValidUtill.checkIsNull(group);
        String groupManagers = group.getManagers();
        String[] groupManagerArr = groupManagers.split(",");
        boolean isManager = false;
        for (String manager : groupManagerArr){
            if(member.getId() == Integer.parseInt(manager)){
                isManager = true;
            }
        }
        if(member.getId().intValue() == groupTopic.getMember().getId().intValue() || member.getIsAdmin() > 0 ||
                isManager || member.getId().intValue() == group.getCreator().intValue()){
            return groupTopicDao.essence(id,essence) == 1;
        }
        throw new OpeErrorException("????????????");
    }


    public Result favor(Member loginMember, int id) {
        GroupTopic groupTopic = this.findById(id);
        ValidUtill.checkIsNull(groupTopic, "???????????????");
        int favor = groupTopic.getFavor();
        String message;
        Result<Integer> result;
        if(groupTopicFavorService.find(id,loginMember.getId()) == null){
            //??????
            groupTopicDao.favor(id,1);
            groupTopicFavorService.save(id,loginMember.getId());
            message = "????????????";

            favor += 1;
            //??????????????????
            scoreDetailService.scoreBonus(loginMember.getId(), ScoreRuleConsts.GROUP_TOPIC_RECEIVED_LIKE, id);
            //??????????????????????????????
            messageService.diggDeal(loginMember.getId(),groupTopic.getMemberId(),AppTag.GROUP,MessageType.GROUP_TOPIC_LIKE,groupTopic.getId());
        }else {
            //??????
            groupTopicDao.favor(id,-1);
            groupTopicFavorService.delete(id,loginMember.getId());
            message = "??????????????????";
            favor -= 1;
            //??????????????????
            //????????????
            scoreDetailService.scoreCancelBonus(loginMember.getId(),ScoreRuleConsts.GROUP_TOPIC_RECEIVED_LIKE, id);
        }
        result = new Result(0,message);
        result.setData(favor);
        return result;
    }

    public List<GroupTopic> listByCustom(int gid, String sort, int num, int day,int thumbnail) {
        return groupTopicDao.listByCustom(gid,sort,num,day,thumbnail);
    }

    public void updateViewCount(int id) {
        groupTopicDao.updateViewCount(id);
    }

    public GroupTopic atFormat(GroupTopic groupTopic){
        if (groupTopic != null){
            groupTopic.setContent(memberService.atFormat(groupTopic.getContent()));
        }
        return groupTopic;
    }
}
