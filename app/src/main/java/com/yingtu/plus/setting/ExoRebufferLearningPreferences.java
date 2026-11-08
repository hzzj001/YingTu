// 移植自上游 fish2018/webhtv (commit 4e30ffa)：app/src/main/java/com/fongmi/android/tv/setting/ExoRebufferLearningPreferences.java，包名已改写为 com.yingtu.plus
package com.yingtu.plus.setting;

import com.github.catvod.utils.Prefers;

/** SharedPreferences adapter kept outside the pure learning store. */
final class ExoRebufferLearningPreferences implements ExoRebufferLearningStore.Backend {

    static final String KEY = "perf_exo_rebuffer_learning_v1";

    @Override
    public String read() {
        return Prefers.getString(KEY);
    }

    @Override
    public void write(String value) {
        Prefers.put(KEY, value);
    }

    @Override
    public void clear() {
        Prefers.remove(KEY);
    }
}
