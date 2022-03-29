package com.rain.mall.user;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
* @author zy
* @Package com.rain.mall.user
* @ClassName UserCenterController
* @Description 个人中心
* @Date 2022-03-29 17:49
*/
@RestController
@RequestMapping(value = "/userCenter", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserCenterController {


    @GetMapping("/test")
    public String listAuctionsForApp() {
        return "111";
    }
}
