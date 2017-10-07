package optifine

import net.minecraft.launchwrapper.IClassTransformer
import net.minecraft.launchwrapper.ITweaker
import net.minecraft.launchwrapper.LaunchClassLoader
import java.io.File

class OptiFineTweakerDevWrapper : ITweaker {
    override fun acceptOptions(p0: MutableList<String>?, p1: File?, p2: File?, p3: String?) { }
    override fun getLaunchArguments(): Array<out String>? = Array(0) {""}
    override fun getLaunchTarget() = "net.minecraft.client.main.Main"
    override fun injectIntoClassLoader(classLoader: LaunchClassLoader) {
        classLoader.registerTransformer("optifine.OptiFineTransformerDevWrapper")
    }
}

class OptiFineTransformerDevWrapper : IClassTransformer {
    val ofTransformer = Class.forName("optifine.OptiFineClassTransformer").newInstance() as IClassTransformer

    override fun transform(name: String?, transformedName: String?, classData: ByteArray?) =
            ofTransformer.transform(name?.replace(".", "/"), transformedName, classData)
}