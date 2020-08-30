package com.dabaoyutech.lingshou.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dabaoyutech.lingshou.model.User;
import com.dabaoyutech.lingshou.util.CookiesPrivateUtils;
import com.dabaoyutech.lingshou.util.TokenPrivateUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.util.Objects;

@RestController
public class GetShangPinImgController {

    @RequestMapping(value = "/GetShangPinImg", method = {RequestMethod.POST})
    public JSONObject GetShangPinImg(@RequestBody JSONObject requestBody, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        //定义输出信息
        JSONObject Return = new JSONObject();


        TokenPrivateUtils TokenPrivateUtils = new TokenPrivateUtils();
//        生成Token
        JSONObject payloadObject = new JSONObject();
        payloadObject.put("username", "2");
        payloadObject.put("code", "2");
        payloadObject.put("time", String.valueOf(System.currentTimeMillis()));
        String Token = TokenPrivateUtils.SetTokenSession(payloadObject,session);

        CookiesPrivateUtils CookiesPrivateUtils = new CookiesPrivateUtils();
////        设置Cookies值
        JSONObject SetCookiesJSONObject = CookiesPrivateUtils.SetCookies(response,request, "token", Token);
        if (!SetCookiesJSONObject.getString("errcode").equals("0")) {
            return SetCookiesJSONObject;
        }
//        取Cookies值
        JSONObject GetCookiesJSONObject = CookiesPrivateUtils.GetCookies(request);
        if (!GetCookiesJSONObject.getString("errcode").equals("0")) {
            return GetCookiesJSONObject;
        }
        JSONObject GetCookiesJSONObjectMsg = JSONObject.parseObject(GetCookiesJSONObject.getString("msg"));
        String Cookietoken = GetCookiesJSONObjectMsg.getString("token");

        //        验证Token
        JSONObject TokenGetReturn = TokenPrivateUtils.GetTokenSession(Cookietoken,session);
        if (!TokenGetReturn.getString("errcode").equals("0")) {
            System.out.println("GetShangPinImg,Token3错误：" + TokenGetReturn.getString("msg"));
        } else {
            System.out.println("GetShangPinImg,Token2成功,返回String型json格式payload中间信息：" + TokenGetReturn.getString("msg"));
        }

        //接收请求参数中的token
        String token = requestBody.getString("token");

        //判空token
        if (StringUtils.isBlank(token)) {
            Return.put("errcode", "1");
            Return.put("msg", "请求参数不合法");
            return Return;
        }

//        //校验token是否正确
//        String adminToken = (String) session.getAttribute("adminToken");
//
//        //判空adminToken
//        if (StringUtils.isBlank(adminToken)) {
//            Return.put("errcode", "1");
//            Return.put("msg", "请先登录");
//            return Return;
//        }
//
//        if (!adminToken.equals(token)) {
//            Return.put("errcode", "1");
//            Return.put("msg", "token校验失败");
//            return Return;
//        }

        //验证session是否存在shanghuid
        String shanghuid = ObjectUtils.toString(session.getAttribute("shanghuid"), "");
        if (StringUtils.isBlank(shanghuid)) {
            Return.put("errcode", "1");
            Return.put("msg", "请先登录");
            return Return;
        }
//        验证是否与token池里面的匹配
        if (!User.token.get(Integer.valueOf(shanghuid)).equals(token)) {
            Return.put("errcode", "1");
            Return.put("msg", "token校验失败");
            return Return;
        }


//        传值type
        String type = requestBody.getString("type");
        System.out.println("GetShangPinImg,接收,type(类型)：" + type);

//        接收空时，防止下面switch报错
        if (type == null) {
            type = "";
        }

//        成功返回
        Return.put("errcode", "0");
        Return.put("msg", "");
        Return.put("imgList", ShaoMiao(type));

        return Return;
    }

    //    扫描本地文件=>返回JSONArray
    private JSONArray ShaoMiao(String type) {
//        type=需要扫描的目录名

//        定义目录名
        String path = null;
        if (StringUtils.isBlank(type)) {
            path = "../webapps/public/static/img/";
        } else {
            path = "../webapps/public/static/img/" + type + "/";
        }
//        System.out.println("ShaoMiao,扫描,path：" + path);

//        返回信息=>获取扫描文件=>返回JSONArray类型
        return Getfilepath(path, new JSONArray());
    }

    //    扫描目录
    private JSONArray Getfilepath(String path, JSONArray Return) {
        File rootDir = new File(path);
//        检查一个是否是文件夹
        if (rootDir.getName().split("\\.").length != 1) {
            JSONObject a = new JSONObject();
            a.put("name", rootDir.getName());
            String[] urlArr = rootDir.getPath().split("../webapps/");
            if (urlArr.length != 1) {
                a.put("url", urlArr[1]);
            } else {
                a.put("url", "");
            }
            Return.add(a);
        } else {
            String[] fileList = rootDir.list();
            for (int i = 0; i < Objects.requireNonNull(fileList).length; i++) {
                path = rootDir.getAbsolutePath() + "/" + fileList[i];
                Getfilepath(path, Return);
            }
        }
        return Return;
    }
}
