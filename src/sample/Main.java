package sample;

import javafx.application.Application;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

/**
 * @author func 12.12.2019
 */
public strictfp class Main extends Application {

    private Font font = Font.font(24);
    private PhongMaterial black = createMaterial(Color.BLACK);

    @Override
    public void start(Stage primaryStage) {
        Group group = new Group();
        Camera camera = new PerspectiveCamera(true);
        camera.setFarClip(500000);

        Cylinder axis = new Cylinder(1, 100000);
        axis.setRotationAxis(Rotate.Z_AXIS);
        axis.setRotate(90);
        axis.setRotationAxis(Rotate.X_AXIS);
        axis.setRotate(90);
        axis.setMaterial(createMaterial(Color.LIGHTBLUE));

        Cylinder ordinate = new Cylinder(1, 100000);
        ordinate.setMaterial(createMaterial(Color.RED));

        Cylinder z = new Cylinder(1, 100000);
        z.setRotationAxis(Rotate.Z_AXIS);
        z.setRotate(90);
        z.setMaterial(createMaterial(Color.GREEN));

        int count = 4000;
        int r = 200;
        int length = 150;

        Point3D previous = null;
        Cylinder[] lines = new Cylinder[count - 1];

        for (int p = 0; p < count; p++) {
            double t = Math.toRadians(p);
            double f = Math.exp(Math.cos(t)) - 2 * Math.cos(4 * t) + Math.pow(Math.sin(t / 12), 5);
            Point3D now = new Point3D(
                    sin(t) * f * r,
                    -cos(t) * f * r,
                    t
            );
            if (p > 0)
                lines[p - 1] = createConnection(previous, now);
            previous = now;
        }

        Rectangle rect = new Rectangle(-1000, -1000, 2000, 2000);
        rect.setOpacity(0);

        group.getChildren().addAll(rect);
        group.getChildren().addAll(setSize(length, r, 0, 1,  0, false));
        group.getChildren().addAll(setSize(length, r, 1, 0,  0, false));
        group.getChildren().addAll(setSize(length, r, 0, 0,  1, true));
        group.getChildren().addAll(ordinate, z, axis);
        group.getChildren().addAll(lines);

        Scene scene = new Scene(group, 1000, 1000);
        scene.setCamera(camera);

        primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, event -> {

            double y = camera.getScaleY();
            double p = camera.getScaleZ();
            double k = 100;

            double xzLength = cos(p) * k;
            double dx = xzLength * sin(y) * (camera.getRotate() % 360 >= 90 && camera.getRotate() % 360 <= 270 ? -1 : 1);
            double dz = xzLength * cos(y) * (camera.getRotate() % 360 >= 90 && camera.getRotate() % 360 <= 270 ? -1 : 1);
            double dy = k * sin(p);

            System.out.println(camera.getRotate());

            switch (event.getCode()) {
                case W:
                    camera.translateYProperty().set(camera.getTranslateY() - dy);
                    break;
                case S:
                    camera.translateYProperty().set(camera.getTranslateY() + dy);
                    break;
                case D:
                    camera.translateXProperty().set(camera.getTranslateX() + dx);
                    camera.translateZProperty().set(camera.getTranslateZ() - dz);
                    break;
                case A:
                    camera.translateXProperty().set(camera.getTranslateX() - dx);
                    camera.translateZProperty().set(camera.getTranslateZ() + dz);
                    break;
                case E:
                    camera.setRotationAxis(Rotate.Y_AXIS);
                    camera.setRotate(camera.getRotate() + 20);
                    break;
            }
        });

        primaryStage.addEventHandler(MouseEvent.MOUSE_MOVED, event -> {
            Tooltip t = new Tooltip("X: " + event.getX()/200 + "; Y: " + -event.getY()/200);
            Tooltip.install(rect, t);
        });

        primaryStage.addEventHandler(ScrollEvent.SCROLL, event -> {
            double delta = event.getDeltaY();
            camera.translateZProperty().set(camera.getTranslateZ() + delta);
            camera.translateXProperty().set(camera.getTranslateX() + (camera.getRotate() > 180 ? -event.getX() / 25 : event.getX() / 25));
            camera.translateYProperty().set(camera.getTranslateY() + (camera.getRotate() > 180 ? -event.getY() / 25 : event.getY() / 25));
        });
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private PhongMaterial createMaterial(Color color) {
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(color);
        material.setSpecularColor(color);
        return material;
    }

    private Cylinder createConnection(Point3D origin, Point3D target) {
        Point3D yAxis = new Point3D(0, 1, 0);
        Point3D diff = target.subtract(origin);

        Point3D mid = target.midpoint(origin);
        Cylinder line = new Cylinder(.9, diff.magnitude() + .2);
        line.setMaterial(black);

        line.getTransforms().addAll(
                new Translate(mid.getX(), mid.getY(), mid.getZ()),
                new Rotate(-Math.toDegrees(Math.acos(diff.normalize().dotProduct(yAxis))), diff.crossProduct(yAxis))
        );
        return line;
    }

    private TextArea[] setSize(int length, int radius, int XProperty, int YProperty, int ZProperty, boolean normal) {
        TextArea[] numbers = new TextArea[length];
        for (int i = -length/2; i < length/2; i++) {
            numbers[i+length/2] = new TextArea(-i + "");
            numbers[i+length/2].translateXProperty().set(-i*radius*XProperty);
            numbers[i+length/2].translateYProperty().set(i*radius*YProperty);
            numbers[i+length/2].translateZProperty().set(i*radius*ZProperty);
            numbers[i+length/2].setMaxWidth(radius);
            numbers[i+length/2].setMaxHeight(radius);
            numbers[i+length/2].setFont(font);
            numbers[i+length/2].setEditable(false);
            numbers[i+length/2].setMouseTransparent(true);
            numbers[i+length/2].setFocusTraversable(false);
            if (normal) {
                numbers[i+length/2].translateYProperty().set(-50);
                numbers[i+length/2].setRotationAxis(Rotate.X_AXIS);
                numbers[i+length/2].setRotate(270);
            }
        }
        return numbers;
    }

    public static void main(String[] args) {
        launch(args);
    }
}