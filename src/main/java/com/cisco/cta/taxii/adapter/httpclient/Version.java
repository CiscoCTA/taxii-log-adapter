package com.cisco.cta.taxii.adapter.httpclient;


/**
 * Provides API version.
 */
public final class Version {

    public static final String UNKNOWN = "UNKNOWN";
    private static final String IMPL_VERSION;


    static {
        String version = Version.class.getPackage().getImplementationVersion();
        IMPL_VERSION = (version == null) ? UNKNOWN : version;
    }

    /**
     * @return Implementation version specified in the MANIFEST.MF
     */
    public static String getImplVersion() {
        return IMPL_VERSION;
    }

}
