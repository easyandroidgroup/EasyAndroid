package com.haoge.usefulcodes.utils.tools

object CommonTool {

    fun isEmpty(data: Any?): Boolean =  when {
            data == null -> true
            data is CharSequence -> data.length == 0
            data is List<*> -> data.isEmpty()
            data is Map<*, *> -> data.isEmpty()
            data.javaClass.isArray -> (data as Array<*>).size == 0
            else -> false
        }
}
