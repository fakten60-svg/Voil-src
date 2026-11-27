package dev.prestigeclient.renderlib.impl.render.vertex;

public class VertexFormats {
    public static final VertexFormat POSITION = new VertexFormat(new VertexElement[]{new VertexElement(3, 5126, 4, false)});
    public static final VertexFormat POSITION_COLOR = new VertexFormat(
        new VertexElement[]{new VertexElement(3, 5126, 4, false), new VertexElement(4, 5121, 1, true)}
    );
    public static final VertexFormat POSITION_COLOR_UV = new VertexFormat(
        new VertexElement[]{new VertexElement(3, 5126, 4, false), new VertexElement(4, 5121, 1, true), new VertexElement(2, 5126, 4, false)}
    );
    public static final VertexFormat POSITION_NORMAL_COLOR_UV = new VertexFormat(
        new VertexElement[]{
            new VertexElement(3, 5126, 4, false),
            new VertexElement(3, 5126, 4, false),
            new VertexElement(4, 5121, 1, true),
            new VertexElement(2, 5126, 4, false)
        }
    );
    public static final VertexFormat POSITION_UV_SIZE_COLOR = new VertexFormat(
        new VertexElement[]{
            new VertexElement(3, 5126, 4, false),
            new VertexElement(2, 5126, 4, false),
            new VertexElement(2, 5126, 4, false),
            new VertexElement(4, 5121, 1, true)
        }
    );
    public static final VertexFormat POSITION_UV = new VertexFormat(
        new VertexElement[]{new VertexElement(3, 5126, 4, false), new VertexElement(2, 5126, 4, false)}
    );
    public static final VertexFormat POSITION_UV_SIZE = new VertexFormat(
        new VertexElement[]{new VertexElement(3, 5126, 4, false), new VertexElement(2, 5126, 4, false), new VertexElement(2, 5126, 4, false)}
    );
}
