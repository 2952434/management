package com.ljx.service;

import cn.afterturn.easypoi.csv.CsvExportUtil;
import cn.afterturn.easypoi.csv.CsvImportUtil;
import cn.afterturn.easypoi.csv.entity.CsvExportParams;
import cn.afterturn.easypoi.entity.ImageEntity;
import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.afterturn.easypoi.excel.entity.TemplateExportParams;
import cn.afterturn.easypoi.excel.entity.enmus.ExcelType;
import cn.afterturn.easypoi.word.WordExportUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.ljx.mapper.ResourceMapper;
import com.ljx.mapper.UserMapper;
import com.ljx.pojo.Resource;
import com.ljx.pojo.User;
import com.ljx.utils.EntityUtils;
import com.ljx.utils.ExcelExportEngine;
import com.opencsv.CSVWriter;
import org.apache.poi.ss.usermodel.*;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;


import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStreamWriter;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ResourceMapper resourceMapper;

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


    public void downLoadUserInfoWithTempalte(Long id, HttpServletRequest request, HttpServletResponse response) throws Exception {
        //        获取模板的路径
        File rootPath = new File(URLDecoder.decode(ResourceUtils.getURL("classpath:").getPath(), "utf-8")); //SpringBoot项目获取根目录的方式
        File templatePath = new File(rootPath.getAbsolutePath(), "/excel_template/userInfo.xlsx");
//        读取模板文件产生workbook对象,这个workbook是一个有内容的工作薄
        Workbook workbook = new XSSFWorkbook(templatePath);
//        读取工作薄的第一个工作表，向工作表中放数据
        Sheet sheet = workbook.getSheetAt(0);
//        处理内容
        User user = userMapper.selectByPrimaryKey(id);
//        接下来向模板中单元格中放数据
//        用户名   第2行第2列
        sheet.getRow(1).getCell(1).setCellValue(user.getUserName());
//        手机号   第3行第2列
        sheet.getRow(2).getCell(1).setCellValue(user.getPhone());
//        生日     第4行第2列  日期转成字符串
        sheet.getRow(3).getCell(1).setCellValue
                (SIMPLE_DATE_FORMAT.format(user.getBirthday()));
//        工资 第5行第2列
        sheet.getRow(4).getCell(1).setCellValue(user.getSalary());
//        工资 第6行第2列
        sheet.getRow(5).getCell(1).setCellValue
                (SIMPLE_DATE_FORMAT.format(user.getHireDate()));
//        省份     第7行第2列
        sheet.getRow(6).getCell(1).setCellValue(user.getProvince());
//        现住址   第8行第2列
        sheet.getRow(7).getCell(1).setCellValue(user.getAddress());
//        司龄     第6行第4列暂时先不考虑
        sheet.getRow(5).getCell(3).setCellFormula("CONCATENATE(DATEDIF(B6,TODAY(),\"Y\"),\"年\",DATEDIF(B6,TODAY(),\"YM\"),\"个月\")");
//        城市     第7行第4列
        sheet.getRow(6).getCell(3).setCellValue(user.getCity());

//        处理图片
//        先创建一个字节输出流
        ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
//        BufferedImage是一个带缓冲区图像类,主要作用是将一幅图片加载到内存中
        BufferedImage bufferImg = ImageIO.read(new File(rootPath + user.getPhoto()));
//        把读取到图像放入到输出流中
        String[] split = user.getPhoto().split("\\.");
        String extName = split[split.length - 1].toUpperCase();
        ImageIO.write(bufferImg, extName, byteArrayOut);
//        创建一个绘图控制类，负责画图
        Drawing patriarch = sheet.createDrawingPatriarch();
//        指定图片的位置   开始列3开始行2结束列4结束行5
//        偏移的单位:是一个英式公制的单位1厘米=360000
        ClientAnchor anchor = new XSSFClientAnchor(0, 0, 0, 0, 2, 1, 4, 5);
        int format = 0;
        switch (extName) {
            case "JPG": {
                format = Workbook.PICTURE_TYPE_JPEG;
            }
            case "JPEG": {
                format = Workbook.PICTURE_TYPE_JPEG;
            }
            case "PNG": {
                format = Workbook.PICTURE_TYPE_PNG;
            }
        }
//        开始把图片写入到sheet指定的位置
        patriarch.createPicture(anchor, workbook.addPicture(
                byteArrayOut.toByteArray(), format));
//        导出的文件名称
        String filename = "用户详细信息数据.xlsx";
//        设置文件的打开方式和mime类型
        ServletOutputStream outputStream = response.getOutputStream();
        response.setHeader("Content-Disposition", "attachment;filename=" + new String(filename.getBytes(), "ISO8859-1"));
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        workbook.write(outputStream);
    }


    public void downLoadUserInfoWithTempalte2(Long id, HttpServletRequest request, HttpServletResponse response) throws Exception {
        //        获取模板的路径
        File rootPath = new File(URLDecoder.decode(ResourceUtils.getURL("classpath:").getPath(), "utf-8")); //SpringBoot项目获取根目录的方式
        File templatePath = new File(rootPath.getAbsolutePath(), "/excel_template/userInfo2.xlsx");
        //        读取模板文件产生workbook对象,这个workbook是一个有内容的工作薄
        Workbook workbook = new XSSFWorkbook(templatePath);
        // 查询用户信息
        User user = userMapper.selectByPrimaryKey(id);
        // 这里使用引擎直接导出
        ExcelExportEngine.writeToExcel(user, workbook, user.getPhoto());
        //            导出的文件名称
        String filename = "用户详细信息数据.xlsx";
        //            设置文件的打开方式和mime类型
        ServletOutputStream outputStream = response.getOutputStream();
        response.setHeader("Content-Disposition", "attachment;filename=" + new String(filename.getBytes(), "ISO8859-1"));
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        workbook.write(outputStream);
    }

    public void downLoadMillion(HttpServletRequest request, HttpServletResponse response) throws Exception {
//        创建一个空的工作薄
        Workbook workbook = new SXSSFWorkbook();
        int page = 1;
        int pageSize = 200000;
        int rowIndex = 1; //每一个工作页的行数
        int num = 0; //总数据量
        Row row;
        Cell cell;
        Sheet sheet = null;
        while (true) {  //不停地查询
            List<User> userList = this.findPage(page, pageSize);
            if (CollectionUtils.isEmpty(userList)) {  //如果查询不到就不再查询了
                break;
            }
            if (num % 1000000 == 0) {  //每100W个就重新创建新的sheet和标题
                rowIndex = 1;
                //        在工作薄中创建一个工作表
                sheet = workbook.createSheet("第" + ((num / 1000000) + 1) + "个工作表");
//        设置列宽
                sheet.setColumnWidth(0, 8 * 256);
                sheet.setColumnWidth(1, 12 * 256);
                sheet.setColumnWidth(2, 15 * 256);
                sheet.setColumnWidth(3, 15 * 256);
                sheet.setColumnWidth(4, 30 * 256);
                //            处理标题
                String[] titles = new String[]{"编号", "姓名", "手机号", "入职日期", "现住址"};
                //        创建标题行
                Row titleRow = sheet.createRow(0);

                for (int i = 0; i < titles.length; i++) {
                    cell = titleRow.createCell(i);
                    cell.setCellValue(titles[i]);
                }
            }

//        处理内容

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
                num++;
            }
            page++;// 继续查询下一页
        }
//            导出的文件名称
        String filename = "百万数据.xlsx";
//            设置文件的打开方式和mime类型
        ServletOutputStream outputStream = response.getOutputStream();
        response.setHeader("Content-Disposition", "attachment;filename=" + new String(filename.getBytes(), "ISO8859-1"));
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        workbook.write(outputStream);
    }

    public void downLoadCSV(HttpServletResponse response) {
        try {
            //            准备输出流
            ServletOutputStream outputStream = response.getOutputStream();
            //            文件名
            String filename = "百万数据.csv";
            //            设置两个头 一个是文件的打开方式 一个是mime类型
            response.setHeader("Content-Disposition", "attachment;filename=" + new String(filename.getBytes(), "ISO8859-1"));
            response.setContentType("text/csv");
            //            创建一个用来写入到csv文件中的writer
            CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream, "utf-8"));
            //            先写头信息
            writer.writeNext(new String[]{"编号", "姓名", "手机号", "入职日期", "现住址"});

            //            如果文件数据量非常大的时候，我们可以循环查询写入
            int page = 1;
            int pageSize = 200000;
            while (true) {  //不停地查询
                List<User> userList = this.findPage(page, pageSize);
                if (CollectionUtils.isEmpty(userList)) {  //如果查询不到就不再查询了
                    break;
                }
                //                把查询到的数据转成数组放入到csv文件中
                for (User user : userList) {
                    writer.writeNext(new String[]{user.getId().toString(), user.getUserName(), user.getPhone(), SIMPLE_DATE_FORMAT.format(user.getHireDate()), user.getAddress()});
                }
                writer.flush();
                page++;
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public User findById(Long id) {
        //查询用户
        User user = userMapper.selectByPrimaryKey(id);
        //根据用户id查询办公用品
        Resource resource = new Resource();
        resource.setUserId(id);
        List<Resource> resourceList = resourceMapper.select(resource);
        user.setResourceList(resourceList);
        return user;
    }


    //    向单元格中写入图片
    private void setCellImage(XWPFTableCell cell, File imageFile) {

        XWPFRun run = cell.getParagraphs().get(0).createRun();
        //        InputStream pictureData, int pictureType, String filename, int width, int height
        try (FileInputStream inputStream = new FileInputStream(imageFile)) {
            run.addPicture(inputStream, XWPFDocument.PICTURE_TYPE_JPEG, imageFile.getName(), Units.toEMU(100), Units.toEMU(50));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //    用于深克隆行
    private void copyRow(XWPFTable xwpfTable, XWPFTableRow sourceRow, int rowIndex) {
        XWPFTableRow targetRow = xwpfTable.insertNewTableRow(rowIndex);
        targetRow.getCtRow().setTrPr(sourceRow.getCtRow().getTrPr());
        //        获取源行的单元格
        List<XWPFTableCell> cells = sourceRow.getTableCells();
        if (CollectionUtils.isEmpty(cells)) {
            return;
        }
        XWPFTableCell targetCell = null;
        for (XWPFTableCell cell : cells) {
            targetCell = targetRow.addNewTableCell();
            //            附上单元格的样式
            //            单元格的属性
            targetCell.getCTTc().setTcPr(cell.getCTTc().getTcPr());
            targetCell.getParagraphs().get(0).getCTP().setPPr(cell.getParagraphs().get(0).getCTP().getPPr());
        }
    }

    /**
     * 下载用户合同数据
     *
     * @param id
     */
    public void downloadContract(Long id, HttpServletResponse response) throws Exception {
        //        1、读取到模板
        File rootFile = new File(URLDecoder.decode(ResourceUtils.getURL("classpath:").getPath(), "utf-8")); //SpringBoot项目获取根目录的方式
        File templateFile = new File(rootFile, "/word_template/contract_template.docx");
        XWPFDocument word = new XWPFDocument(new FileInputStream(templateFile));
        //        2、查询当前用户User--->map
        User user = this.findById(id);
        Map<String, String> params = new HashMap<>();
        params.put("userName", user.getUserName());
        params.put("hireDate", SIMPLE_DATE_FORMAT.format(user.getHireDate()));
        params.put("address", user.getAddress());
        //        3、替换数据
        //         处理正文开始
        List<XWPFParagraph> paragraphs = word.getParagraphs();
        for (XWPFParagraph paragraph : paragraphs) {
            List<XWPFRun> runs = paragraph.getRuns();
            for (XWPFRun run : runs) {
                String text = run.getText(0);
                for (String key : params.keySet()) {
                    if (text.contains(key)) {
                        run.setText(text.replaceAll(key, params.get(key)), 0);
                    }
                }
            }
        }
        //         处理正文结束

        //      处理表格开始     名称	价值	是否需要归还	照片
        List<Resource> resourceList = user.getResourceList(); //表格中需要的数据
        XWPFTable xwpfTable = word.getTables().get(0);

        XWPFTableRow row = xwpfTable.getRow(0);
        int rowIndex = 1;
        for (Resource resource : resourceList) {
            //        添加行
            //            xwpfTable.addRow(row);
            copyRow(xwpfTable, row, rowIndex);
            XWPFTableRow row1 = xwpfTable.getRow(rowIndex);
            row1.getCell(0).setText(resource.getName());
            row1.getCell(1).setText(resource.getPrice().toString());
            row1.getCell(2).setText(resource.getNeedReturn() ? "需求" : "不需要");

            File imageFile = new File(rootFile, "/static" + resource.getPhoto());
            setCellImage(row1.getCell(3), imageFile);
            rowIndex++;
        }
        //     处理表格开始结束
        //        4、导出word
        String filename = "员工(" + user.getUserName() + ")合同.docx";
        response.setHeader("content-disposition", "attachment;filename=" + new String(filename.getBytes(), "ISO8859-1"));
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        word.write(response.getOutputStream());
    }

    public void downLoadXlsxWithEayPoi(HttpServletResponse response) throws Exception {
        //        查询用户数据
        List<User> userList = userMapper.selectAll();
        //指定导出的格式是高版本的格式
        ExportParams exportParams = new ExportParams("员工信息", "数据", ExcelType.XSSF);
        //        直接使用EasyPOI提供的方法
        Workbook workbook = ExcelExportUtil.exportExcel(exportParams, User.class, userList);
        String filename = "员工信息.xlsx";
        //            设置文件的打开方式和mime类型
        ServletOutputStream outputStream = response.getOutputStream();
        response.setHeader("Content-Disposition", "attachment;filename=" + new String(filename.getBytes(), "ISO8859-1"));
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        workbook.write(outputStream);
    }

    public void uploadExcelWithEasyPOI(MultipartFile file) throws Exception {

        ImportParams importParams = new ImportParams();
        importParams.setTitleRows(1); //有多少行的标题
        importParams.setHeadRows(1);//有多少行的头
        List<User> userList = ExcelImportUtil.importExcel(file.getInputStream(), User.class, importParams);

        System.out.println(userList);
        for (User user : userList) {
            user.setId(null);
            userMapper.insertSelective(user);
        }
    }

    public void downLoadUserInfoWithEastPOI(Long id, HttpServletRequest request, HttpServletResponse response) throws Exception {
        //        获取模板的路径
        File rootPath = new File(URLDecoder.decode(ResourceUtils.getURL("classpath:").getPath(), "utf-8")); //SpringBoot项目获取根目录的方式
        File templatePath = new File(rootPath.getAbsolutePath(), "/excel_template/userInfo3.xlsx");
        //        读取模板文件
        TemplateExportParams params = new TemplateExportParams(templatePath.getPath(), true);

        //        查询用户，转成map
        User user = userMapper.selectByPrimaryKey(id);
        Map<String, Object> map = EntityUtils.entityToMap(user);
        ImageEntity image = new ImageEntity();
        //        image.setHeight(640); //测试发现 这里设置了长度和宽度在合并后的单元格中没有作用
        //        image.setWidth(380);
        image.setRowspan(4);//向下合并三行
        image.setColspan(2);//向右合并两列
        image.setUrl(user.getPhoto());
        map.put("photo", image);
        Workbook workbook = ExcelExportUtil.exportExcel(params, map);

        //            导出的文件名称
        String filename = "用户详细信息数据.xlsx";
        //            设置文件的打开方式和mime类型
        ServletOutputStream outputStream = response.getOutputStream();
        response.setHeader("Content-Disposition", "attachment;filename=" + new String(filename.getBytes(), "ISO8859-1"));
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        workbook.write(outputStream);
    }

    public void downLoadCSVWithEasyPOI(HttpServletResponse response) throws Exception {
        ServletOutputStream outputStream = response.getOutputStream();
//        文件名
        String filename = "百万数据.csv";
//        设置两个头 一个是文件的打开方式 一个是mime类型
        response.setHeader("Content-Disposition", "attachment;filename=" + new String(filename.getBytes(), "ISO8859-1"));
        response.setContentType("application/csv");
//        创建一个用来写入到csv文件中的writer
        CsvExportParams params = new CsvExportParams();
//        设置忽略的列
        params.setExclusions(new String[]{"照片"}); //这里写表头 中文
        List<User> list = userMapper.selectAll();
        CsvExportUtil.exportCsv(params, User.class, list, outputStream);
    }

    public void downloadContractWithEasyPOI(Long id, HttpServletResponse response) throws Exception {

        File rootPath = new File(URLDecoder.decode(ResourceUtils.getURL("classpath:").getPath(), "utf-8")); //SpringBoot项目获取根目录的方式
        File templatePath = new File(rootPath.getAbsolutePath(), "/word_template/contract_template2.docx");

        //        先获取导出word需要的数据
        User user = this.findById(id);
        //        把需要的数据放到map中，方便替换
        Map<String, Object> params = new HashMap<>();
        params.put("userName", user.getUserName());
        params.put("hireDate", SIMPLE_DATE_FORMAT.format(user.getHireDate()));
        params.put("address", user.getAddress());

        //        下面是表格中需要的数据
        List<Map<String, Object>> maplist = new ArrayList<>();
        Map<String, Object> map;
        for (Resource resource : user.getResourceList()) {
            map = new HashMap<>();
            map.put("name", resource.getName());
            map.put("price", resource.getPrice());
            map.put("needReturn", resource.getNeedReturn());
            ImageEntity image = new ImageEntity();
            image.setHeight(180);
            image.setWidth(240);
            image.setUrl(rootPath.getPath() + "\\static" + resource.getPhoto());
            map.put("photo", image);
            maplist.add(map);
        }
        //        把组建好的表格需要的数据放到大map中
        params.put("maplist", maplist);
        //        根据模板+数据 导出文档
        XWPFDocument xwpfDocument = WordExportUtil.exportWord07(templatePath.getPath(), params);
        String filename = user.getUserName() + "_合同.docx";
        //            设置文件的打开方式和mime类型
        ServletOutputStream outputStream = response.getOutputStream();
        response.setHeader("Content-Disposition", "attachment;filename=" + new String(filename.getBytes(), "ISO8859-1"));
        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        xwpfDocument.write(outputStream);
    }
}
