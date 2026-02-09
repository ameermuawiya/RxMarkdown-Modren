/*
 * Copyright (C) 2016 yydcdut (yuyidong2015@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.yydcdut.markdown.syntax.text;

import androidx.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;

import com.yydcdut.markdown.MarkdownConfiguration;
import com.yydcdut.markdown.loader.MDImageLoader;
import com.yydcdut.markdown.span.MDImageSpan;
import com.yydcdut.markdown.syntax.SyntaxKey;
import com.yydcdut.markdown.utils.CharacterProtector;
import com.yydcdut.markdown.utils.TextHelper;

import java.util.regex.Pattern;

class ImageSyntax extends TextSyntaxAdapter {

    private static final String TAG = "ImageSyntax";
    private static final String DEFAULT_TEXT = "image";
    private static final String PATTERN = ".*[!\\[]{1}.*[\\](]{1}.*[)]{1}.*";

    private int[] mSize;
    private MDImageLoader mMDImageLoader;

    public ImageSyntax(@NonNull MarkdownConfiguration markdownConfiguration) {
        super(markdownConfiguration);
        mSize = markdownConfiguration.getDefaultImageSize();
        mMDImageLoader = markdownConfiguration.getRxMDImageLoader();
    }

    @Override
    boolean isMatch(@NonNull String text) {
        return contains(text) || Pattern.compile(PATTERN).matcher(text).matches();
    }

    @NonNull
    @Override
    boolean encode(@NonNull SpannableStringBuilder ssb) {
        boolean handled = false;
        handled |= replace(ssb, SyntaxKey.KEY_IMAGE_BACKSLASH_LEFT, CharacterProtector.getKeyEncode());
        handled |= replace(ssb, SyntaxKey.KEY_IMAGE_BACKSLASH_MIDDLE, CharacterProtector.getKeyEncode2());
        handled |= replace(ssb, SyntaxKey.KEY_IMAGE_BACKSLASH_RIGHT, CharacterProtector.getKeyEncode4());
        return handled;
    }

    @Override
    SpannableStringBuilder format(@NonNull SpannableStringBuilder ssb, int lineNumber) {
        return parse(ssb);
    }

    @NonNull
    @Override
    void decode(@NonNull SpannableStringBuilder ssb) {
        replace(ssb, CharacterProtector.getKeyEncode(), SyntaxKey.KEY_IMAGE_BACKSLASH_LEFT);
        replace(ssb, CharacterProtector.getKeyEncode2(), SyntaxKey.KEY_IMAGE_BACKSLASH_MIDDLE);
        replace(ssb, CharacterProtector.getKeyEncode3(), SyntaxKey.KEY_IMAGE_BACKSLASH_RIGHT);
    }

    private static boolean contains(String text) {
        if (text.length() < 5 || TextUtils.equals(text, "![]()")) return true;

        char[] array = text.toCharArray();
        char[] find = {'!', '[', ']', '(', ')'};
        int fp = 0;

        for (int i = 0; i < array.length; i++) {
            if (TextHelper.getChar(array, i) == find[fp]) {
                fp++;
                if (fp == find.length) return true;
            }
        }
        return false;
    }

    @NonNull
    private SpannableStringBuilder parse(@NonNull SpannableStringBuilder ssb) {
        SpannableStringBuilder tmp = new SpannableStringBuilder();
        String tmpTotal = ssb.toString();

        while (true) {
            int p0 = tmpTotal.indexOf(SyntaxKey.KEY_IMAGE_LEFT);
            int p1 = tmpTotal.indexOf(SyntaxKey.KEY_IMAGE_MIDDLE);
            int p2 = tmpTotal.indexOf(SyntaxKey.KEY_IMAGE_RIGHT);

            if (p0 == -1 || p1 == -1 || p2 == -1) break;

            if (p0 < p1 && p1 < p2) {
                try {
                    int tmpCenter = tmpTotal.indexOf(SyntaxKey.KEY_IMAGE_MIDDLE);
                    String tmpLeft = tmpTotal.substring(0, tmpCenter);

                    int positionHeader = tmpLeft.lastIndexOf(SyntaxKey.KEY_IMAGE_LEFT);
                    tmp.append(tmpTotal.substring(0, positionHeader));

                    int index = tmp.length();

                    tmpTotal = tmpTotal.substring(
                            positionHeader + SyntaxKey.KEY_IMAGE_LEFT.length()
                    );

                    int positionCenter = tmpTotal.indexOf(SyntaxKey.KEY_IMAGE_MIDDLE);

                    safeDelete(
                            ssb,
                            index,
                            index + SyntaxKey.KEY_IMAGE_LEFT.length()
                    );

                    tmp.append(tmpTotal.substring(0, positionCenter));

                    tmpTotal = tmpTotal.substring(
                            positionCenter + SyntaxKey.KEY_IMAGE_MIDDLE.length()
                    );

                    int positionFooter = tmpTotal.indexOf(SyntaxKey.KEY_IMAGE_RIGHT);
                    String link = tmpTotal.substring(0, positionFooter);

                    if (index == tmp.length()) {
                        tmp.append(DEFAULT_TEXT);
                    }

                    int spanStart = clamp(index, ssb.length());
                    int spanEnd = clamp(tmp.length(), ssb.length());

                    ssb.setSpan(
                            new MDImageSpan(link, mSize[0], mSize[1], mMDImageLoader),
                            spanStart,
                            spanEnd,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    );

                    safeDelete(
                            ssb,
                            spanEnd,
                            spanEnd
                                    + SyntaxKey.KEY_IMAGE_MIDDLE.length()
                                    + link.length()
                                    + SyntaxKey.KEY_IMAGE_RIGHT.length()
                    );

                    tmpTotal = tmpTotal.substring(
                            positionFooter + SyntaxKey.KEY_IMAGE_RIGHT.length()
                    );

                } catch (Throwable t) {
                    Log.e(TAG, "Image parse error, skipped", t);
                    break;
                }

            } else if (p0 < p1 && p2 < p1) {
                tmpTotal = replaceFirstOne(tmpTotal,
                        SyntaxKey.KEY_IMAGE_RIGHT,
                        SyntaxKey.PLACE_HOLDER
                );

            } else if (p1 < p0 && p1 < p2) {
                tmp.append(tmpTotal.substring(0, p1 + SyntaxKey.KEY_IMAGE_MIDDLE.length()));
                tmpTotal = tmpTotal.substring(p1 + SyntaxKey.KEY_IMAGE_MIDDLE.length());

            } else if (p2 < p0 && p2 < p1) {
                tmp.append(tmpTotal.substring(0, p2 + SyntaxKey.KEY_IMAGE_RIGHT.length()));
                tmpTotal = tmpTotal.substring(p2 + SyntaxKey.KEY_IMAGE_RIGHT.length());
            }
        }
        return ssb;
    }

    private static int clamp(int value, int max) {
        if (value < 0) return 0;
        if (value > max) return max;
        return value;
    }

    private static void safeDelete(
            SpannableStringBuilder ssb,
            int start,
            int end
    ) {
        try {
            start = clamp(start, ssb.length());
            end = clamp(end, ssb.length());
            if (start < end) ssb.delete(start, end);
        } catch (Throwable t) {
            Log.e(TAG, "safeDelete failed", t);
        }
    }

    @NonNull
    private String replaceFirstOne(
            @NonNull String content,
            @NonNull String target,
            @NonNull String replacement
    ) {
        int matchStart = content.indexOf(target);
        if (matchStart == -1) return content;

        StringBuilder result = new StringBuilder(content.length());
        result.append(content, 0, matchStart);
        result.append(replacement);
        result.append(content.substring(matchStart + target.length()));
        return result.toString();
    }
}