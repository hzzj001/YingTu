package com.yingtu.plus.update;

/**
 * 兼容实现，待上游 webhtv 源码对齐。
 *
 * OCI 镜像产物描述（Update.bean 的 oci 字段类型，仅作类型占位）。
 * 兼容实现恒为无效（isValid() 返回 false）。
 */
public class OciArtifact {

    public boolean isValid() {
        return false;
    }
}
