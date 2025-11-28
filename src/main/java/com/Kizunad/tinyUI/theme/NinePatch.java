package com.Kizunad.tinyUI.theme;

/**
 * 九宫格描述，记录边框切片尺寸与资源路径。
 */
public final class NinePatch {

    private final String texturePath;
    private final int left;
    private final int right;
    private final int top;
    private final int bottom;

    public NinePatch(final String texturePath, final int left, final int right,
                     final int top, final int bottom) {
        this.texturePath = texturePath;
        this.left = Math.max(0, left);
        this.right = Math.max(0, right);
        this.top = Math.max(0, top);
        this.bottom = Math.max(0, bottom);
    }

    public String getTexturePath() {
        return texturePath;
    }

    public int getLeft() {
        return left;
    }

    public int getRight() {
        return right;
    }

    public int getTop() {
        return top;
    }

    public int getBottom() {
        return bottom;
    }
}
