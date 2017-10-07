package host.serenity.serenity.util.shader;

import java.io.InputStream;

public class Asset {
    private String label;

    public Asset(String label) {
        this.label = label;
    }

    public InputStream asInputStream(){
        return this.getClass().getClassLoader().getResourceAsStream(label);
    }
}