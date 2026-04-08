package com.yupi.yuaiagent.demo.invoke;

import java.io.File;
import java.io.FileOutputStream;

public class Test1 {
    public static void main(String[] args) throws Exception {
        // 目标：在 "demo/a/b/" (不存在的目录) 下创建 "file.txt"
        File file = new File("demo/a/b/file.txt");

        // 1. 【关键步骤】先创建父目录 (如果不存在)
        // 如果注释掉下面这行，下面的 new FileOutputStream 就会报错
        file.getParentFile().mkdirs();

        // 2. 创建文件流 -> 会自动创建 "file.txt" 这个文件
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write("Hello".getBytes());
            System.out.println("✅ 成功！文件已创建在: " + file.getAbsolutePath());
        }
    }
}
