package dev.reactant.resourcestirrer.resourceloader

import org.reflections.Reflections
import org.reflections.scanners.ResourcesScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import org.reflections.util.FilterBuilder
import java.io.InputStream
import java.util.regex.Pattern


class ClassLoaderResourceLoader(val classLoader: ClassLoader) : ResourceLoader {
    override fun getResourceFile(resourcePath: String): InputStream? {
        return classLoader.getResourceAsStream(resourcePath)
    }

    override fun getResourceFiles(resourcePath: String): Set<String> {
        val reflections = Reflections(ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(resourcePath, classLoader))
                .setScanners(ResourcesScanner())
                .filterInputsBy(FilterBuilder().includePackage(resourcePath))
        );
        val fileNames = reflections.getResources(Pattern.compile(".*\\.(png|gif)(\\.mcmeta)?"))
        return fileNames;
    }
}
