// 移植自上游 fish2018/webhtv（commit 4e30ffa），包名保持原样（第三方 ijkplayer 包名）。
package tv.danmaku.ijk.media.player.misc;

/* loaded from: classes3.dex */
public interface IMediaDataSource {
    void close();

    long getSize();

    int readAt(long j8, byte[] bArr, int i8, int i9);
}
