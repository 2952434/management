package com.ljx.demo;

import org.apache.poi.xwpf.usermodel.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * @Author: Ljx
 * @Date: 2023/10/5 21:07
 * @role:
 */
public class WordDemo {
    public static void main(String[] args) throws Exception {

        XWPFDocument document = new XWPFDocument(new FileInputStream("E:\\学习\\Java\\编程强化\\Java报表数据可视化过程\\day03\\资料\\test.docx"));
//        读取正文
        List<XWPFParagraph> paragraphs = document.getParagraphs();
        for (XWPFParagraph paragraph : paragraphs) {
            System.out.println(paragraph.getText());
            List<XWPFRun> runs = paragraph.getRuns();
            for (XWPFRun run : runs) {
                System.out.println(run.getText(0));
            }
        }

//        读取表格
        XWPFTable xwpfTable = document.getTables().get(0);
        List<XWPFTableRow> rows = xwpfTable.getRows();
        for (XWPFTableRow row : rows) {
            List<XWPFTableCell> tableCells = row.getTableCells();
            for (XWPFTableCell tableCell : tableCells) {
                List<XWPFParagraph> paragraphs1 = tableCell.getParagraphs();
                for (XWPFParagraph xwpfParagraph : paragraphs1) {
                    System.out.println(xwpfParagraph.getText());
                }
            }
        }
    }
}
