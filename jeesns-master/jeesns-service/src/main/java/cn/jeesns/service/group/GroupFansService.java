package cn.jeesns.service.group;

import cn.jeesns.core.dto.Result;
import cn.jeesns.core.exception.OpeErrorException;
import cn.jeesns.core.model.Page;
import cn.jeesns.core.service.BaseService;
import cn.jeesns.model.group.Group;
import cn.jeesns.model.group.GroupFans;
import cn.jeesns.model.member.Member;
import cn.jeesns.dao.group.IGroupFansDao;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by zchuanzhao on 2016/12/26.
 */
@Service("groupFansService")
public class GroupFansService extends BaseService<GroupFans> {
    @Resource
    private IGroupFansDao groupFansDao;

    public Result listByPage(Page page, Integer groupId) {
        List<GroupFans> list = groupFansDao.list(page, groupId);
        Result model = new Result(0,page);
        model.setData(list);
        return model;
    }

    public GroupFans findByMemberAndGroup(@Param("groupId") Integer groupId, @Param("memberId") Integer memberId) {
        return groupFansDao.findByMemberAndGroup(groupId,memberId);
    }

    /**
     * 关注
     * @param loginMember
     * @param groupId
     * @return
     */
    public boolean save(Member loginMember, Integer groupId) {
        if (null != groupFansDao.findByMemberAndGroup(groupId,loginMember.getId())){
            throw new OpeErrorException("已经关注");
        }
        return groupFansDao.save(groupId,loginMember.getId()) == 1;
    }

    /**
     * 取消关注
     * @param loginMember
     * @param groupId
     * @return
     */
    public boolean delete(Member loginMember, Integer groupId) {
       return groupFansDao.delete(groupId,loginMember.getId()) > 0;
    }


    public Result listByMember(Page page, Integer memberId) {
        List<Group> list = groupFansDao.listByMember(page, memberId);
        Result model = new Result(0,page);
        model.setData(list);
        return model;
    }

}
