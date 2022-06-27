package cn.jeesns.service.cms;

import cn.jeesns.dao.cms.IArticleCommentDao;
import cn.jeesns.model.cms.Article;
import cn.jeesns.model.cms.ArticleComment;
import cn.jeesns.model.member.Member;
import cn.jeesns.service.member.MemberService;
import cn.jeesns.service.member.MessageService;
import cn.jeesns.service.member.ScoreDetailService;
import cn.jeesns.service.system.ActionLogService;
import cn.jeesns.utils.ActionUtil;
import cn.jeesns.utils.ScoreRuleConsts;
import cn.jeesns.core.service.BaseService;
import cn.jeesns.core.utils.ValidUtill;
import cn.jeesns.core.consts.AppTag;
import cn.jeesns.core.enums.MessageType;
import cn.jeesns.core.exception.OpeErrorException;
import cn.jeesns.core.model.Page;
import cn.jeesns.core.utils.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by zchuanzhao on 2016/10/14.
 */
@Service("articleCommentService")
public class ArticleCommentService extends BaseService<ArticleComment> {
    @Resource
    private IArticleCommentDao articleCommentDao;
    @Resource
    private ArticleService articleService;
    @Resource
    private ActionLogService actionLogService;
    @Resource
    private ScoreDetailService scoreDetailService;
    @Resource
    private MessageService messageService;
    @Resource
    private MemberService memberService;

    @Override
    public ArticleComment findById(Integer id) {
        return this.atFormat(super.findById(id));
    }

    public boolean save(Member loginMember, String content, Integer articleId) {
        Article article = articleService.findById(articleId);
        ValidUtill.checkIsNull(article, "文章不存在");
        ValidUtill.checkIsBlank(content, "内容不能为空");
        ArticleComment articleComment = new ArticleComment();
        articleComment.setMemberId(loginMember.getId());
        articleComment.setArticleId(articleId);
        articleComment.setContent(content);
        boolean result = super.save(articleComment);
        if(!result){
            throw new OpeErrorException();
        }
        //@会员处理并发送系统消息
        messageService.atDeal(loginMember.getId(),content, AppTag.CMS, MessageType.CMS_ARTICLE_COMMENT_REFER,articleComment.getId());
        messageService.diggDeal(loginMember.getId(),article.getMemberId(),content,AppTag.CMS,MessageType.CMS_ARTICLR_REPLY,article.getId());
        //文章评论奖励
        scoreDetailService.scoreBonus(loginMember.getId(), ScoreRuleConsts.ARTICLE_REVIEWS,articleComment.getId());
        return true;
    }

    public List listByPage(Page page, int articleId, String key) {
        if (StringUtils.isNotBlank(key)){
            key = "%"+key+"%";
        }
        List<ArticleComment> list = articleCommentDao.list(page, articleId, key);
        this.atFormat(list);
        return list;
    }

    public void deleteByArticle(Integer articleId) {
        articleCommentDao.deleteByArticle(articleId);
    }

    @Transactional
    public boolean delete(Member loginMember, int id) {
        boolean result = super.deleteById(id);
        if(!result){
            throw new OpeErrorException();
        }
        //扣除积分
        scoreDetailService.scoreCancelBonus(loginMember.getId(), ScoreRuleConsts.ARTICLE_REVIEWS,id);
        actionLogService.save(loginMember.getCurrLoginIp(),loginMember.getId(), ActionUtil.DELETE_ARTICLE_COMMENT,"ID："+id);
        return true;
    }

    public ArticleComment atFormat(ArticleComment articleComment){
        articleComment.setContent(memberService.atFormat(articleComment.getContent()));
        return articleComment;
    }

    public List<ArticleComment> atFormat(List<ArticleComment> articleCommentList){
        for (ArticleComment articleComment : articleCommentList){
            atFormat(articleComment);
        }
        return articleCommentList;
    }
}
