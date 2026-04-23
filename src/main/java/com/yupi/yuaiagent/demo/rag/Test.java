package com.yupi.yuaiagent.demo.rag;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Test {
    public static void main(String[] args) {
        List<Integer> numbers = Arrays.asList(1, 2, 3);

// 1. 搭建阶段（此时什么都没发生，数据还在集合里睡觉）
        Stream<Integer> stream = numbers.stream()
                .filter(n -> {
                    System.out.println("Filter: " + n);
                    return n > 1;
                })
                .map(n -> {
                    System.out.println("Map: " + n);
                    return n * 10;
                });

// 2. 启动阶段（终止操作）
// 只有执行到这一行，上面的 filter 和 map 才会真正开始跑
        List<Integer> result = stream.collect(Collectors.toList());
    }
}
