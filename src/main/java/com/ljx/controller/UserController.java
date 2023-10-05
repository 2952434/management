package com.ljx.controller;

import com.ljx.pojo.User;
import com.ljx.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
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

    @GetMapping(value = "/downLoadXlsxByPoi", name = "使用POI下载高版本")
    public void downLoadXlsx(HttpServletRequest request, HttpServletResponse response) throws Exception {
//        userService.downLoadXlsx(response);
        userService.downLoadXlsxWithTempalte(request, response); //下载的excel带样式
    }

    @GetMapping(value = "/download", name = "导出用户详细信息")
    public void downLoadUserInfoWithTempalte(Long id, HttpServletRequest request, HttpServletResponse response) throws Exception {
//        userService.downLoadUserInfoWithTempalte(id, request, response);
        userService.downLoadUserInfoWithTempalte2(id, request, response);
    }

    @GetMapping(value = "/downLoadMillion", name = "导出用户百万数据的导出")
    public void downLoadMillion(Long id, HttpServletRequest request, HttpServletResponse response) throws Exception {
        userService.downLoadMillion(request, response);
    }

    @GetMapping(value = "/downLoadCSV", name = "导出用户数据到CSV文件中")
    public void downLoadCSV(HttpServletResponse response) {
        userService.downLoadCSV(response);
    }
}
