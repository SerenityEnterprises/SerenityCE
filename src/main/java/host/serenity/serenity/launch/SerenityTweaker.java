package host.serenity.serenity.launch;

import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SerenityTweaker implements ITweaker {
    private final List<String> args = new ArrayList<>();

    @Override
    public void acceptOptions(List<String> list, File gameDir, File assetsDir, String profile) {
        args.addAll(list);

        if (!args.contains("--version") && profile != null) {
            args.add("--version");
            args.add(profile);
        }

        if (!args.contains("--assetsDir") && assetsDir != null) {
            args.add("--assetsDir");
            args.add(assetsDir.getPath());
        }

        if (!args.contains("--gameDir") && gameDir != null) {
            args.add("--gameDir");
            args.add(gameDir.getPath());
        }
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader launchClassLoader) {
        launchClassLoader.addClassLoaderExclusion("org.apache.logging.log4j.");
        MixinBootstrap.init();

        Mixins.addConfiguration("mixins.serenity.core.json");
        MixinEnvironment.getDefaultEnvironment().setObfuscationContext("notch");
        MixinEnvironment.getDefaultEnvironment().setSide(MixinEnvironment.Side.CLIENT);
    }

    @Override
    public String getLaunchTarget() {
        return "net.minecraft.client.main.Main";
    }

    @Override
    public String[] getLaunchArguments() {
        return args.toArray(new String[args.size()]);
    }
}
