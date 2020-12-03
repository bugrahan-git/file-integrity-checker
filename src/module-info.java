module mod {
    requires sun.security.tools.keytool.Main;
    opens keytool to sun.security.tools.keytool.Main;
    exports keytool;
}
