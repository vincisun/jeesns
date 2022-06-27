package cn.jeesns.service.member;

import cn.jeesns.service.system.ScoreRuleService;
import cn.jeesns.core.dto.Result;
import cn.jeesns.core.model.Page;
import cn.jeesns.core.service.BaseService;
import cn.jeesns.dao.member.IScoreDetailDao;
import cn.jeesns.model.member.ScoreDetail;
import cn.jeesns.model.system.ScoreRule;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;

/**
 * Created by zchuanzhao on 2017/2/21.
 */
@Service
public class ScoreDetailService extends BaseService<ScoreDetail> {
    @Resource
    private IScoreDetailDao scoreDetailDao;
    @Resource
    private ScoreRuleService scoreRuleService;
    @Resource
    private MemberService memberService;

    public Result<ScoreDetail> list(Page page, Integer memberId) {
        List<ScoreDetail> list = scoreDetailDao.list(page,memberId);
        Result model = new Result(0,page);
        model.setData(list);
        return model;
    }

    /**
     * 是否能奖励，true表示可以奖励
     * @param memberId
     * @param scoreRuleId
     * @param type
     * @return
     */
    public boolean canBonus(int memberId, int scoreRuleId, String type) {
        List<ScoreDetail> list = scoreDetailDao.canBonus(memberId,scoreRuleId,type);
        return list.size() == 0;
    }

    /**
     * 根据会员、获取奖励的外键、奖励规则ID获取奖励激励，不包括foreign_id=0
     * @param memberId
     * @param scoreRuleId
     * @param forgignId
     * @return
     */
    public ScoreDetail findByForeignAndRule(int memberId, int scoreRuleId, int forgignId) {
        return scoreDetailDao.findByForeignAndRule(memberId,scoreRuleId,forgignId);
    }

    public void cancel(int scoreDetailId) {
        scoreDetailDao.cancel(scoreDetailId);
    }



    /**
     * 根据积分规则奖励
     * @param memberId
     * @param scoreRuleId
     */
    public void scoreBonus(int memberId, int scoreRuleId) {
        this.scoreBonus(memberId,scoreRuleId,0);
    }


    /**
     * 根据积分规则奖励
     * @param memberId
     * @param scoreRuleId
     * @param foreignId
     */
    public void scoreBonus(int memberId, int scoreRuleId, int foreignId) {
        ScoreRule scoreRule = scoreRuleService.findById(scoreRuleId);
        if(scoreRule != null){
            if(scoreRule.getScore() != 0){
                String type = scoreRule.getType();
                boolean canBonus = true;
                //unlimite为不限制奖励次数
                if(!"unlimite".equals(type)){
                    canBonus = this.canBonus(memberId, scoreRuleId, type);
                }
                if(canBonus){
                    //每个会员、每个奖励规则、每个外键（不包含0）只能奖励一次
                    if(this.findByForeignAndRule(memberId, scoreRuleId, foreignId) == null){
                        memberService.updateScore(scoreRule.getScore(), memberId);
                        ScoreDetail scoreDetail = new ScoreDetail();
                        scoreDetail.setType(1);
                        scoreDetail.setMemberId(memberId);
                        scoreDetail.setForeignId(foreignId);
                        scoreDetail.setScore(scoreRule.getScore());
                        String remark = scoreRule.getName();
                        if(foreignId > 0){
                            remark += " #"+foreignId;
                        }
                        scoreDetail.setRemark(remark);
                        scoreDetail.setScoreRuleId(scoreRuleId);
                        this.save(scoreDetail);
                    }

                }
            }
        }
    }

    public void scoreCancelBonus(int memberId, int scoreRuleId, int foreignId) {
        ScoreDetail scoreDetail = this.findByForeignAndRule(memberId, scoreRuleId, foreignId);
        if(scoreDetail != null){
            this.cancel(scoreDetail.getId());
            //扣除积分
            memberService.updateScore(-scoreDetail.getScore(), memberId);
            ScoreDetail scoreDetailCancel = new ScoreDetail();
            scoreDetailCancel.setType(2);
            scoreDetailCancel.setMemberId(memberId);
            scoreDetailCancel.setForeignId(foreignId);
            scoreDetailCancel.setScore(-scoreDetail.getScore());
            scoreDetailCancel.setRemark("撤销积分奖励 #"+scoreDetail.getId());
            scoreDetailCancel.setScoreRuleId(scoreRuleId);
            this.save(scoreDetailCancel);
        }
    }

    public void save(Integer type, Integer memberId, Integer foreignId, Integer score, String remark) {
        ScoreDetail scoreDetail = new ScoreDetail();
        scoreDetail.setType(type);
        scoreDetail.setMemberId(memberId);
        scoreDetail.setForeignId(foreignId);
        scoreDetail.setScore(score);
        scoreDetail.setRemark(remark);
        save(scoreDetail);
    }
}
