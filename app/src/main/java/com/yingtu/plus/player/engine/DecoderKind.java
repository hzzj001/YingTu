package com.yingtu.plus.player.engine;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * 解码器种类（exo / ijk 引擎在汇报播放事实时用它标明当前解码方式）。
 * 仅含调用点用到的三个取值。
 */
public enum DecoderKind {
    HARDWARE, SOFTWARE, UNKNOWN
}
