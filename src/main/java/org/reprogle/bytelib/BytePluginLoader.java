package org.reprogle.bytelib;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;

@SuppressWarnings("UnstableApiUsage")
public class BytePluginLoader implements PluginLoader {
    @Override
    public void classloader(PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();

        resolver.addDependency(new Dependency(new DefaultArtifact("dev.dejvokep:boosted-yaml:1.3.7"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("com.google.inject:guice:7.0.0"), null));

        classpathBuilder.addLibrary(resolver);
    }
}
