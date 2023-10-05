package com.ljx.demo;

import com.ljx.pojo.User;
import com.opencsv.CSVReader;

import java.io.FileReader;
import java.text.SimpleDateFormat;

//读取百万级数据的csv文件
public class CsvDemo {

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public static void main(String[] args) throws Exception {
        CSVReader csvReader = new CSVReader(new FileReader("d:\\百万用户数据的导出.csv"));
        String[] titles = csvReader.readNext(); //读取到第一行 是小标题
//        "编号","姓名","手机号","入职日期","现住址"
        User user = null;
        while (true){
            user = new User();
            String[] content = csvReader.readNext();
            if(content==null){
                break;
            }
            user.setId(Long.parseLong(content[0]));
            user.setUserName(content[1]);
            user.setPhone(content[2]);
            user.setHireDate(simpleDateFormat.parse(content[3]));
            user.setAddress(content[4]);
            System.out.println(user);
        }
    }
}
