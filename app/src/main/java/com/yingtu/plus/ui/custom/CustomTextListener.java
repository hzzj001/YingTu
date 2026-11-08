// 移植自上游 fish2018/webhtv app/src/mobile/java/com/fongmi/android/tv/ui/custom/CustomTextListener.java @ HEAD 4e30ffa（2026-10-07），仅包名 com.fongmi.android.tv → com.yingtu.plus；逻辑未改。
package com.yingtu.plus.ui.custom;

import android.text.Editable;
import android.text.TextWatcher;

public class CustomTextListener implements TextWatcher {

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
    }
}
