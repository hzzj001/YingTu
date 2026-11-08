// 移植自上游 fish2018/webhtv（commit 4e30ffa），包名保持原样（第三方 ijkplayer 包名）。
package tv.danmaku.ijk.media.player.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.CLASS)
/* loaded from: classes3.dex */
public @interface AccessedByNative {
}
