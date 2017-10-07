package host.serenity.serenity.util;

import com.google.common.reflect.ClassPath;
import host.serenity.serenity.Serenity;
import host.serenity.serenity.api.command.Command;
import host.serenity.serenity.api.file.ClientDataHandler;
import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.plugin.Plugin;

import java.io.IOException;
import java.util.Set;
import java.util.function.Consumer;

public class SerenityBootstrapper implements Runnable {
    private static final String PREFIX = "host.serenity.serenity";

    @Override
    public void run() {
        try {
            ClassPath classpath = ClassPath.from(this.getClass().getClassLoader());
            Set<ClassPath.ClassInfo> classInfoSet = classpath.getAllClasses();

            for (ClassPath.ClassInfo classInfo : classInfoSet) {
                try {
                    if (classInfo.getPackageName().startsWith(PREFIX)) {
                        if (classInfo.getPackageName().startsWith(PREFIX + ".modules")) {
                            tryBootstrapClass(Module.class, classInfo.load(), module -> {
                                module.bootstrap();
                                Serenity.getInstance().getModuleManager().getModules().add(module);
                            });
                        }
                        if (classInfo.getPackageName().startsWith(PREFIX + ".commands")) {
                            tryBootstrapClass(Command.class, classInfo.load(), Serenity.getInstance().getCommandManager().getCommands()::add);
                        }
                        if (classInfo.getPackageName().startsWith(PREFIX + ".files")) {
                            tryBootstrapClass(ClientDataHandler.class, classInfo.load(), Serenity.getInstance().getFileManager()::registerDataHandler);
                        }
                    }

                    if (classInfo.getSimpleName().startsWith("SerenityPlugin")) {
                        tryBootstrapClass(Plugin.class, classInfo.load(), Serenity.getInstance().getPluginManager()::tryLoad);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Serenity.getInstance().getModuleManager().sortModules();

        Serenity.getInstance().getFileManager().loadAll();
        Serenity.getInstance().getFileManager().saveAll(); // Save after loading so that all files are populated on client launch.
    }

    private static <T> void tryBootstrapClass(Class<T> baseClass, Class<?> cls, Consumer<T> bootstrapper) throws IllegalAccessException, InstantiationException {
        if (baseClass.isAssignableFrom(cls)) {
            @SuppressWarnings("unchecked") Class<? extends T> tClass = (Class<? extends T>) cls;

            bootstrapper.accept(tClass.newInstance());
        }
    }
}
