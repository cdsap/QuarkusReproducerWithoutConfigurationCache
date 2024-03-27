package org.example;

public enum LaunchMode {

    /**
     * A normal production build. At the moment this can be both native image or
     * JVM mode, but eventually these will likely be split
     */
    NORMAL("prod", "quarkus.profile"),
    /**
     * quarkus:dev or an IDE launch (when we support IDE launch)
     */
    DEVELOPMENT("dev", "quarkus.profile"),
    /**
     * a test run
     */
    TEST("test", "quarkus.test.profile");

    public boolean isDevOrTest() {
        return this != NORMAL;
    }

    private final String defaultProfile;
    private final String profileKey;

    LaunchMode(final String defaultProfile, final String profileKey) {
        this.defaultProfile = defaultProfile;
        this.profileKey = profileKey;
    }
}

