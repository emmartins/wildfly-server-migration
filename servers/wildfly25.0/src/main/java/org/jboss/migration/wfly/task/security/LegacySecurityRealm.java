package org.jboss.migration.wfly.task.security;

public class LegacySecurityRealm {

    private final String name;
    private Authentication authentication;
    private Authorization authorization;
    private ServerIdentities serverIdentities;

    public LegacySecurityRealm(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Authentication getAuthentication() {
        return authentication;
    }

    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }

    public Authorization getAuthorization() {
        return authorization;
    }

    public void setAuthorization(Authorization authorization) {
        this.authorization = authorization;
    }

    public ServerIdentities getServerIdentities() {
        return serverIdentities;
    }

    public void setServerIdentities(ServerIdentities serverIdentities) {
        this.serverIdentities = serverIdentities;
    }

    public String getElytronSecurityDomainName() {
        return name+"-securitydomain";
    }

    public String getElytronPropertiesRealmName() {
        return name+"-propertiesrealm";
    }

    public String getElytronHttpAuthenticationFactoryName() {
        return name+"-httpAuthenticationFactory";
    }

    public String getElytronSaslAuthenticationFactoryName() {
        return name+"-saslAuthenticationFactory";
    }

    @Override
    public String toString() {
        return "LegacySecurityRealm{" +
                "name='" + name + '\'' +
                ", authentication=" + authentication +
                ", authorization=" + authorization +
                ", serverIdentities=" + serverIdentities +
                '}';
    }

    public static class Authentication {
        private Local local;
        private Properties properties;

        public Local getLocal() {
            return local;
        }

        public void setLocal(Local local) {
            this.local = local;
        }

        public Properties getProperties() {
            return properties;
        }

        public void setProperties(Properties properties) {
            this.properties = properties;
        }

        @Override
        public String toString() {
            return "Authentication{" +
                    "local=" + local +
                    ", properties=" + properties +
                    '}';
        }
    }

    public static class Authorization {
        private boolean mapGroupsToRoles = true;
        private Properties properties;

        public boolean isMapGroupsToRoles() {
            return mapGroupsToRoles;
        }

        public void setMapGroupsToRoles(boolean mapGroupsToRoles) {
            this.mapGroupsToRoles = mapGroupsToRoles;
        }

        public Properties getProperties() {
            return properties;
        }

        public void setProperties(Properties properties) {
            this.properties = properties;
        }

        @Override
        public String toString() {
            return "Authorization{" +
                    "mapGroupsToRoles=" + mapGroupsToRoles +
                    ", properties=" + properties +
                    '}';
        }
    }

    public static class Properties {
        private String path;
        private String relativeTo;
        private boolean plainText;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getRelativeTo() {
            return relativeTo;
        }

        public void setRelativeTo(String relativeTo) {
            this.relativeTo = relativeTo;
        }

        public boolean isPlainText() {
            return plainText;
        }

        public void setPlainText(boolean plainText) {
            this.plainText = plainText;
        }

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
        private String defaultUser;
        private String allowedUsers;
        private boolean skipGroupLoading;

        public String getDefaultUser() {
            return defaultUser;
        }

        public void setDefaultUser(String defaultUser) {
            this.defaultUser = defaultUser;
        }

        public String getAllowedUsers() {
            return allowedUsers;
        }

        public void setAllowedUsers(String allowedUsers) {
            this.allowedUsers = allowedUsers;
        }

        public boolean isSkipGroupLoading() {
            return skipGroupLoading;
        }

        public void setSkipGroupLoading(boolean skipGroupLoading) {
            this.skipGroupLoading = skipGroupLoading;
        }

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
        private String sslKeystorePath;
        private String sslKeystoreRelativeTo;
        private String sslKeystoreKeystorePassword;
        private String sslKeystoreAlias;
        private String sslKeystoreKeyPassword;
        private String sslKeystoreGenerateSelfSignedCertificateHost;

        public String getSslKeystorePath() {
            return sslKeystorePath;
        }

        public void setSslKeystorePath(String sslKeystorePath) {
            this.sslKeystorePath = sslKeystorePath;
        }

        public String getSslKeystoreRelativeTo() {
            return sslKeystoreRelativeTo;
        }

        public void setSslKeystoreRelativeTo(String sslKeystoreRelativeTo) {
            this.sslKeystoreRelativeTo = sslKeystoreRelativeTo;
        }

        public String getSslKeystoreKeystorePassword() {
            return sslKeystoreKeystorePassword;
        }

        public void setSslKeystoreKeystorePassword(String sslKeystoreKeystorePassword) {
            this.sslKeystoreKeystorePassword = sslKeystoreKeystorePassword;
        }

        public String getSslKeystoreAlias() {
            return sslKeystoreAlias;
        }

        public void setSslKeystoreAlias(String sslKeystoreAlias) {
            this.sslKeystoreAlias = sslKeystoreAlias;
        }

        public String getSslKeystoreKeyPassword() {
            return sslKeystoreKeyPassword;
        }

        public void setSslKeystoreKeyPassword(String sslKeystoreKeyPassword) {
            this.sslKeystoreKeyPassword = sslKeystoreKeyPassword;
        }

        public String getSslKeystoreGenerateSelfSignedCertificateHost() {
            return sslKeystoreGenerateSelfSignedCertificateHost;
        }

        public void setSslKeystoreGenerateSelfSignedCertificateHost(String sslKeystoreGenerateSelfSignedCertificateHost) {
            this.sslKeystoreGenerateSelfSignedCertificateHost = sslKeystoreGenerateSelfSignedCertificateHost;
        }

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
