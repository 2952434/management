package com.ljx.demo;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import java.nio.file.Files;
import java.nio.file.Paths;

public class POIDemo01 {
    public static void main(String[] args) throws Exception{
        Workbook workbook = new HSSFWorkbook(); //创建了一个全新（里面什么都没有）的工作薄
        Sheet sheet = workbook.createSheet("demo测试");  //创建了一个全新（里面什么都没有）的工作表
        Row row = sheet.createRow(0);  //创建了第一行（空的）
        Cell cell = row.createCell(0);//创建的是第一行的第一个单元格
        cell.setCellValue("这是我第一次玩POI");
//        把工作薄输出到本地磁盘
        workbook.write(Files.newOutputStream(Paths.get("d://test.xls")));
    }
}
