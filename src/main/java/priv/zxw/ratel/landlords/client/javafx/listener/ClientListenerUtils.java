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

    /** code - listener 映射 */
    private static final Map<ClientEventCode, ClientListener> LISTENER_MAP = new HashMap<>(16);

    /**
     * 获取code对应的事件监听器
     *
     * @param code 事件编码
     * @return  code对应的事件监听器，如果不存在对应的事件监听器则返回null
     */
    public static ClientListener getListener(ClientEventCode code) {
        return LISTENER_MAP.get(code);
    }

    public static ClientEventCode[] supportCodes() {
        return LISTENER_MAP.keySet().toArray(new ClientEventCode[] {});
    }

    public static void setUIService(UIService uiService) {
        for (ClientListener listener : LISTENER_MAP.values()) {
            listener.setUIService(uiService);
        }
    }

    private static final String JAR_FILE_NAME = "javafx-ratel-client";

    static {
        List<Class<ClientListener>> listenerClassList;
        try {
            listenerClassList = findListener();
        } catch (NullPointerException var9) {
            String userDir = System.getProperty("user.dir");
            File userDirFile = new File(userDir);
            File jarFile = Stream.of(userDirFile.listFiles())
                                .filter((file) -> file.getName().contains(JAR_FILE_NAME))
                                .findFirst()
                                .orElseThrow(() ->
                                        new RuntimeException("当前 " + userDir + " 目录下找不到 " + "javafx-ratel-client" + "-{version}.jar 包"));

            try {
                listenerClassList = findListenerInJarFile(new JarFile(jarFile.getAbsoluteFile()));
            } catch (IOException var8) {
                throw new RuntimeException(var8);
            }
        }

        Iterator<Class<ClientListener>> var1 = listenerClassList.iterator();

        while(var1.hasNext()) {
            Class<ClientListener> clazz = var1.next();

            try {
                ClientListener listener = clazz.newInstance();
                LISTENER_MAP.put(listener.getCode(), listener);
            } catch (InstantiationException var6) {
                LOGGER.warn(clazz.getName() + " 不能被实例化");
            } catch (IllegalAccessException var7) {
                LOGGER.warn(clazz.getName() + " 没有默认构造函数或默认构造函数不可访问", var7);
            }
        }

    }

    private static List<Class<ClientListener>> findListener() {
        ClassLoader defaultClassLoader = ClientListenerUtils.class.getClassLoader();
        URL classWorkPath = ClientListenerUtils.class.getResource("");
        File classWorkDir = new File(classWorkPath.getPath());

        return loadClasses(defaultClassLoader, classWorkDir.listFiles((FileFilter) ClientListenerUtils::isNormalClass))
                .stream()
                .filter(clazz -> clazz.getSuperclass() == AbstractClientListener.class)
                .map(clazz -> (Class<ClientListener>) clazz)
                .collect(Collectors.toList());
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

        List<Class<?>> classList = new ArrayList<>(classFiles.length);
        for (File classFile : classFiles) {
            String absolutePath = classFile.getAbsolutePath();
            String classFullName = absolutePath.substring(classpath.length(), absolutePath.lastIndexOf("."))
                    .replace(File.separator, ".");

            try {
                classList.add(classLoader.loadClass(classFullName));
            } catch (ClassNotFoundException e) {
                LOGGER.warn("默认类加载器在 {} 路径下没有找到 {} 类", classpath, classFullName);
            }
        }

        return classList;
    }

    private static final String PACKAGE_NAME = "priv/zxw/ratel/landlords/client/javafx/listener";

    private static List<Class<ClientListener>> findListenerInJarFile(JarFile jarFile) {
        List<Class<?>> classes = new ArrayList(10);
        ClassLoader defaultClassLoader = ClientListenerUtils.class.getClassLoader();
        Enumeration enumeration = jarFile.entries();

        while(enumeration.hasMoreElements()) {
            JarEntry jarEntry = (JarEntry)enumeration.nextElement();
            String entryName = jarEntry.getName();

            LOGGER.info(entryName);

            boolean isMaybeListenerClass = entryName.contains(PACKAGE_NAME) && isNormalClass(entryName);
            if (isMaybeListenerClass) {
                String classFullName = entryName.substring(entryName.lastIndexOf(PACKAGE_NAME), entryName.lastIndexOf(".")).replace("/", ".");

                try {
                    classes.add(defaultClassLoader.loadClass(classFullName));
                } catch (ClassNotFoundException var9) {
                    LOGGER.warn("默认类加载器在 {} jar包中下没有找到 {} 类", jarFile.getName(), classFullName);
                }
            }
        }

        return (List) classes.stream()
                             .filter((clazz) -> clazz.getSuperclass() == AbstractClientListener.class)
                             .collect(Collectors.toList());
    }
}