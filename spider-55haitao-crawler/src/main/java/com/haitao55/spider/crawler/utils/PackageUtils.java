package com.haitao55.spider.crawler.utils;

import java.io.File;
import java.io.FileFilter;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 功能：扫描当前jar包，获取给定package下的所有class
 * 
 * @author Arthur.Liu
 * @time 2016年6月1日 上午11:08:42
 * @version 1.0
 */
public class PackageUtils {

    private static final Logger logger = LoggerFactory
            .getLogger(Constants.LOGGER_NAME_SYSTEM);

    public static Set<String> getClasses(String packageName) {
        return getClasses(packageName, Charset.defaultCharset().toString());
    }

    public static Set<String> getClasses(String packageName, String charset) {
        if (StringUtils.isBlank(packageName)) {
            return Collections.emptySet();
        }
        try {
            boolean recursive = true;
            Set<String> classes = new LinkedHashSet<String>();
            String packageDirName = packageName.replace('.', '/');
            Enumeration<URL> dirs = Thread.currentThread()
                    .getContextClassLoader().getResources(packageDirName);
            for (; dirs.hasMoreElements();) {
                URL url = dirs.nextElement();
                String protocol = url.getProtocol();
                if (StringUtils.equalsIgnoreCase("jar", protocol)) {
                    JarFile jar = ((JarURLConnection) url.openConnection())
                            .getJarFile();
                    findAndAddClassesInPackageByJar(packageName, jar,
                            recursive, classes);
                } else if (StringUtils.equalsIgnoreCase("file", protocol)) {
                    String filePath = URLDecoder.decode(url.getFile(), charset);
                    findAndAddClassesInPackageByFile(packageName, filePath,
                            recursive, classes);
                }
            }
            return classes;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return Collections.emptySet();
    }

    private static void findAndAddClassesInPackageByFile(String packageName,
            String filePath, final boolean recursive, Set<String> classes) {
        File dir = new File(filePath);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        // 获取目录下的所有class文件
        File[] files = dir.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return (recursive && file.isDirectory())
                        || (file.getName().endsWith(".class"));
            }
        });

        for (File file : files) {
            // 如果是目录 则继续扫描
            if (file.isDirectory()) {
                findAndAddClassesInPackageByFile(
                        packageName + "." + file.getName(),
                        file.getAbsolutePath(), recursive, classes);
            } else {
                // 如果是java类文件 去掉后面的.class 只留下类名
                String className = file.getName().substring(0,
                        file.getName().length() - 6);
                classes.add(packageName + '.' + className);
            }
        }
    }

    private static void findAndAddClassesInPackageByJar(String packageName,
            JarFile jar, boolean recursive, Set<String> classes) {
        String packageDirName = packageName.replace('.', '/');
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            // 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            // 如果是以/开头的
            if (name.startsWith("/")) {
                name = name.substring(1);
            }
            if (!name.startsWith(packageDirName)) {
                continue;
            }
            // 如果是一个.class文件 而且不是目录
            if (name.endsWith(".class") && !entry.isDirectory()) {
                // 去掉后面的".class" 获取真正的类名
                String className = name.substring(0, name.length() - 6);
                classes.add(className.replace('/', '.'));
            }
        }
    }
}
