package host.serenity.serenity.util;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.function.Consumer;

public class Bootstrapper<T> {
    private String prefix;
    private Class<T> targetClass;

    public Bootstrapper(String prefix, Class<T> targetClass) {
        this.prefix = prefix;
        this.targetClass = targetClass;
    }

    public void bootstrap(Consumer<T> consumer) {
        try {
            ClassPath classpath = ClassPath.from(this.getClass().getClassLoader());
            ImmutableSet<ClassPath.ClassInfo> classInfoSet;

            if (prefix == null) {
                classInfoSet = classpath.getAllClasses();
            } else {
                classInfoSet = classpath.getTopLevelClassesRecursive(prefix);
            }

            for (ClassPath.ClassInfo classInfo : classInfoSet) {
                try {
                    Class<?> cls = classInfo.load();
                    if (targetClass.isAssignableFrom(cls)) {
                        @SuppressWarnings("unchecked")
                        Class<? extends T> tClass = (Class<? extends T>) cls;
                        if (!Modifier.isAbstract(tClass.getModifiers()) && !tClass.isInterface()) {
                            try {
                                consumer.accept(tClass.newInstance());
                            } catch (InstantiationException | IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (Throwable t) {}
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
