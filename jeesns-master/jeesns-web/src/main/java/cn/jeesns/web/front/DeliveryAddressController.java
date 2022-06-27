package cn.jeesns.web.front;

import cn.jeesns.model.member.DeliveryAddress;
import cn.jeesns.core.annotation.Before;
import cn.jeesns.core.controller.BaseController;
import cn.jeesns.core.dto.Result;
import cn.jeesns.core.exception.OpeErrorException;
import cn.jeesns.interceptor.UserLoginInterceptor;
import cn.jeesns.model.member.Member;
import cn.jeesns.service.member.DeliveryAddressService;
import cn.jeesns.utils.MemberUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;


/**
 * 收货地址
 * Created by zchuanzhao on 2019/5/16.
 */
@Controller("frontDeliveryAddressController")
@RequestMapping("/member/deliveryAddress/")
@Before(UserLoginInterceptor.class)
public class DeliveryAddressController extends BaseController {
    private static final String MEMBER_FTL_PATH = "/member/deliveryAddress/";
    @Resource
    private DeliveryAddressService deliveryAddressService;

    @GetMapping("list")
    public String list(Model model){
        Member loginMember = MemberUtil.getLoginMember(request);
        List<DeliveryAddress> deliveryAddressList = deliveryAddressService.listByMemberId(loginMember.getId());
        model.addAttribute("deliveryAddressList", deliveryAddressList);
        return MEMBER_FTL_PATH + "list";
    }


    @GetMapping("add")
    public String add(Model model){
        return MEMBER_FTL_PATH + "add";
    }

    @PostMapping("save")
    @ResponseBody
    public Result save(@Validated DeliveryAddress deliveryAddress){
        if (deliveryAddress.getIsDefault() == null){
            deliveryAddress.setIsDefault(0);
        }
        Member loginMember = MemberUtil.getLoginMember(request);
        deliveryAddress.setMemberId(loginMember.getId());
        boolean result = deliveryAddressService.save(deliveryAddress);
        return new Result(result);
    }

    @GetMapping("edit/{id}")
    public String edit(@PathVariable("id") Integer id, Model model){
        Member loginMember = MemberUtil.getLoginMember(request);
        DeliveryAddress deliveryAddress = deliveryAddressService.findById(id);
        if (deliveryAddress != null && deliveryAddress.getMemberId().intValue() == loginMember.getId().intValue()){
            model.addAttribute("deliveryAddress", deliveryAddress);
        }
        return MEMBER_FTL_PATH + "edit";
    }

    @PostMapping("update")
    @ResponseBody
    public Result update(@Valid DeliveryAddress deliveryAddress){
        Member loginMember = MemberUtil.getLoginMember(request);
        DeliveryAddress findDeliveryAddress = deliveryAddressService.findById(deliveryAddress.getId());
        if (findDeliveryAddress.getMemberId().intValue() != loginMember.getId().intValue()){
            throw new OpeErrorException("error");
        }
        if (deliveryAddress.getIsDefault() == null){
            deliveryAddress.setIsDefault(0);
        }
        boolean result = deliveryAddressService.update(deliveryAddress);
        return new Result(result);
    }


    @GetMapping("delete/{id}")
    @ResponseBody
    public Result delete(@PathVariable("id") Integer id){
        Member loginMember = MemberUtil.getLoginMember(request);
        DeliveryAddress findDeliveryAddress = deliveryAddressService.findById(id);
        if (findDeliveryAddress.getMemberId().intValue() != loginMember.getId().intValue()){
            throw new OpeErrorException("error");
        }
        deliveryAddressService.delete(findDeliveryAddress);
        return new Result(0);
    }
}
