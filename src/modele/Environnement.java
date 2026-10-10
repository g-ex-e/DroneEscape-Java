package modele;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Arrays;

/**
 * La classe Environnement représente l'environnement dans lequel évoluent les drones et l'intrus.
 */
public class Environnement {

    // Déclaration des variables d'instance

    private int taille; // Taille de l'environnement (nombre de lignes/colonnes)
    private int espace; // Espace entre les cellules
    private Group troupe; // Groupe contenant tous les éléments graphiques de l'environnement
    private Scene scene; // La scène graphique
    private double pourcentageVert; // Pourcentage de cellules vertes dans l'environnement
    private double pourcentageCinq = 0.15; // Pourcentage de cellules contenant le nombre 5
    private List<Drone> drones; // Liste des drones présents dans l'environnement
    private Intrus intrus; // L'intrus présent dans l'environnement
    private int[][] matrice; // Matrice représentant l'environnement
    private Group coucheNoir; // Groupe graphique représentant la couche noire d'obscurcissement
    private double[][] transparence; // Tableau de transparence pour la couche noire
    private static final double ASSOMBRISSEMENT_PAR_TICK = 0.15; // Taux d'assombrissement à chaque mise à jour
    private static final double FREQUENCE_MISE_A_JOUR = 0.3; // Fréquence de mise à jour de la transparence
    private int intrusRow = -1; // Ligne où se trouve l'intrus
    private int intrusCol = -1; // Colonne où se trouve l'intrus
    public long debutDetectionIntrus = 0; // Temps de début de la détection de l'intrus
    private static final long TEMPS_DETECTION_DEFAITE = 5000; // Durée de détection pour la défaite (5 secondes)
    public static final double DETECTION_RADIUS = 3; // Rayon de détection des drones autour de l'intrus
    public long dernierTempsDetection = 0; // Dernier temps de détection de l'intrus
    private boolean intrusContinuouslyDetected = false; // Indicateur de détection continue de l'intrus
    private long detectionStartTime = 0; // Temps de début de la détection continue
    private double x; // Coordonnée X du drone
    private double y; // Coordonnée Y du drone

    /**
     * Constructeur de la classe Environnement.
     *
     * @param taille         Taille de l'environnement (nombre de lignes/colonnes)
     * @param espace         Espace entre les cellules
     * @param troupe         Groupe contenant tous les éléments graphiques de l'environnement
     * @param pourcentageVert Pourcentage de cellules vertes dans l'environnement
     * @param intrus         L'intrus présent dans l'environnement
     */
    public Environnement(int taille, int espace, Group troupe, double pourcentageVert, Intrus intrus) {
        // Initialisation des variables d'instance
        this.taille = taille;
        this.espace = espace;
        this.troupe = troupe;
        this.pourcentageVert = pourcentageVert;
        this.drones = new ArrayList<>();
        this.intrus = intrus;

        // Création de la scène graphique
        this.scene = new Scene(troupe, taille * espace, taille * espace); // Assurez-vous que cette ligne est avant de passer l'objet Environnement à d'autres objets.

        // Initialisation de la matrice représentant l'environnement
        this.matrice = new int[taille][taille];

        // Création des cellules et initialisation de l'environnement
        creerToutesCellules((int) (pourcentageVert * 100), (int) (pourcentageCinq * 100));
        initialiserEnvironnement();

        // Initialisation du mouvement des drones
        initDronesMovement();

        // Affichage de la matrice
        afficherMatrice();

        // Initialisation de la couche noire
        coucheNoir();

        // Placement aléatoire des drones
        placerDronesAleatoirement(); // S'assurer que les drones sont placés en premier

        // Placement de l'intrus aux coins en évitant les drones
        placerIntrusAuxCoinsAvecVerificationDrones();

        // Initialisation du tableau de transparence
        transparence = new double[taille][taille];
        for (int i = 0; i < taille; i++) {
            Arrays.fill(transparence[i], 1.0); // Initialiser toutes les cases comme complètement opaques
        }

        // Lancement de la mise à jour de la transparence
        Timeline timelineTransparence = new Timeline(
                new KeyFrame(Duration.seconds(FREQUENCE_MISE_A_JOUR), e -> mettreAJourTransparence()));
        timelineTransparence.setCycleCount(Timeline.INDEFINITE);
        timelineTransparence.play();
    }

        
  
    
    private void mettreAJourTransparence() { //Fonction globale qui permet de mettre à jour la transparence, pour avoir cette effet de visibilité "suivi"
        for (int i = 0; i < matrice.length; i++) {
            for (int j = 0; j < matrice[i].length; j++) {
                // Vérifier si la case est en dehors de la zone circulaire autour de l'intrus
                if (Math.sqrt(Math.pow(i - intrusRow, 2) + Math.pow(j - intrusCol, 2)) > 4) {
                    if (transparence[i][j] < 1.0) { // Uniquement pour les cases non complètement opaques
                        transparence[i][j] = Math.min(1.0, transparence[i][j] + ASSOMBRISSEMENT_PAR_TICK);
                    }
                }
                Rectangle rect = (Rectangle)coucheNoir.getChildren().get(i * matrice.length + j);
                rect.setFill(new Color(0, 0, 0, transparence[i][j]));
            }
        }
    }
    


    public void placerDronesAleatoirement() { //Fonction qui place les drones aléatoirement, avec une distance entre eux
        int nombreDeDrones = drones.size();
        double distanceMinimum = espace * 10; // La distance minimale entre deux drones

        for (int i = 0; i < nombreDeDrones; i++) {
            Drone drone = drones.get(i);
            boolean positionValide;

            do {
                // Générer une position aléatoire
                double x = Math.random() * (taille * espace);
                double y = Math.random() * (taille * espace);

                // Convertir en indices de matrice
                int row = (int) (y / espace);
                int col = (int) (x / espace);

                // Vérifier si la position est valide (pas sur une cellule verte et pas trop proche d'autres drones)
                positionValide = !estCelluleVerte(row, col);
                if (positionValide) {
                    for (Drone autreDrone : drones) {
                        if (autreDrone != drone && autreDrone.estProche(x, y, distanceMinimum)) {
                            positionValide = false;
                            break;
                        }
                    }
                }

                // Si la position est valide, placer le drone
                if (positionValide) {
                    drone.positionner(x, y);
                }
            } while (!positionValide);
        }
    }


    
    public void afficherVictoire() { //Fonction qui affiche la victoire
        Text texteVictoire = new Text("Félicitations ! Vous avez gagné.");
        texteVictoire.setFont(new Font(48)); // Vous pouvez ajuster la taille selon vos besoins
        texteVictoire.setFill(Color.RED); // Choisissez la couleur que vous préférez
        texteVictoire.setX(scene.getWidth() / 2 - texteVictoire.getLayoutBounds().getWidth() / 2);
        texteVictoire.setY(scene.getHeight() / 2);

        troupe.getChildren().add(texteVictoire);
    }



    private void creerToutesCellules(int pourcentageArbre, int pourcentageCinq) { //Fonction qui crée notre matrice avec le bon pourcentage d'arbre, les cellules grises,
    																			// Les zones d'extraction, et la donnés a recherché, le "backend" de initialiserEnvironnement
        Random random = new Random();
        for (int i = 0; i < matrice.length; i++) {
            for (int j = 0; j < matrice[i].length; j++) {
                int randomValue = random.nextInt(100);
                if (randomValue < pourcentageArbre) {
                    // La valeur aléatoire est inférieure au pourcentage pour les arbres, mettre 1 dans la case
                    matrice[i][j] = 1;
                } else if (randomValue < pourcentageArbre + pourcentageCinq) {
                    // La valeur aléatoire est dans la plage du pourcentage pour les 5, mettre 5 dans la case
                    matrice[i][j] = 5;
                } else {
                    // Sinon, mettre 0 dans la case
                    matrice[i][j] = 0;
                }
            }
        }

        //Placement de la donné à trouver
        int rowSpecial = random.nextInt(taille);
        int colSpecial = random.nextInt(taille);
        matrice[rowSpecial][colSpecial] = 3;

        //Placement des zones d'extractions
        int milieu = taille / 2;
        matrice[0][milieu - 1] = 4;
        matrice[0][milieu] = 4;

        matrice[taille - 1][milieu - 1] = 4;
        matrice[taille - 1][milieu] = 4;

        matrice[milieu - 1][0] = 4;
        matrice[milieu][0] = 4;

        matrice[milieu - 1][taille - 1] = 4;
        matrice[milieu][taille - 1] = 4;
    }


    public void revelerZone(double x, double y, int rayon) { //Fonction qui gère le revelage de zone, en lien avec la couche noir, pour l'intrus
        intrusRow = (int) (y / espace);
        intrusCol = (int) (x / espace);

        for (int i = 0; i < matrice.length; i++) {
            for (int j = 0; j < matrice[i].length; j++) {
                if (Math.sqrt(Math.pow(i - intrusRow, 2) + Math.pow(j - intrusCol, 2)) <= rayon) {
                    transparence[i][j] = 0.0; // Révéler complètement la case
                }
                Rectangle rect = (Rectangle)coucheNoir.getChildren().get(i * matrice.length + j);
                rect.setFill(new Color(0, 0, 0, transparence[i][j]));
            }
        }
    }



    private void initialiserEnvironnement() { // Fonction qui initalise l'environement et qui définit graphiquement chaque chose
    										// Exemple : ça définit que les valeurs 1 de la matrice sont les cases grises, les 4 les cases jaunes d'extraction etc..
        for (int i = 0; i < matrice.length; i++) {
            for (int j = 0; j < matrice[i].length; j++) {
                // Create a rectangle for each grid cell
                Rectangle pixel = new Rectangle(j * espace, i * espace, espace, espace);

                // Set the fill color based on the cell type
                if (matrice[i][j] == 1) {
                    pixel.setFill(Color.web("#90ed90")); // Green for "1"
                } else if (matrice[i][j] == 3) {
                    pixel.setFill(Color.BLUE); // Blue for "3"
                } else if (matrice[i][j] == 4) {
                    pixel.setFill(Color.YELLOW); // Yellow for "4"
                } 
                else if (matrice[i][j] == 5) {
                    pixel.setFill(Color.web("#145A32")); // Dark green for "5"
                }
                else {
                    pixel.setFill(Color.web("#696969")); // Grey by default
                }

                // Set the stroke (border) color to black and stroke width to 1
                pixel.setStroke(Color.BLACK);
                pixel.setStrokeWidth(1);

                troupe.getChildren().add(pixel);
            }
        }
    }



    public void changerCelluleEnGris(int row, int col) { //Fonction pour repasser une cellule en gris (quand on recupere la donné recherché, case bleu, la case redevient grise)
        matrice[row][col] = 0;
        Rectangle cellule = (Rectangle)troupe.getChildren().get(row * taille + col);
        cellule.setFill(Color.web("#696969")); // Couleur grise
    }
    

    
    private void coucheNoir() {  // Initialisation de la couche noire 
        coucheNoir = new Group();
        for (int i = 0; i < matrice.length; i++) {
            for (int j = 0; j < matrice[i].length; j++) {
                Rectangle pixelNoir = new Rectangle(j * espace, i * espace, espace, espace);
                pixelNoir.setFill(Color.BLACK);
                coucheNoir.getChildren().add(pixelNoir);
            }
        }
        this.troupe.getChildren().add(coucheNoir); // Ajouter la couche noire en haut
    }


    private void initDronesMovement() { //Fonction qui gère les mouvements des drones initialement, leurs modes "recherche"
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.1), event -> {
            for (Drone drone : drones) {
                drone.move();
            }
            checkProximite();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }


    public boolean LigneDeVue(double xDrone, double yDrone, double xIntrus, double yIntrus) { // Fonction permettant de calculer si il y a un bloc vert entre l'intrus et le drone qui le detecte 
    																							// Pour que les drones ne puissent pas detecter à travers les murs
    																							//On utilise l'algorithme de Bresenham, que j'ai trouvé sur internet.
        // Converti les coordonnées
        int startX = (int) (xDrone / espace);
        int startY = (int) (yDrone / espace);
        int endX = (int) (xIntrus / espace);
        int endY = (int) (yIntrus / espace);

        // Bresenham algorithme
        int dx = Math.abs(endX - startX);
        int dy = -Math.abs(endY - startY);
        int sx = startX < endX ? 1 : -1;
        int sy = startY < endY ? 1 : -1;
        int err = dx + dy, e2;

        if (estCelluleVerte(startX, startY)) {
            return false; // Initial position is blocked
        }

        while (true) {
            if (startX == endX && startY == endY) {
                return true; // Line of sight is clear
            }

            e2 = 2 * err;
            if (e2 >= dy) {
                if (e2 != dy && estCelluleVerte(startX + sx, startY)) {
                    return false; // Horizontal step would move into a green cell
                }
                err += dy;
                startX += sx;
            }
            if (e2 <= dx) {
                if (e2 != dx && estCelluleVerte(startX, startY + sy)) {
                    return false; // Vertical step would move into a green cell
                }
                err += dx;
                startY += sy;
            }
        }
    }

    public void checkProximite() { // Fonction checkProximite qui vérifie si l'intrus est a proximité de lui, si oui, il envoie l'informations aux autres drones
    								//La fonction permet aussi de gerer le cas de la défaite, si l'intrus est detecté pendant 5 secondes 
    								//Elle permet aussi de reintialiser le comportement des Drones, si ils ont detecté le drone, mais qu'ils ne le voit plus au bout de 15s
        long tempsActuel = System.currentTimeMillis();
        boolean intrusDetecte = false;

        for (Drone drone : drones) {
            if (drone.detecteIntrus(intrus)) {
                intrusDetecte = true;
                if (!intrusContinuouslyDetected) {
                    detectionStartTime = tempsActuel;
                    intrusContinuouslyDetected = true;
                    informerDrones(intrus.getIntrusX(), intrus.getIntrusY());
                }
                dernierTempsDetection = tempsActuel;
                break;
            }
        }

        if (intrusDetecte) {
            // Gestion de la défaite si l'intrus est détecté pendant plus de 5 secondes
            if (tempsActuel - detectionStartTime >= TEMPS_DETECTION_DEFAITE) {
                afficherDefaite();
            }
        } else if (intrusContinuouslyDetected) {
            if (tempsActuel - dernierTempsDetection > 15000) {
                reinitialiserComportementDrones();
                intrusContinuouslyDetected = false;
                dernierTempsDetection = tempsActuel; // Réinitialiser le timer après réinitialisation des drones
            }
        }
    }

 
    public void placerIntrusAuxCoinsAvecVerificationDrones() { //Fonction pour placer notre intru.
        Random random = new Random();
        
        int[][] coins = {
            {0, 0},       // Coin 1
            {taille - 1, 0},      // Coin 2
            {0, taille - 1},      // Coin 3
            {taille - 1, taille - 1}      // Coin 4
        };

        boolean positionValide;
        do {
            positionValide = true;
            // Choisir un coin au hasard
            int[] coinChoisi = coins[random.nextInt(coins.length)];
            double xCoin = coinChoisi[1] * espace; // Coordonnée X du coin
            double yCoin = coinChoisi[0] * espace; // Coordonnée Y du coin

            // Vérifier la proximité avec les drones
            for (Drone drone : drones) {
                if (drone.estProche(intrus.getX(), intrus.getY(), espace * 3)) { // Utilisez une distance appropriée
                    positionValide = false;
                    break;
                }
            }

            // Si la position est valide, placer l'intrus
            if (positionValide) {
                intrus.positionner(xCoin, yCoin);
            } else {
                // Attendre un peu avant de réessayer si aucun coin n'est disponible
                try {
                    Thread.sleep(1000); // Attendre 1 seconde
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } while (!positionValide);
    }


    private void afficherDefaite() { //Fonction qui affiche la défaite, si appeler par par la fonction checkProximite si l'intrus est detecté pendant 5s.
        Text texteDefaite = new Text("Défaite ! Vous avez perdu.");
        texteDefaite.setFont(new Font(48));
        texteDefaite.setFill(Color.RED);
        texteDefaite.setX(scene.getWidth() / 2 - texteDefaite.getLayoutBounds().getWidth() / 2);
        texteDefaite.setY(scene.getHeight() / 2);
        troupe.getChildren().add(texteDefaite);
    }
    
    public void reinitialiserComportementDrones() { // Fonction qui reintialise le comportement des drones, ils retournent en mode "recherche"
        for (Drone drone : drones) {
            drone.reinitialiserComportement();
        }
    }
    
    public void afficherMatrice() { //Fonction pour afficher la matrice, pour débug
        for (int i = 0; i < matrice.length; i++) {
            for (int j = 0; j < matrice[i].length; j++) {
                System.out.print(matrice[i][j] + " ");
            }
            System.out.println();
        }
    }


    public boolean estCelluleVerte(int row, int col) { //Fonction très utile, qui nous permet de savoir si le bloc à côté est une cellule verte ou non
        // Vérifiez si les coordonnées sont hors limites avant de vérifier la cellule verte 1 ou verte foncé 5
        if (row < 0 || col < 0 || row >= taille || col >= taille) {
            return false;
        }
        return matrice[row][col] == 1 || matrice[row][col] == 5; // 1 est le code pour les cellules vertes, 5 est le code pour les cellules 5
    }



    public void informerDrones(double intrusX, double intrusY) { //Fonction qui envoie l'informations aux autres drones de la position de l'intrus
        for (Drone drone : drones) {
            drone.recevoirPositionIntrus(intrusX, intrusY);
        }
    }
    

    public void addDrone(Drone drone) { //Ajouter des drones
        drones.add(drone);
    }
    
    // Getters Troupe, Taille, Scene, Espace, Drones, Cellules...
    
    public Group getTroupe() {
        return troupe;
    }

    public int getTaille() {
        return taille;
    }
    
    public Scene getScene() {
        return scene;
    }

    public int getEspace() {
        return espace;
    }
    
    public List<Drone> getDrones() {
        return drones;
    }
    
    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }
    
    public int getCellule(int row, int col) {
        return matrice[row][col];
    }
    

}
