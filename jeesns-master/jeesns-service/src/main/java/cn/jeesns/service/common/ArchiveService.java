package cn.jeesns.service.common;

import cn.jeesns.dao.common.IArchiveDao;
import cn.jeesns.model.common.Archive;
import cn.jeesns.core.dto.Result;
import cn.jeesns.core.utils.HtmlUtil;
import cn.jeesns.core.utils.StringUtils;
import cn.jeesns.model.member.Member;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;

/**
 * Created by zchuanzhao on 2016/10/14.
 */
@Service("archiveService")
public class ArchiveService {
    @Resource
    private IArchiveDao archiveDao;
    @Resource
    private ArchiveFavorService archiveFavorService;

    public Archive findByArchiveId(int id) {
        return archiveDao.findByArchiveId(id);
    }

    public boolean save(Member member, Archive archive) {
        archive.setMemberId(member.getId());
        if (archive.getViewCount() == null) {
            archive.setViewCount(0);
        }
        if (archive.getViewRank() == null) {
            archive.setViewRank(0);
        }
        if (StringUtils.isEmpty(archive.getDescription())) {
            String contentStr = HtmlUtil.delHTMLTag(archive.getContent());
            if (contentStr.length() > 200) {
                archive.setDescription(contentStr.substring(0, 200));
            } else {
                archive.setDescription(contentStr);
            }
        }
        if (StringUtils.isEmpty(archive.getThumbnail())) {
            Document doc = Jsoup.parseBodyFragment(archive.getContent());
            Elements elements = doc.select("img[src]");
            if (elements.size() > 0) {
                String imgsrc = elements.get(0).attr("src");
                archive.setThumbnail(imgsrc);
            }
        }
        return archiveDao.save(archive) == 1;
    }

    public void updateViewCount(int id) {
        archiveDao.updateViewCount(id);
    }

    @Transactional
    public Result favor(Member loginMember, int archiveId) {
        String message;
        Result<Integer> result;
        if(archiveFavorService.find(archiveId,loginMember.getId()) == null){
            //增加
            archiveDao.favor(archiveId,1);
            archiveFavorService.save(archiveId,loginMember.getId());
            message = "喜欢成功";
            result = new Result(0,message);
        }else {
            //减少
            archiveDao.favor(archiveId,-1);
            archiveFavorService.delete(archiveId,loginMember.getId());
            message = "取消喜欢成功";
            result = new Result(1,message);
        }
        Archive findArchive = this.findByArchiveId(archiveId);
        result.setData(findArchive.getFavor());
        return result;
    }

    public boolean update(Member member, Archive archive) {
        Archive findArchive = this.findByArchiveId(archive.getArchiveId());
        if (findArchive == null) {
            return false;
        }
        findArchive.setTitle(archive.getTitle());
        findArchive.setThumbnail(archive.getThumbnail());
        findArchive.setContent(archive.getContent());
        findArchive.setDescription(archive.getDescription());
        findArchive.setKeywords(archive.getKeywords());
        //普通会员
        if (member.getIsAdmin() == 0) {
            if (member.getId().intValue() != findArchive.getMemberId().intValue()) {
                return false;
            }
        } else {
            //管理员
            findArchive.setSource(archive.getSource());
            findArchive.setViewCount(archive.getViewCount());
            findArchive.setWriter(archive.getWriter());
            findArchive.setViewRank(archive.getViewRank());
        }
        if (findArchive.getViewCount() == null) {
            findArchive.setViewCount(0);
        }
        if (findArchive.getViewRank() == null) {
            findArchive.setViewRank(0);
        }
        if (StringUtils.isEmpty(findArchive.getDescription())) {
            String contentStr = HtmlUtil.delHTMLTag(findArchive.getContent());
            if (contentStr.length() > 200) {
                findArchive.setDescription(contentStr.substring(0, 200));
            } else {
                findArchive.setDescription(contentStr);
            }
        }
        if (StringUtils.isEmpty(findArchive.getThumbnail())) {
            Document doc = Jsoup.parseBodyFragment(findArchive.getContent());
            Elements elements = doc.select("img[src]");
            if (elements.size() > 0) {
                String imgsrc = elements.get(0).attr("src");
                findArchive.setThumbnail(imgsrc);
            }
        }
        return archiveDao.update(findArchive) == 1;
    }

    @Transactional
    public boolean delete(int id) {
        return archiveDao.delete(id) == 1;
    }

}
