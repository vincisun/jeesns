package cn.jeesns.dao.shop;

import cn.jeesns.model.shop.GoodsCate;
import cn.jeesns.core.dao.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品分类
 * Created by zchuanzhao on 2019/5/15.
 */
@Mapper
public interface IGoodsCateDao extends BaseMapper<GoodsCate> {

    /**
     * 获取分类
     * @return
     */
    List<GoodsCate> list();

    /**
     * 获取顶级分类
     * @return
     */
    List<GoodsCate> topList();

    /**
     * 获取第二级分类
     * @return
     */
    List<GoodsCate> sonList();

    /**
     * 通过父类ID获取子类列表
     * @param fid
     * @return
     */
    List<GoodsCate> findListByFid(@Param("fid") int fid);



}