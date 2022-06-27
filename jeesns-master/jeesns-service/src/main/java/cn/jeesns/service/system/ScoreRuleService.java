package cn.jeesns.service.system;

import cn.jeesns.core.service.BaseService;
import cn.jeesns.dao.system.IScoreRuleDao;
import cn.jeesns.model.system.ScoreRule;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;

/**
 * Created by zchuanzhao on 2017/3/24.
 */
@Service
public class ScoreRuleService extends BaseService<ScoreRule> {
    @Resource
    private IScoreRuleDao scoreRuleDao;

    public List<ScoreRule> list() {
        return super.listAll();
    }

    @Override
    public ScoreRule findById(Integer id) {
        return super.findById(id);
    }

    @Override
    public boolean update(ScoreRule scoreRule) {
        return super.update(scoreRule);
    }

    public boolean enabled(int id) {
        return scoreRuleDao.enabled(id) == 1;
    }

}
