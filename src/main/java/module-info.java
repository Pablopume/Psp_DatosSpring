module javafx {
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires MaterialFX;
    requires lombok;
    requires org.apache.logging.log4j;
    requires jakarta.cdi;
    requires jakarta.inject;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.yaml;
    requires com.google.gson;
    requires retrofit2;
    requires retrofit2.converter.moshi;
    requires moshi;
    requires okhttp3;
    requires jakarta.xml.bind;
    requires io.vavr;
    requires java.sql;
    requires com.zaxxer.hikari;
    requires commons.dbcp2;
    requires jakarta.annotation;

    exports ui.screens.orders.deleteorders;
    exports services.impl;
    exports dao.imp;
    exports model;
    exports ui.main to javafx.graphics;
    exports ui.screens.principal;
    exports ui.screens.login;
    exports ui.screens.common;
    exports ui.screens.customers.addcustomer;
    exports ui.screens.customers.showcustomers;
    exports ui.screens.customers.deletecustomer;
    exports ui.screens.customers.editcustomers;
    exports ui.screens.orders.listorders;
    exports ui.screens.orders.addorder;
    exports configuration;
    exports common;
    opens ui.screens.common;
    opens ui.screens.login;
    opens ui.screens.customers.editcustomers;
    opens ui.screens.principal;
    opens ui.main;
    opens ui.screens.orders.listorders;
    opens css;
    opens fxml;
    opens configuration;
    opens ui.screens.orders.addorder;
    opens ui.screens.orders.editorder;
    opens ui.screens.customers.addcustomer;
    opens ui.screens.customers.deletecustomer;
    opens ui.screens.customers.showcustomers;
    opens ui.screens.orders.deleteorders;
    exports dao;
    exports model.errors;
    exports services;
    opens dao.imp;
}
