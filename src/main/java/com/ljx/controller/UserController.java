package com.ljx.controller;

import com.ljx.pojo.User;
import com.ljx.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/findPage")
    public List<User> findPage(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "10") Integer pageSize) {
        return userService.findPage(page, pageSize);
    }

    @GetMapping("/downLoadXlsByJxl")
    public void downLoadXlsByJxl(HttpServletResponse response) {
        userService.downLoadXlsByJxl(response);
    }


    @PostMapping(value = "/uploadExcel", name = "上传用户数据")
    public void uploadExcel(MultipartFile file) throws Exception {
        userService.uploadExcel(file);
    }
}
