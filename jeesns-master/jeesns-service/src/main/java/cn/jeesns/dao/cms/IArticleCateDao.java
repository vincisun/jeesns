package cn.jeesns.dao.cms;

import cn.jeesns.core.dao.BaseMapper;
import cn.jeesns.model.cms.ArticleCate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文章栏目DAO接口
 * Created by zchuanzhao on 2016/11/26.
 */
@Mapper
public interface IArticleCateDao extends BaseMapper<ArticleCate> {

    /**
     * 获取栏目
     * @return
     */
    List<ArticleCate> list();

    /**
     * 通过父类ID获取子类列表
     * @param fid
     * @return
     */
    List<ArticleCate> findListByFid(@Param("fid") int fid);

}