package com.ljx.demo;

import com.ljx.pojo.User;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.usermodel.XSSFComment;

public class SheetHandler implements XSSFSheetXMLHandler.SheetContentsHandler {

//    编号 用户名  手机号  入职日期 现住址
    private User user=null;
    @Override
    public void startRow(int rowIndex) { //每一行的开始   rowIndex代表的是每一个sheet的行索引
        if(rowIndex==0){
            user = null;
        }else{
            user = new User();
        }
    }
    @Override  //处理每一行的所有单元格
    public void cell(String cellName, String cellValue, XSSFComment comment) {

        if(user!=null){
            String letter = cellName.substring(0, 1);  //每个单元名称的首字母 A  B  C
            switch (letter){
                case "A":{
                    user.setId(Long.parseLong(cellValue));
                    break;
                }
                case "B":{
                    user.setUserName(cellValue);
                    break;
                }
            }
        }
    }
    @Override
    public void endRow(int rowIndex) { //每一行的结束
        if(rowIndex!=0){
            System.out.println(user);
        }

    }
}
