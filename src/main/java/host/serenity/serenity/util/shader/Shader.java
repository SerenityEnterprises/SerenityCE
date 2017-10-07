package host.serenity.serenity.util.shader;

import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class Shader {
    private int combined;
    private int vertex;
    private int fragment;

    public Shader(Asset vertex, Asset fragment){
        // Creates the vertex shader from the asset.
        this.vertex = ShaderHelper.INSTANCE.create(vertex, ShaderType.VERTEX);

        // Creates the fragment shader from the asset.
        this.fragment = ShaderHelper.INSTANCE.create(fragment, ShaderType.FRAGMENT);

        // Throws an exception if the shader programs didn't compile correctly.
        if(this.vertex == 0 || this.fragment == 0)
            throw new RuntimeException(String.format("Invalid shader compile. Vertex: %s, Fragment: %s, error: (%s)\n (%s).", vertex, fragment, getLogInfo(this.vertex), getLogInfo(this.fragment)));

        // Creates the combined shader.
        this.combined = ARBShaderObjects.glCreateProgramObjectARB();

        // Attaches the vertex shader to the combined shader.
        ARBShaderObjects.glAttachObjectARB(this.combined, this.vertex);

        // Attaches the fragment shader to the combined shader.
        ARBShaderObjects.glAttachObjectARB(this.combined, this.fragment);

        // Links the combined shader.
        ARBShaderObjects.glLinkProgramARB(this.combined);

        // Validates the combined shader program to check for errors.
        ARBShaderObjects.glValidateProgramARB(this.combined);

        // Unbinds the shader.
        unbind();
    }

    public static String getLogInfo(int obj) {
        return ARBShaderObjects.glGetInfoLogARB(obj, ARBShaderObjects.glGetObjectParameteriARB(obj, ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB));
    }

    public void bind(){
        // Binds the combined shader.
        ARBShaderObjects.glUseProgramObjectARB(combined);
    }

    public void unbind(){
        // Unbinds the current shader.
        ARBShaderObjects.glUseProgramObjectARB(0);
    }

    public int getUniform(String label){
        // Returns the uniform location.
        return OpenGlHelper.glGetUniformLocation(combined, label);
    }

    public void setVector(String label, Vector2f value) {
        GL20.glUniform2f(getUniform(label), value.x, value.y);
    }

    public void setVector(String label, Vector3f value) {
        GL20.glUniform3f(getUniform(label), value.x, value.y, value.z);
    }

    public void setVector(String label, Vector4f value) {
        GL20.glUniform4f(getUniform(label), value.x, value.y, value.z, value.w);
    }

    public void setSampler2d(String label, int value){
        int loc = getUniform(label);
        GL20.glUniform1i(loc, 0);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, value);
    }

    public void setSampler2d(String label, int value, int activeTexture){
        int loc = getUniform(label);
        GL20.glUniform1i(loc, 0);
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + activeTexture);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, value);
    }

    public void setInteger(String label, int value){
        // Sets the value.
        GL20.glUniform1i(getUniform(label), value);
    }

    public void setFloat(String label, float value){
        // Sets the value.
        GL20.glUniform1f(getUniform(label), value);
    }

    public void setBoolean(String label, boolean value){
        // Sets the value to 1 or 0 depending on the state of the value.
        setInteger(label, value ? 1 : 0);
    }

}