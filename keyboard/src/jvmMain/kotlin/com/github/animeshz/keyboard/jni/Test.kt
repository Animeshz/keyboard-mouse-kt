package com.github.animeshz.keyboard.jni

public class Test {
    public external fun test(a: Int): Int

    public companion object {
        init {
            System.load("keyboard")
        }
    }
}
