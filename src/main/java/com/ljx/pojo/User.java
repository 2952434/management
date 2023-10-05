package com.ljx.pojo;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
import java.util.List;
/**
 * 员工
 */
@Data
@Table(name="tb_user")
public class User {
    @Id
    @KeySql(useGeneratedKeys = true)
    @Excel(name = "编号", orderNum = "0", width = 5)
    private Long id;         //主键
    @Excel(name = "员工名", orderNum = "1", width = 15,isImportField="true")
    private String userName; //员工名
    @Excel(name = "手机号", orderNum = "2", width = 15,isImportField="true")
    private String phone;    //手机号
    @Excel(name = "省份名", orderNum = "3", width = 15,isImportField="true")
    private String province; //省份名
    @Excel(name = "城市名", orderNum = "4", width = 15,isImportField="true")
    private String city;     //城市名
    @Excel(name = "工资", orderNum = "5", width = 10, type=10, isImportField="true") //type=10表示会导出数字
    private Integer salary;   // 工资
    @JsonFormat(pattern="yyyy-MM-dd")
    @Excel(name = "入职日期",  format = "yyyy-MM-dd",orderNum = "6", width = 15,isImportField="true")
    private Date hireDate; // 入职日期
    private String deptId;   //部门id
    @Excel(name = "出生日期",  format = "yyyy-MM-dd",orderNum = "7", width = 15,isImportField="true")
    private Date birthday; //出生日期
    @Excel(name = "照片", orderNum = "10",width = 15,type = 2,isImportField="true",savePath = "src/main/resources/static/user_photos")
    private String photo;    //一寸照片
    @Excel(name = "现在居住地址", orderNum = "9", width = 30,isImportField="true")
    private String address;  //现在居住地址

    private List<Resource> resourceList; //办公用品

}
