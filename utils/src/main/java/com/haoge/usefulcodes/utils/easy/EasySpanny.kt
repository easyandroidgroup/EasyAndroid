package com.haoge.usefulcodes.utils.easy

/*
 * Copyright 2015 Pavlovsky Ivan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 *
 * @author binaryfork
 *
 * Please report any issues
 * https://github.com/binaryfork/Spanny/issues
 */
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ImageSpan

/**
 * Spannable wrapper for simple creation of Spannable strings.
 */
class EasySpanny : SpannableStringBuilder {

    private var flag = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE

    constructor() : super("") {}

    constructor(text: CharSequence) : super(text) {}

    constructor(text: CharSequence, vararg spans: Any) : super(text) {
        for (span in spans) {
            setSpan(span, 0, length)
        }
    }

    constructor(text: CharSequence, span: Any) : super(text) {
        setSpan(span, 0, text.length)
    }

    /**
     * Appends the character sequence `text` and spans `spans` over the appended part.
     * @param text the character sequence to append.
     * @param spans the object or objects to be spanned over the appended text.
     * @return this `EasySpanny`.
     */
    fun append(text: CharSequence, vararg spans: Any): EasySpanny {
        append(text)
        for (span in spans) {
            setSpan(span, length - text.length, length)
        }
        return this
    }

    fun append(text: CharSequence, span: Any): EasySpanny {
        append(text)
        setSpan(span, length - text.length, length)
        return this
    }

    /**
     * Add the ImageSpan to the start of the text.
     * @return this `EasySpanny`.
     */
    fun append(text: CharSequence, imageSpan: ImageSpan): EasySpanny {
        var text = text
        text = ".$text"
        append(text)
        setSpan(imageSpan, length - text.length, length - text.length + 1)
        return this
    }

    /**
     * Append plain text.
     * @return this `EasySpanny`.
     */
    override fun append(text: CharSequence): EasySpanny {
        super.append(text)
        return this
    }


    @Deprecated("use {@link #append(CharSequence text)}")
    fun appendText(text: CharSequence): EasySpanny {
        append(text)
        return this
    }

    /**
     * Change the flag. Default is SPAN_EXCLUSIVE_EXCLUSIVE.
     * The flags determine how the span will behave when text is
     * inserted at the start or end of the span's range
     * @param flag see [Spanned].
     */
    fun setFlag(flag: Int) {
        this.flag = flag
    }

    /**
     * Mark the specified range of text with the specified object.
     * The flags determine how the span will behave when text is
     * inserted at the start or end of the span's range.
     */
    private fun setSpan(span: Any, start: Int, end: Int) {
        setSpan(span, start, end, flag)
    }

    /**
     * Sets a span object to all appearances of specified text in the spannable.
     * A new instance of a span object must be provided for each iteration
     * because it can't be reused.
     *
     * @param textToSpan Case-sensitive text to span in the current spannable.
     * @param getSpan    Interface to get a span for each spanned string.
     * @return `EasySpanny`.
     */
    fun findAndSpan(textToSpan: CharSequence, getSpan: GetSpan): EasySpanny {
        var lastIndex = 0
        while (lastIndex != -1) {
            lastIndex = toString().indexOf(textToSpan.toString(), lastIndex)
            if (lastIndex != -1) {
                setSpan(getSpan.span, lastIndex, lastIndex + textToSpan.length)
                lastIndex += textToSpan.length
            }
        }
        return this
    }

    /**
     * Interface to return a new span object when spanning multiple parts in the text.
     */
    interface GetSpan {

        /**
         * @return A new span object should be returned.
         */
        val span: Any
    }

    companion object {

        /**
         * Sets span objects to the text. This is more efficient than creating a new instance of EasySpanny
         * or SpannableStringBuilder.
         * @return `SpannableString`.
         */
        fun spanText(text: CharSequence, vararg spans: Any): SpannableString {
            val spannableString = SpannableString(text)
            for (span in spans) {
                spannableString.setSpan(span, 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            return spannableString
        }

        fun spanText(text: CharSequence, span: Any): SpannableString {
            val spannableString = SpannableString(text)
            spannableString.setSpan(span, 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            return spannableString
        }
    }
}