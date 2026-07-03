package com.example.cj.videoeditor.utils;

import android.annotation.SuppressLint;

import java.text.NumberFormat;

public class FormatUtil {

    private FormatUtil() {}

    public static String maskPhone(String phone) {
        if (phone == null || phone.length() != 11) return phone;
        return phone.substring(0, 3) + "*****" + phone.substring(8);
    }

    @SuppressLint("DefaultLocale")
    public static String formatMoney(String amount) {
        try {
            long value = Long.parseLong(amount);
            NumberFormat nf = NumberFormat.getInstance();
            nf.setGroupingUsed(true);
            return "¥" + nf.format(value);
        } catch (Exception e) {
            return "¥" + amount;
        }
    }
}