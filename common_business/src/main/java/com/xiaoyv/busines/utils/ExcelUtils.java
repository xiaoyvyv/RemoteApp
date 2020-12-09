package com.xiaoyv.busines.utils;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.ArrayUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.xiaoyv.ui.listener.SimpleResultListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

/**
 * ExcelUtils
 *
 * @author why
 * @since 2020/12/09
 **/
public class ExcelUtils {

    /**
     * 从excel中读取数据
     */
    public static void readExcel(String path, SimpleResultListener<List<List<String>>> listener) {
        readExcel(new File(path), listener);
    }

    public static void readExcel(@NonNull File xlsFile, SimpleResultListener<List<List<String>>> listener) {
        ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<List<List<String>>>() {
            @Override
            public List<List<String>> doInBackground() {
                List<List<String>> map = new ArrayList<>();
                if (!FileUtils.isFileExists(xlsFile)) {
                    return map;
                }
                // 获得工作簿对象
                Workbook workbook;
                try {
                    workbook = Workbook.getWorkbook(xlsFile);
                } catch (IOException | BiffException e) {
                    e.printStackTrace();
                    listener.onResult(map);
                    return map;
                }

                // 获得所有工作表
                Sheet[] sheetNames = workbook.getSheets();
                if (ArrayUtils.isEmpty(sheetNames)) {
                    listener.onResult(map);
                    return map;
                }

                // 遍历工作表
                for (Sheet sheet : sheetNames) {
                    // 获得行数
                    int rows = sheet.getRows();
                    // 获得列数
                    int cols = sheet.getColumns();
                    // 读取数据
                    for (int row = 0; row < rows; row++) {
                        List<String> item = new ArrayList<>();
                        for (int col = 0; col < cols; col++) {
                            Cell cell = sheet.getCell(col, row);
                            item.add(cell.getContents());
                        }
                        map.add(item);
                    }
                }
                // 关闭资源
                workbook.close();
                return map;
            }

            @Override
            public void onSuccess(List<List<String>> result) {
                listener.onResult(result);
            }
        });
    }
}

