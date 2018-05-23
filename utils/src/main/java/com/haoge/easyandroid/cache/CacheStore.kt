package com.haoge.easyandroid.cache

import java.util.*

/**
 * 进行临时性存储。方便将不方便放入Intent中进行存储的数据进行存储：
 *
 * // 举例：跨页面传递Context
 *
 * // 跳转前存储context
 * Intent intent = getIntent();
 * int index = CacheStore.get().put(context);
 * intent.putExtra(KEY_INDEX_CONTEXT, index);
 * startActivity(intent);
 *
 * // 跳转后读取context
 * Intent intent = getIntent();
 * int index = intent.getExtra(KEY_INDEX_CONTEXT);
 * // 读取后自动从CacheStore容器中移除。
 * Context context = CacheStore.get().get(index);
 *
 * @author haoge
 */
object CacheStore {

    // 自动增长的数据存储容器
    private var stores = arrayOfNulls<Any>(10)

    /**
     * 根据索引取出该位置的数据并将此位置进行重新置空
     */
    fun <T> get(index: Int): T? {
        return try {
            get(index, true)
        } catch (cast: ClassCastException) {
            null
        }
    }

    /**
     * @param index 存储的值的索引下标
     * @param remove 是否自动移除
     */
    fun <T> get(index:Int, remove:Boolean = true):T? {
        if (index < 0 || index >= stores.size) {
            return null
        }
        val value = stores[index]
        if (remove) {
            stores[index] = null
        }
        try {
            @Suppress("UNCHECKED_CAST")
            return value as T
        } catch (cast: ClassCastException) {
            return null
        }
    }

    /**
     * 在容器中寻找空位的位置下标索引进行数据存储
     */
    fun put(value: Any?): Int {
        if (value == null) {
            return -1
        }
        val index = findIndex(value)
        stores[index] = value
        return index
    }

    /**
     * 使用具体数据value匹配具体的位置索引值, 下标匹配分别为一下三种逻辑：
     *
     *  1. 当value在容器中已存在时：返回对应的位置下标
     *  1. 当value在容易中不存在且当前容器有空位：返回首个空位下标
     *  1. 当value在容易中不存在且当前容器无空位：对容器进行扩容并返回首个空位下标
     *
     */
    private fun findIndex(value: Any): Int {
        var firstEmptyIndex = -1
        for (i in stores.indices) {
            val item = stores[i]
            if (item == null && firstEmptyIndex == -1) {
                firstEmptyIndex = i
            }

            if (item === value) {
                return i
            }
        }

        if (firstEmptyIndex == -1) {
            // 到此说明容器已满，需要扩容。定每次扩容大小为10
            val lastLength = stores.size
            stores = Arrays.copyOf<Any>(stores, lastLength + 10)
            return lastLength
        }
        // 返回首个空闲元素索引值。
        return firstEmptyIndex
    }

}