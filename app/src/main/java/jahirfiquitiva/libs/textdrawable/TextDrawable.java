package jahirfiquitiva.libs.textdrawable;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

public class TextDrawable {
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private int width = 200;
        private int height = 200;
        private int color = Color.GRAY;
        
        public Builder square() { return this; }
        
        public Builder size(int w, int h) {
            this.width = w;
            this.height = h;
            return this;
        }
        
        public Builder color(int c) {
            this.color = c;
            return this;
        }
        
        public Builder color(int[] colors) {
            return this;
        }
        
        public TextDrawable buildRect(String text) {
            return new TextDrawable(width, height, color);
        }

        // 第 24 轮：ImgUtil（移植自上游）调 buildRect(text, color)；fork 自带的这个替身文件
        // 原来只有 buildRect(String)，编译时挡住了 JitPack 真库。补上带 color 的重载。
        public TextDrawable buildRect(String text, int color) {
            this.color = color;
            return new TextDrawable(width, height, color);
        }
        
        public TextDrawable buildRound(String text) {
            return new TextDrawable(width, height, color);
        }
        
        public TextDrawable buildRoundRect(String text, int radius) {
            return new TextDrawable(width, height, color);
        }

        // 第 24 轮：同上，补 buildRoundRect(text, color, radius)，供 ImgUtil 调用。
        public TextDrawable buildRoundRect(String text, int color, int radius) {
            this.color = color;
            return new TextDrawable(width, height, color);
        }
    }
    
    private final int width;
    private final int height;
    private final int color;
    
    private TextDrawable(int w, int h, int c) {
        this.width = w;
        this.height = h;
        this.color = c;
    }
    
    public int getColor() { return color; }
    
    public GradientDrawable toShapeDrawable() {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        return d;
    }
}
