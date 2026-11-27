package dev.prestigeclient.renderlib.impl.render.mgr;

import dev.prestigeclient.renderlib.api.path.RLPath;
import dev.prestigeclient.renderlib.impl.render.resource.ShaderProgram;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.lwjgl.opengl.GL20;

public class ShaderManager {
    public static ShaderProgram loadShader(RLPath rlpath, RLPath rlpath1, boolean flag) {
        if (flag) {
            throw new UnsupportedOperationException(
                "SPIR-V shader loading is not supported on macOS (no GL_ARB_gl_spirv); pass loadAsSpirv=false and ship GLSL sources."
            );
        } else {
            int i = createShader(35633, rlpath);
            int j = createShader(35632, rlpath1);
            int k = GL20.glCreateProgram();
            GL20.glAttachShader(k, i);
            GL20.glAttachShader(k, j);
            GL20.glLinkProgram(k);
            String s = rlpath.getPath();
            String s1 = rlpath1.getPath();
            int l = s.lastIndexOf("/");
            String s2 = s.substring(l + 1);
            l = s1.lastIndexOf("/");
            String s3 = s1.substring(l + 1);
            String s4 = s2 + ":" + s3;
            if (GL20.glGetProgrami(k, 35714) == 0) {
                String s5 = GL20.glGetProgramInfoLog(k);
                System.err.println(s5);
                throw new RuntimeException("Program link failed. See error above for: " + s4);
            } else {
                GL20.glDeleteShader(i);
                GL20.glDeleteShader(j);
                return new ShaderProgram(k, s4);
            }
        }
    }

    private static int createShader(int i, RLPath rlpath) {
        int j = GL20.glCreateShader(i);

        String s;
        try (InputStream inputstream = rlpath.openStream()) {
            s = new String(inputstream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ioexception) {
            throw new RuntimeException("Failed to load GLSL shader: " + rlpath, ioexception);
        }

        GL20.glShaderSource(j, s);
        GL20.glCompileShader(j);
        if (GL20.glGetShaderi(j, 35713) == 0) {
            String s1 = GL20.glGetShaderInfoLog(j);
            System.err.println(s1);
            throw new RuntimeException("Shader compile failed for " + rlpath + ". See logs above for error");
        } else {
            return j;
        }
    }
}
