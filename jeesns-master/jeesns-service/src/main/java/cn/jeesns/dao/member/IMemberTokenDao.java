package cn.jeesns.dao.member;

import cn.jeesns.model.member.MemberToken;
import cn.jeesns.core.dao.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

/**
 * Created by zchuanzhao on 2017/7/15.
 */
@Mapper
public interface IMemberTokenDao extends BaseMapper<MemberToken> {

    MemberToken getByToken(@Param("token") String token);

    Integer save(@Param("memberId") Integer memberId, @Param("token") String token, @Param("expireTime") Date expireTime);

}