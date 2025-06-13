package com.example.endtoendencryptionsystem.utils


import android.util.Log
import net.sourceforge.pinyin4j.PinyinHelper


object PinyinUtils {

    /**
     * 获取单个字符的拼音首字母
     */
    fun getPinyinInitial(char: Char): String {
        val pinyinArray = PinyinHelper.toHanyuPinyinStringArray(char)
        return if (pinyinArray != null && pinyinArray.isNotEmpty()) {
            // 取拼音首字母并大写
            pinyinArray[0].substring(0, 1).uppercase()
        } else {
            "#"
        }
    }

    /**
     * 获取整个字符串的首字母（支持中文、英文等）
     */
    fun getPinyinInitials(name: String): String {
        if (name.isEmpty()) return "#"
        val firstChar = name[0]
        // 如果是英文字母，直接返回大写
        if (firstChar.isEnglishLetter()) {
            return firstChar.uppercaseChar().toString()
        }
        // 否则尝试转为拼音首字母
        return getPinyinInitial(firstChar)
    }

    // 扩展函数：判断是否是英文字母 A-Z / a-z
    private fun Char.isEnglishLetter(): Boolean {
        return this in 'A'..'Z' || this in 'a'..'z'
    }
}

