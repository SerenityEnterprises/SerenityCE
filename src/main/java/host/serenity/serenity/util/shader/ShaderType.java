package host.serenity.serenity.util.shader;

import org.lwjgl.opengl.ARBFragmentShader;
import org.lwjgl.opengl.ARBVertexShader;

public enum ShaderType {
    VERTEX(ARBVertexShader.GL_VERTEX_SHADER_ARB),
    FRAGMENT(ARBFragmentShader.GL_FRAGMENT_SHADER_ARB);

    private int typeId;

    ShaderType(int typeId) {
        this.typeId = typeId;
    }

    public int getTypeId() {
        return typeId;
    }

}