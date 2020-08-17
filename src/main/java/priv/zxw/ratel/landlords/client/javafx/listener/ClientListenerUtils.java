package priv.zxw.ratel.landlords.client.javafx.listener;

import org.nico.ratel.landlords.enums.ClientEventCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.zxw.ratel.landlords.client.javafx.ui.UIService;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class ClientListenerUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientListenerUtils.class);
    private static final Map<ClientEventCode, ClientListener> LISTENER_MAP = new HashMap(16);
    private static final String JAR_FILE_NAME = "javafx-ratel-client";
    private static final String PACKAGE_NAME = "priv/zxw/ratel/landlords/client/javafx/listener";

    public ClientListenerUtils() {
    }

    public static ClientListener getListener(ClientEventCode code) {
        ClientListener clientListener = (ClientListener)LISTENER_MAP.get(code);
        return clientListener;
    }

    public static ClientEventCode[] supportCodes() {
        return (ClientEventCode[])LISTENER_MAP.keySet().toArray(new ClientEventCode[0]);
    }

    public static void setUIService(UIService uiService) {
        Iterator var1 = LISTENER_MAP.values().iterator();

        while(var1.hasNext()) {
            ClientListener listener = (ClientListener)var1.next();
            listener.setUIService(uiService);
        }

    }

    private static List<Class<ClientListener>> findListener() {
        ClassLoader defaultClassLoader = ClientListenerUtils.class.getClassLoader();
        URL classWorkPath = ClientListenerUtils.class.getResource("");
        File classWorkDir = new File(classWorkPath.getPath());
        return (List)loadClasses(defaultClassLoader, classWorkDir.listFiles((FileFilter) ClientListenerUtils::isNormalClass)).stream().filter((clazz) -> {
            return clazz.getSuperclass() == AbstractClientListener.class;
        }).map((clazz) -> {
            return clazz;
        }).collect(Collectors.toList());
    }

    private static boolean isNormalClass(File file) {
        return isNormalClass(file.getName());
    }

    private static boolean isNormalClass(String entryName) {
        boolean isClassFile = entryName.endsWith(".class");
        boolean isNotInnerClassFile = !entryName.matches("[A-Z]\\w+\\$\\w+.class");
        return isClassFile && isNotInnerClassFile;
    }

    private static List<Class<?>> loadClasses(ClassLoader classLoader, File[] classFiles) {
        String classpath = classLoader.getResource("").getPath();
        List<Class<?>> classList = new ArrayList(classFiles.length);
        File[] var4 = classFiles;
        int var5 = classFiles.length;

        for(int var6 = 0; var6 < var5; ++var6) {
            File classFile = var4[var6];
            String absolutePath = classFile.getAbsolutePath();
            String classFullName = absolutePath.substring(classpath.length() - 1, absolutePath.lastIndexOf(".")).replace(File.separator, ".");

            try {
                classList.add(classLoader.loadClass(classFullName));
            } catch (ClassNotFoundException var11) {
                LOGGER.warn("默认类加载器在 {} 路径下没有找到 {} 类", classpath, classFullName);
            }
        }

        return classList;
    }

    private static List<Class<ClientListener>> findListenerInJarFile(JarFile jarFile) {
        List<Class<?>> classes = new ArrayList(10);
        ClassLoader defaultClassLoader = ClientListenerUtils.class.getClassLoader();
        Enumeration enumeration = jarFile.entries();

        while(enumeration.hasMoreElements()) {
            JarEntry jarEntry = (JarEntry)enumeration.nextElement();
            String entryName = jarEntry.getName();
            LOGGER.info(entryName);
            boolean isMaybeListenerClass = entryName.contains("priv/zxw/ratel/landlords/client/javafx/listener") && isNormalClass(entryName);
            if (isMaybeListenerClass) {
                String classFullName = entryName.substring(entryName.lastIndexOf("priv/zxw/ratel/landlords/client/javafx/listener"), entryName.lastIndexOf(".")).replace("/", ".");

                try {
                    classes.add(defaultClassLoader.loadClass(classFullName));
                } catch (ClassNotFoundException var9) {
                    LOGGER.warn("默认类加载器在 {} jar包中下没有找到 {} 类", jarFile.getName(), classFullName);
                }
            }
        }

        return (List)classes.stream().filter((clazz) -> {
            return clazz.getSuperclass() == AbstractClientListener.class;
        }).map((clazz) -> {
            return clazz;
        }).collect(Collectors.toList());
    }

    static {
        List listenerClassList = null;
        try {
            listenerClassList = findListener();
        } catch (NullPointerException var9) {
            String userDir = System.getProperty("user.dir");
            File userDirFile = new File(userDir);
            File jarFile = (File) Stream.of(userDirFile.listFiles()).filter((file) -> {
                return file.getName().contains("javafx-ratel-client");
            }).findFirst().orElseThrow(() -> {
                return new RuntimeException("当前 " + userDir + " 目录下找不到 " + "javafx-ratel-client" + "-{version}.jar 包");
            });

            try {
                listenerClassList = findListenerInJarFile(new JarFile(jarFile.getAbsoluteFile()));
            } catch (IOException var8) {
            }
        }

        Iterator var1 = listenerClassList.iterator();

        while(var1.hasNext()) {
            Class clazz = (Class)var1.next();

            try {
                ClientListener listener = (ClientListener)clazz.newInstance();
                LISTENER_MAP.put(listener.getCode(), listener);
            } catch (InstantiationException var6) {
                LOGGER.warn(clazz.getName() + " 不能被实例化");
            } catch (IllegalAccessException var7) {
                LOGGER.warn(clazz.getName() + " 没有默认构造函数或默认构造函数不可访问", var7);
            }
        }

    }
}

