package pmedit.ext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pmedit.prefs.LocalDataDir;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ExtensionLoader {
    //All providers
    public static List<PmeExtension> providers() {
        List<PmeExtension> services = new ArrayList<>();
        ServiceLoader<PmeExtension> loader = ServiceLoader.load(PmeExtension.class);
        loader.forEach(services::add);
        return services;
    }

    public static boolean verifyJarSignature(Path jar, PublicKey pubKey) throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException, NoSuchProviderException {

        byte[] sigBytes = Files.readAllBytes(jar.resolveSibling(jar.getFileName() + ".sig"));
        Signature verifier = Signature.getInstance("SHA256withRSA");
        verifier.initVerify(pubKey);
        try (InputStream in = Files.newInputStream(jar)) {
            byte[] buffer = new byte[65536];
            int n;
            while ((n = in.read(buffer)) != -1) {
                verifier.update(buffer, 0, n); // Feed raw bytes
            }
        }

        return verifier.verify(sigBytes);
    }

    protected static void addJarsFromDir(Path thisClassJar, Path pluginPath, List<URL> urls, PublicKey publicKey, Logger LOG){
        try {
            if(Files.exists(pluginPath)) {
                LOG.debug("Searching: {}", pluginPath);
                try(Stream<Path> files = Files.list(pluginPath)) {
                    files.forEach(path -> {
                        if (path.getFileName().toString().toLowerCase().endsWith(".jar")) {
                            if (path.equals(thisClassJar)) {
                                LOG.trace("Ignore app Jar: {}", path);
                                return;
                            }
                            LOG.debug("Consider: {}", path);
                            long start = System.currentTimeMillis();
                            try {
                                if (verifyJarSignature(path, publicKey)) {
                                    urls.add(path.toUri().toURL());
                                }
                                LOG.debug("Verified in {} ms", System.currentTimeMillis() - start);
                            } catch (IOException | NoSuchAlgorithmException | SignatureException | InvalidKeyException |
                                     NoSuchProviderException e) {
                                LOG.error("Verify Error: {} on {} ", e.getMessage(), path);
                            }
                        }
                    });
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected static URL[] getPluginDirectories(PublicKey publicKey, Logger LOG){
        Path thisClassJar = Path.of(getClassSourceUri(PmeExtension.class));
        Path pluginPath = Files.isDirectory(thisClassJar)? thisClassJar : thisClassJar.getParent();
        List<URL> urls = new ArrayList<>();
        addJarsFromDir(thisClassJar, pluginPath, urls, publicKey, LOG);
        Path userDirPlugins = Path.of(LocalDataDir.getAppDataDir(), "plugins");
        addJarsFromDir(thisClassJar, userDirPlugins, urls, publicKey, LOG);
        return urls.toArray(URL[]::new);
    }

    protected static String[] getCurrentClassPath() {
        String classpath = System.getProperty("java.class.path");
        if (classpath == null || classpath.isEmpty()) {
            return new String[0];
        }

        // Use the path separator appropriate for the current OS
        String separator = File.pathSeparator;
        return classpath.split(java.util.regex.Pattern.quote(separator));
    }

    protected static URLClassLoader getExtensionClassLoader(PublicKey pubKey, Logger LOG){
        URL[] urls = getPluginDirectories(pubKey, LOG);
        LOG.debug("Looking for extension in classpath {}" , System.getProperty("java.class.path"));
        LOG.debug("Additional classpath for extensions {}" , (Object) urls);
        return new URLClassLoader(
                urls,
                PmeExtension.class.getClassLoader()
        );
    }

    protected static URI getClassSourceUri(Class clazz){
        try {
            return clazz.getProtectionDomain().getCodeSource().getLocation().toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String DEV_CLASS_PATH = File.separator +"modules" + File.separator + "extension-api" + File.separator + "target" + File.separator + "classes" + File.separator;
    public static PmeExtension get() {
        final Logger LOG = LoggerFactory.getLogger(ExtensionLoader.class);
        PmeExtension extensionInstance = null;

        long start = System.currentTimeMillis();
        PublicKey pubKey = ExtensionSignature.getPublicKey();
        URI thisClassSourceUrl = getClassSourceUri(PmeExtension.class);
        URLClassLoader classLoader = getExtensionClassLoader(pubKey, LOG);
        Set<URI> extensionJars = new LinkedHashSet<>();
        extensionJars.add(thisClassSourceUrl);
        if(thisClassSourceUrl.getPath().endsWith(DEV_CLASS_PATH)){
            final Pattern MATCH_DEV_CLASS_PATH = Pattern.compile("(modules|extensions)/[^/]+/target/classes$", Pattern.MULTILINE);
            for(String entry: getCurrentClassPath()){
                if(MATCH_DEV_CLASS_PATH.matcher(entry).find()){
                    extensionJars.add(new File(entry).toURI());
                }
            }
        }
        extensionJars.addAll(Arrays.stream(classLoader.getURLs()).map(u -> {
            try {
                return u.toURI();
            } catch (URISyntaxException e) {
                LOG.error("Invalid URL {} ",u, e);
            }
            return null;
        }).toList());

        ServiceLoader<PmeExtension> loader = ServiceLoader.load(PmeExtension.class, classLoader);
        LOG.info("Loaded extensions in {} ms",
                System.currentTimeMillis() - start
        );
        long startExtIteration = System.currentTimeMillis();
        Iterator<PmeExtension> entry = loader.iterator();
        while (true) {
            if(!entry.hasNext()){
                break;
            }
            PmeExtension ext = entry.next();
            if (extensionInstance == null || ext.priority() > extensionInstance.priority()) {
                long startValidate = System.currentTimeMillis();
                URI extJarUrl = getClassSourceUri(ext.getClass());
                boolean extensionValid = extensionJars.contains(extJarUrl);
                LOG.debug("Found PmeExtension {} priotity={} valid={} in {} ms ",
                        ext.getClass().getName(),
                        ext.priority(), extensionValid,
                        System.currentTimeMillis() - startValidate);
                if(extensionValid) {
                    extensionInstance = ext;
                }
            }
        }
        LOG.debug("Enumerated extensions in {} ms",
                System.currentTimeMillis() - startExtIteration
        );
        if(extensionInstance == null){
            RuntimeException e = new RuntimeException("Failed to find any configured extensions! Program is in non functional state!");
            LOG.error("PmeExtension.get()", e);
            throw e;
        }
        LOG.info("Loaded extension {} in {} ms",
                extensionInstance.getClass().getName(),
                System.currentTimeMillis() - start
        );

        return extensionInstance;
    }

}
