module com.dupedb.api {
    requires java.net.http;
    requires java.desktop;
    requires jdk.httpserver;
    requires com.google.gson;

    exports com.dupedb.api;
    exports com.dupedb.api.auth;
    exports com.dupedb.api.api;
    exports com.dupedb.api.model;
    exports com.dupedb.api.exception;
}
