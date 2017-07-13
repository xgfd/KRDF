package utils;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by xgfd on 14/07/2017.
 */
public class DoubleKeyHashMapTest {
    @Test
    public void get() throws Exception {
        DoubleKeyHashMap<String, Integer, Integer> lfu = new DoubleKeyHashMap<>(6, 0.5f);
        lfu.put("a", 1, 1);
        lfu.put("b", 2, 2);
        lfu.put("c", 3, 3);
        lfu.put("aa", 4, 4);
        lfu.put("bb", 5, 5);
        lfu.put("cc", 6, 6);
        System.out.println(lfu.get("a", 1));
        System.out.println(lfu.get("b", 2));
        System.out.println(lfu.get("c", 3));
        System.out.println(lfu.get("aa", 4));
        System.out.println(lfu.get("bb", 5));
        System.out.println(lfu.get("cc", 6));
        System.out.println(lfu.get("a", 1));
        System.out.println(lfu.get("b", 2));
        System.out.println(lfu.get("c", 3));
        lfu.put("dd", 7, 7);
        System.out.println(lfu.get("a", 1));
        System.out.println(lfu.get("b", 2));
        System.out.println(lfu.get("c", 3));
        System.out.println(lfu.get("aa", 4));
        System.out.println(lfu.get("bb", 5));
        System.out.println(lfu.get("cc", 6));
        System.out.println(lfu.get("dd", 7));
    }

}