package com.haoge.usefulcodes.cache;

import com.haoge.usefulcodes.utils.cache.CacheStore;

import org.junit.Test;

public class CacheStoreTest {

    @Test
    public void CacheTest() {
        for (int i = 0; i < 10; i++) {
            // 先存储慢预存容器
            CacheStore.INSTANCE.put("The cache index is " + i);
        }

        String value = CacheStore.INSTANCE.get(3);// 取出第三个位置的数据
        System.out.println("取出的第三个位置的数据为：" + value);
        value = CacheStore.INSTANCE.get(3);// 再次对此位置进行读取。应该取出为null
        System.out.println("再次取出的第三个位置的数据为：" + value);
        CacheStore.INSTANCE.put("再次进行存储的数据，此处应该存放在首个空位(索引3)处。");
        value = CacheStore.INSTANCE.get(3);// 再次对此位置进行读取。应该取出为新加数据
        System.out.println("再次取出的第三个位置的数据为：" + value);

        CacheStore.INSTANCE.put("这条数据会填满目前的容器");
        int index = CacheStore.INSTANCE.put("这条数据应该会导致容器进行扩容，并返回首个空位索引(index=10)");
        System.out.println("index = " + index);

    }
}