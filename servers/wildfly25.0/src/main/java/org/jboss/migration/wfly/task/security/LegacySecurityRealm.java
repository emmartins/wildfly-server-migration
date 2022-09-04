package org.jboss.migration.wfly.task.security;

public class LegacySecurityRealm {

    String name;
    Authentication authentication;
    Authorization authorization;
    ServerIdentities serverIdentities;

    @Override
    public String toString() {
        return name;
    }

    public static class Authentication {
        Local local;
        Properties properties;

        @Override
        public String toString() {
            return "Authentication{" +
                    "local=" + local +
                    ", properties=" + properties +
                    '}';
        }
    }

    public static class Authorization {
        boolean mapGroupsToRoles = true;
        Properties properties;
        @Override
        public String toString() {
            return "Authorization{" +
                    "mapGroupsToRoles=" + mapGroupsToRoles +
                    ", properties=" + properties +
                    '}';
        }
    }

    public static class Properties {
        String path;
        String relativeTo;
        boolean plainText = false;

        @Override
        public String toString() {
            return "Properties{" +
                    "path='" + path + '\'' +
                    ", relativeTo='" + relativeTo + '\'' +
                    ", plainText=" + plainText +
                    '}';
        }
    }

    public static class Local {
        String defaultUser;
        String allowedUsers;
        boolean skipGroupLoading;

        @Override
        public String toString() {
            return "Local{" +
                    "defaultUser='" + defaultUser + '\'' +
                    ", allowedUsers='" + allowedUsers + '\'' +
                    ", skipGroupLoading=" + skipGroupLoading +
                    '}';
        }
    }

    public static class ServerIdentities {
        String sslKeystorePath;
        String sslKeystoreRelativeTo;
        String sslKeystoreKeystorePassword;
        String sslKeystoreAlias;
        String sslKeystoreKeyPassword;
        String sslKeystoreGenerateSelfSignedCertificateHost;
        @Override
        public String toString() {
            return "ServerIdentities{" +
                    "sslKeystorePath='" + sslKeystorePath + '\'' +
                    ", sslKeystoreRelativeTo='" + sslKeystoreRelativeTo + '\'' +
                    ", sslKeystoreKeystorePassword='" + sslKeystoreKeystorePassword + '\'' +
                    ", sslKeystoreAlias='" + sslKeystoreAlias + '\'' +
                    ", sslKeystoreKeyPassword='" + sslKeystoreKeyPassword + '\'' +
                    ", sslKeystoreGenerateSelfSignedCertificateHost='" + sslKeystoreGenerateSelfSignedCertificateHost + '\'' +
                    '}';
        }
    }
}
