package com.ljx.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.ljx.mapper.UserMapper;
import com.ljx.pojo.User;
import org.apache.poi.ss.usermodel.*;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;


import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-ss");

    public List<User> findAll() {
        return userMapper.selectAll();
    }

    public List<User> findPage(Integer page, Integer pageSize) {
        PageHelper.startPage(page, pageSize);  //开启分页
        Page<User> userPage = (Page<User>) userMapper.selectAll(); //实现查询
        return userPage.getResult();
    }

    public void downLoadXlsByJxl(HttpServletResponse response) {
        try {
//            创建一个工作薄
            ServletOutputStream outputStream = response.getOutputStream();
            WritableWorkbook workbook = jxl.Workbook.createWorkbook(outputStream);
//            创建一个工作表
            WritableSheet sheet = workbook.createSheet("一个JXL入门", 0);
//            设置列宽
            sheet.setColumnView(0, 5);
            sheet.setColumnView(1, 8);
            sheet.setColumnView(2, 15);
            sheet.setColumnView(3, 15);
            sheet.setColumnView(4, 30);
//            处理标题
            String[] titles = new String[]{"编号", "姓名", "手机号", "入职日期", "现住址"};
            Label label = null;
            for (int i = 0; i < titles.length; i++) {
                label = new Label(i, 0, titles[i]);
                sheet.addCell(label);
            }
//            处理导出的内容
            List<User> userList = this.findAll();
            int rowIndex = 1;
            for (User user : userList) {
                label = new Label(0, rowIndex, user.getId().toString());
                sheet.addCell(label);
                label = new Label(1, rowIndex, user.getUserName());
                sheet.addCell(label);
                label = new Label(2, rowIndex, user.getPhone());
                sheet.addCell(label);
                label = new Label(3, rowIndex, SIMPLE_DATE_FORMAT.format(user.getHireDate()));
                sheet.addCell(label);
                label = new Label(4, rowIndex, user.getAddress());
                sheet.addCell(label);
                rowIndex++;
            }

            //            导出的文件名称
            String filename = "一个JXL入门.xls";
//            设置文件的打开方式和mime类型
            response.setHeader("Content-Disposition", "attachment;filename=" + new String(filename.getBytes(), "ISO8859-1"));
            response.setContentType("application/vnd.ms-excel");
//            导出
            workbook.write();
//            关闭资源
            workbook.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void uploadExcel(MultipartFile file) throws Exception {
        Workbook workbook = new XSSFWorkbook(file.getInputStream()); //根据上传的输入流创建workbook
        Sheet sheet = workbook.getSheetAt(0); //获取工作薄中的第一个工作表
        int lastRowIndex = sheet.getLastRowNum(); //获取这个sheet中最后一行数据，为了循环遍历

        //        以下三个为了节省栈内存，所以提到循环的外面
        User user = null;
        Row row = null;
        Cell cell = null;

        //开始循环每行，获取每行的单元格中的值，放入到user属性中
        for (int i = 1; i <= lastRowIndex; i++) {
            row = sheet.getRow(i);
            user = new User();
            //          因为第一个列单元格中是字符串，可以直接使用getStringCellValue方法
            String userName = row.getCell(0).getStringCellValue(); //用户名
            user.setUserName(userName);
            String phone = null; //手机号
            try {
                phone = row.getCell(1).getStringCellValue();
            } catch (IllegalStateException e) {
                phone = row.getCell(1).getNumericCellValue() + "";
            }
            user.setPhone(phone);
            String province = row.getCell(2).getStringCellValue(); //省份
            user.setProvince(province);
            String city = row.getCell(3).getStringCellValue(); //城市
            user.setCity(city);
            //            因为在填写excel中的数据时就可以约定这个列只能填写数值，所以可以直接用getNumericCellValue方法
            Integer salary = ((Double) row.getCell(4).getNumericCellValue()).intValue(); //工资
            user.setSalary(salary);
            String hireDateStr = row.getCell(5).getStringCellValue(); //入职日期
            Date hireDate = SIMPLE_DATE_FORMAT.parse(hireDateStr);
            user.setHireDate(hireDate);
            String birthdayStr = row.getCell(6).getStringCellValue(); //出生日期
            Date birthday = SIMPLE_DATE_FORMAT.parse(birthdayStr);
            user.setBirthday(birthday);

            String address = row.getCell(7).getStringCellValue(); //现住地址
            user.setAddress(address);
            userMapper.insert(user);
        }

    }

    public void downLoadXlsx(HttpServletResponse response) throws Exception {
        //        创建一个空的工作薄
        Workbook workbook = new XSSFWorkbook();
        //        在工作薄中创建一个工作表
        Sheet sheet = workbook.createSheet("测试");
        //        设置列宽
        sheet.setColumnWidth(0, 5 * 256);
        sheet.setColumnWidth(1, 8 * 256);
        sheet.setColumnWidth(2, 15 * 256);
        sheet.setColumnWidth(3, 15 * 256);
        sheet.setColumnWidth(4, 30 * 256);
        //            处理标题
        String[] titles = new String[]{"编号", "姓名", "手机号", "入职日期", "现住址"};

        //        创建标题行
        Row titleRow = sheet.createRow(0);
        Cell cell = null;
        for (int i = 0; i < titles.length; i++) {
            cell = titleRow.createCell(i);
            cell.setCellValue(titles[i]);
        }
        //        处理内容
        List<User> userList = this.findAll();
        int rowIndex = 1;
        Row row = null;
        for (User user : userList) {
            row = sheet.createRow(rowIndex);
            cell = row.createCell(0);
            cell.setCellValue(user.getId());

            cell = row.createCell(1);
            cell.setCellValue(user.getUserName());

            cell = row.createCell(2);
            cell.setCellValue(user.getPhone());

            cell = row.createCell(3);
            cell.setCellValue(SIMPLE_DATE_FORMAT.format(user.getHireDate()));

            cell = row.createCell(4);
            cell.setCellValue(user.getAddress());

            rowIndex++;
        }
        //            导出的文件名称
        String filename = "员工数据.xlsx";
        //            设置文件的打开方式和mime类型
        ServletOutputStream outputStream = response.getOutputStream();
        response.setHeader("Content-Disposition", "attachment;filename=" + new String(filename.getBytes(), "ISO8859-1"));
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        workbook.write(outputStream);

    }


    public void downLoadXlsxWithTempalte(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //        获取模板的路径
        File rootPath = new File(URLDecoder.decode(ResourceUtils.getURL("classpath:").getPath(), "utf-8")); //SpringBoot项目获取根目录的方式
        File templatePath = new File(rootPath.getAbsolutePath(), "/excel_template/userList.xlsx");
        //        读取模板文件产生workbook对象,这个workbook是一个有内容的工作薄
        Workbook workbook = new XSSFWorkbook(templatePath);
        //        读取工作薄的第一个工作表，向工作表中放数据
        Sheet sheet = workbook.getSheetAt(0);
        //        获取第二个的sheet中那个单元格中的单元格样式
        CellStyle cellStyle = workbook.getSheetAt(1).getRow(0).getCell(0).getCellStyle();
        //        处理内容
        List<User> userList = this.findAll();
        int rowIndex = 2;
        Row row = null;
        Cell cell = null;
        for (User user : userList) {
            row = sheet.createRow(rowIndex);
            row.setHeightInPoints(15); //设置行高

            cell = row.createCell(0);
            cell.setCellValue(user.getId());
            cell.setCellStyle(cellStyle); //设置单元格样式

            cell = row.createCell(1);
            cell.setCellValue(user.getUserName());
            cell.setCellStyle(cellStyle);

            cell = row.createCell(2);
            cell.setCellValue(user.getPhone());
            cell.setCellStyle(cellStyle);

            cell = row.createCell(3);
            cell.setCellValue(SIMPLE_DATE_FORMAT.format(user.getHireDate()));
            cell.setCellStyle(cellStyle);

            cell = row.createCell(4);
            cell.setCellValue(user.getAddress());
            cell.setCellStyle(cellStyle);

            rowIndex++;
        }
        //            导出的文件名称
        String filename = "用户列表数据.xlsx";
        //            设置文件的打开方式和mime类型
        ServletOutputStream outputStream = response.getOutputStream();
        response.setHeader("Content-Disposition", "attachment;filename=" + new String(filename.getBytes(), "ISO8859-1"));
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        workbook.write(outputStream);

    }
}
