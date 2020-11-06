package cn.itrip.controller;

import cn.itrip.dao.itripHotel.ItripHotelMapper;
import cn.itrip.dao.itripUser.ItripUserMapper;
import cn.itrip.pojo.ItripUser;
import com.alibaba.fastjson.JSONArray;
import cz.mallat.uasparser.UserAgentInfo;
import itrip.common.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Controller
public class HotelController {

    @Resource
    ItripUserMapper userdao;

    @Resource
    JredisApi jredisApi;

    @RequestMapping("/api/dologin")
    @ResponseBody
    public Dto dologin(String name, String password,HttpServletRequest request) throws Exception {//name和password为客户端请求传入的值
        //去数据库查询数据
        String pas= MD5.getMd5(password,32);
        System.out.println(pas);
        ItripUser user=userdao.dologin(name, MD5.getMd5(password,32));
        if (user!=null){
            //存入session,用redis替换
            //redis 中的Key=token，value=用户实体
            String token=generateToken(request.getHeader("User-Agent"),user);
            String value= JSONArray.toJSONString(user);
            if (jredisApi.getRedis(token)==null) {
                jredisApi.SetRedis(token, value,60*60*2);
            }
        //返回数据的实体类
            ItripTokenVO tokenVO=new ItripTokenVO(token, Calendar.getInstance().getTimeInMillis()+7200,Calendar.getInstance().getTimeInMillis());
            return DtoUtil.returnDataSuccess(tokenVO);
        }
        return DtoUtil.returnDataSuccess("登录失败");
    }

    public String generateToken(String agent, ItripUser user) {
        // TODO Auto-generated method stub
        try {
            UserAgentInfo userAgentInfo = UserAgentUtil.getUasParser().parse(
                    agent);
            StringBuilder sb = new StringBuilder();
            sb.append("token:");//统一前缀
            if (userAgentInfo.getDeviceType().equals(UserAgentInfo.UNKNOWN)) {
                if (UserAgentUtil.CheckAgent(agent)) {
                    sb.append("MOBILE-");
                } else {
                    sb.append("PC-");
                }
            } else if (userAgentInfo.getDeviceType()
                    .equals("Personal computer")) {
                sb.append("PC-");
            } else
                sb.append("MOBILE-");
//			sb.append(user.getUserCode() + "-");
            sb.append(MD5.getMd5(user.getUserCode(),32) + "-");//加密用户名称
            sb.append(user.getId() + "-");
            sb.append(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
                    + "-");
            sb.append(MD5.getMd5(agent, 6));// 识别客户端的简化实现——6位MD5码

            return sb.toString();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    @Resource
    ItripHotelMapper dao;

    @RequestMapping("/list")
    @ResponseBody
    public Object getlist() throws Exception {
        return dao.getItripHotelById(new Long(1));
    }
}
