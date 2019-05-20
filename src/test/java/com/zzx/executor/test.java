package com.zzx.executor;

import javax.sound.midi.Soundbank;

public class test {
    public static void main(String[] args) {
        String s="2019-04-2512:01:40";
        String date=s.substring(0,10);
        String time =s.substring(10);
        System.out.println(date);
        System.out.println(time);
    }
}
