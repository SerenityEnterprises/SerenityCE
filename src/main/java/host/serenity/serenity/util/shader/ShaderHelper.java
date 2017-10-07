package host.serenity.serenity.util.shader;

import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.util.Scanner;

import static host.serenity.serenity.util.shader.Shader.getLogInfo;

public class ShaderHelper {
    public static final ShaderHelper INSTANCE = new ShaderHelper();

    public int create(String shaderCode, ShaderType type){
        // The shader id.
        int shader = 0;
        try {
            // Create shader program
            shader = ARBShaderObjects.glCreateShaderObjectARB(type.getTypeId());

            // Returns if the shader isn't found.
            if(shader == 0)
                return 0;

            // Load and compile shader source.
            ARBShaderObjects.glShaderSourceARB(shader, shaderCode);
            ARBShaderObjects.glCompileShaderARB(shader);

            //Check for errors and throws an exception if one is found.
            if (ARBShaderObjects.glGetObjectParameteriARB(shader, ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB) == GL11.GL_FALSE) {
                System.out.println(GL20.glGetShaderInfoLog(shader, 500));
                System.err.println("Could not compile shader!");
                throw new RuntimeException("Error creating shader: " + getLogInfo(shader));
            }

            // Return the shader id.
            return shader;
        } catch(Exception exception) {
            // Deletes the shader.
            ARBShaderObjects.glDeleteObjectARB(shader);

            // Prints the stack trace.
            exception.printStackTrace();
        }

        // Returns no shader.
        return 0;
    }

    /**
     * Creates a shader from the given asset and shader type and returns the id of it.
     *
     * @param shader The asset form of the shader.
     * @param type The type of the shader.
     */
    public int create(Asset shader, ShaderType type) {
        // To to-be code of the shader.
        String code = "";

        // The shader input stream as a scanner to get the code from the asset.
        Scanner scanner = new Scanner(shader.asInputStream());

        // Adds all the code in the shader to the code string.
        while (scanner.hasNext())
            code += scanner.nextLine() + "\n";

        // Creates and returns the shader.
        return create(code, type);
    }
}
