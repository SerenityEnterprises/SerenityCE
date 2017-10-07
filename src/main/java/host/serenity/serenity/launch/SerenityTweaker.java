package host.serenity.serenity.launch;

import com.google.common.collect.Lists;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import java.io.File;
import java.util.List;

public class SerenityTweaker implements ITweaker {
    private List<String> args;

    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
        this.args = Lists.newArrayList(args);
        this.args.add("--version");
        this.args.add(profile);
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader launchClassLoader) {
        try {
            launchClassLoader.addClassLoaderExclusion("org.apache.logging.log4j.");
            MixinBootstrap.init();

            Mixins.addConfiguration("mixins.serenity.core.json");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getLaunchTarget() {
        return "net.minecraft.client.main.Main";
    }

    @Override
    public String[] getLaunchArguments() {
        return this.args.toArray(new String[this.args.size()]);
    }
}
