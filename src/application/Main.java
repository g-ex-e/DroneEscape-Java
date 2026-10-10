package application;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import modele.Environnement;
import modele.Intrus;
import modele.Drone;

public class Main extends Application {
    // Déclarez une variable d'instance pour stocker l'environnement
    private Environnement env;

    @Override
    public void start(Stage primaryStage) {
        // Paramètres de l'environnement
        int taille = 54; // Doublez le nombre de carrés par ligne et par colonne
        int espace = 13; // Réduisez la taille de chaque carré
        double pourcentageVert = 0.15;

        // Création de l'intrus
        Intrus intrus = new Intrus();
        Group root = new Group();

        // Initialisation de l'environnement
        env = new Environnement(taille, espace, root, pourcentageVert, intrus);
        intrus.setEnvironnement(env);

        // Ajout de l'intrus à la scène
        root.getChildren().add(intrus.getIntrus());

        // Nombre de drones réguliers et méga-drones
        int nombreDeDrones = 5;
        int nombreMegaDrones = 5;

        // Ajout de drones réguliers à l'environnement
        for (int i = 0; i < nombreDeDrones; i++) {
            Drone drone = new Drone(env, false);
            env.addDrone(drone);
            root.getChildren().add(drone.getDrone());
            root.getChildren().add(drone.getVisionField());
        }

        // Ajout de méga-drones à l'environnement
        for (int i = 0; i < nombreMegaDrones; i++) {
            Drone drone = new Drone(env, true);
            env.addDrone(drone);
            root.getChildren().add(drone.getDrone());
            root.getChildren().add(drone.getVisionField());
        }

        // Utilisation de la scène de l'environnement plutôt que d'en créer une nouvelle
        Scene scene = env.getScene();

        // Gestion des événements clavier pour l'intrus
        scene.setOnKeyPressed(event -> intrus.handleKeyPress(event));
        
        // Placement aléatoire des drones dans l'environnement
        env.placerDronesAleatoirement();

        // Configuration de la fenêtre principale
        primaryStage.setScene(scene);
        primaryStage.setWidth(716.5);
        primaryStage.setHeight(740);
        primaryStage.show();
    }
}
