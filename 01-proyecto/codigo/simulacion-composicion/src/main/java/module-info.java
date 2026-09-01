module com.simulacion {
    requires javafx.controls;
    requires javafx.swing;
    requires java.desktop;
    requires org.jfree.jfreechart;

    opens com.simulacion to javafx.controls;
    opens com.simulacion.ejercicio1 to javafx.controls;
    opens com.simulacion.ejercicio2 to javafx.controls;
    opens com.simulacion.ejercicio3 to javafx.controls;
    opens com.simulacion.ejercicio4 to javafx.controls;
    opens com.simulacion.util to javafx.controls;

    exports com.simulacion;
    exports com.simulacion.ejercicio1;
    exports com.simulacion.ejercicio2;
    exports com.simulacion.ejercicio3;
    exports com.simulacion.ejercicio4;
    exports com.simulacion.util;
}
