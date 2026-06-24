package com.yupi.yuaiagent.demo.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class TranslateInstanceTest {
    @Resource
    TranslateInstance translateInstance;

    @Test
    void translate() {
        translateInstance.translate("hello","en","zh");
    }
    int cnt = 0;
    @Test
    void test1(){
        int n = 22;
        int[] memo = new int[n+1];
        Arrays.fill(memo,-1);
        int base = (int)Math.sqrt(n*1.0);
        int ans = dfs(base,n,memo);
        System.out.println("最终次数："+ans);
    }
    public int dfs(int base,int tar,int[] memo){
        cnt++;
        System.out.println("第"+cnt+"个dfs，base="+base+",tar="+tar);
        if(tar==0){
            return 0;
        }
        if(tar<0||base<=0){
            return Integer.MAX_VALUE/2;
        }

        if(memo[tar]!=-1){
            return memo[tar];
        }
        if(tar<base*base){
            return dfs(base-1,tar,memo);
        }

        //选这个数
        int choose = dfs(base,tar-base*base,memo) + 1;
        //不选这个数
        int notChoose = dfs(base-1,tar,memo);
        memo[tar] = Math.min(choose,notChoose);
        System.out.println("memo["+tar+"]="+"被赋值："+memo[tar]);

        return memo[tar];
    }

}