package cn.jeesns.service.group;

import cn.jeesns.core.service.BaseService;
import cn.jeesns.dao.group.IGroupTopicTypeDao;
import cn.jeesns.model.group.GroupTopicType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author: zchuanzhao
 * @date: 2018/5/9 下午1:17
 */
@Service
public class GroupTopicTypeService extends BaseService<GroupTopicType> {
    @Resource
    private IGroupTopicTypeDao groupTopicTypeDao;

    public GroupTopicType findById(int id) {
        return super.findById(id);
    }

    public List<GroupTopicType> list(int groupId) {
        return groupTopicTypeDao.list(groupId);
    }


}
