package modele;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import java.util.Random;

/**
 * La classe Drone représente un drone dans l'environnement.
 */
public class Drone {
    private Circle drone;
    private Circle visionField;
    private Environnement environnement;
    private double VITESSE_MAX = 3.0;
    public double vitesseMax;
    public static final double VISION_RADIUS = 3;
    private Direction directionActuelle;
    private Random random;
    private double cibleIntrusX = -1;
    private double cibleIntrusY = -1;
    public static final double DETECTION_RADIUS = 3;
    public long dernierTempsDetection = 0;
    private long debutDetectionIntrus = 0;
    private double x; // Coordonnée X du drone
    private double y; // Coordonnée Y du drone
    private boolean mega;

    /**
     * Enumération représentant les directions possibles pour le déplacement du drone.
     */
    private enum Direction {
        HAUT, BAS, GAUCHE, DROITE
    }

    /**
     * Constructeur de la classe Drone.
     *
     * @param environnement L'environnement dans lequel le drone évolue.
     * @param mega          Indique si le drone est de type Mega.
     */
    public Drone(Environnement environnement, boolean mega) {
        this.environnement = environnement;
        this.drone = new Circle(0, 0, mega ? 7 : 5, mega ? Color.web("#A93226") : Color.RED);
        this.visionField = new Circle(0, 0, VISION_RADIUS * environnement.getEspace(), new Color(0, 0, 1, 0.12));
        this.random = new Random();
        this.directionActuelle = Direction.values()[random.nextInt(Direction.values().length)];
        this.mega = mega;
        this.vitesseMax = mega ? 2.0 : 3.0;
        setRandomPosition();
        majChampsDeVisionDrone();
    }


    public void move() { //Fonction générale pour le déplacement
        if (cibleIntrusX != -1 && cibleIntrusY != -1) {
            moveVersIntrus();
        } else {
            if (!peutBouger(directionActuelle)) {
                directionActuelle = choisirNouvelleDirection();
            }
            moveInDirection(directionActuelle);
        }
        majChampsDeVisionDrone();
        
            }

    private void moveVersIntrus() { //Fonction qui permet de se déplacer vers l'intrus, grace aux fonction choisirNouvelleDirection et moveInDirection
        double deltaX = cibleIntrusX - drone.getCenterX();
        double deltaY = cibleIntrusY - drone.getCenterY();
        Direction directionVersIntrus = choisirDirectionVersPoint(deltaX, deltaY);

        if (!peutBouger(directionVersIntrus)) {
            directionVersIntrus = choisirNouvelleDirection();
        }
        moveInDirection(directionVersIntrus);
    }
    
    
    
    public void positionner(double x, double y) { //Fonction pour positionner les dronnes, au centre d'une cellule
        this.drone.setCenterX(x);
        this.drone.setCenterY(y);
        majChampsDeVisionDrone();
    }
    
    public boolean estProche(double x, double y, double distanceMinimum) { //Fonction utile pour la detection de l'intrus
        double deltaX = this.getDroneX() - x;
        double deltaY = this.getDroneY() - y;
        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        return distance < distanceMinimum;
    }


    private Direction choisirDirectionVersPoint(double deltaX, double deltaY) { // Fonction qui permet de choisir une nouvelle direction pour un drone, dans le cas ou il doit se déplacer qq part, avec un point donné

        if (Math.abs(deltaX) > Math.abs(deltaY)) {
            return deltaX > 0 ? Direction.DROITE : Direction.GAUCHE;
        } else {
            return deltaY > 0 ? Direction.BAS : Direction.HAUT;
        }
    }

    
    
    private Direction choisirNouvelleDirection() { //Fonction qui permet de choisir une nouvelle direction pour un drone, dans le cas ou il doit se déplacer qq part
        Direction nouvelleDirection;
        do {
            nouvelleDirection = Direction.values()[random.nextInt(Direction.values().length)];
        } while (!peutBouger(nouvelleDirection));
        return nouvelleDirection;
    }

    private boolean peutBouger(Direction direction) { //Fonction qui utilise estUnMoveValide pour décider si le drone peut bouger ou non
        double newX = drone.getCenterX();
        double newY = drone.getCenterY();

        switch (direction) {
            case HAUT:
                newY -= VITESSE_MAX;
                break;
            case BAS:
                newY += VITESSE_MAX;
                break;
            case GAUCHE:
                newX -= VITESSE_MAX;
                break;
            case DROITE:
                newX += VITESSE_MAX;
                break;
        }

        return estUnMoveValide(newX, newY);
    }

    private boolean estUnMoveValide(double newX, double newY) { //Fonction qui défini si telle action est un move valide ou non pour un drone
        int row = (int) (newY / environnement.getEspace());
        int col = (int) (newX / environnement.getEspace());

        if (mega) {
            // Les méga drones peuvent traverser les murs verts clairs (valeur 1)
            return newX >= 0 && newX < environnement.getScene().getWidth() &&
                   newY >= 0 && newY < environnement.getScene().getHeight() &&
                   !environnement.estCelluleVerte(row, col);
        } else {
            // Les drones normaux ne peuvent pas traverser les murs verts clairs (valeur 1)
            return newX >= 0 && newX < environnement.getScene().getWidth() &&
                   newY >= 0 && newY < environnement.getScene().getHeight();
        }
    }


    private void moveInDirection(Direction direction) { //Fonction qui permettent aux drones de se déplacer dans une direction
        double deltaX = 0;
        double deltaY = 0;

        switch (direction) {
            case HAUT:
                deltaY = -VITESSE_MAX;
                break;
            case BAS:
                deltaY = VITESSE_MAX;
                break;
            case GAUCHE:
                deltaX = -VITESSE_MAX;
                break;
            case DROITE:
                deltaX = VITESSE_MAX;
                break;
        }

        double newX = drone.getCenterX() + deltaX;
        double newY = drone.getCenterY() + deltaY;

        if (estUnMoveValide(newX, newY)) {
            drone.setCenterX(newX);
            drone.setCenterY(newY);
        }
    }
    
    
    public boolean detecteIntrus(Intrus intrus) { //Fonction qui detecte l'intrus en fonction de son champs de vision, et de la fonction ligneDeVue, pour qu'il ne detecte pas l'intrus a travers un bloc vert
        double distance = Math.sqrt(Math.pow(getDroneX() - intrus.getIntrusX(), 2) + Math.pow(getDroneY() - intrus.getIntrusY(), 2));
        if (distance <= VISION_RADIUS * environnement.getEspace()) {
            return environnement.LigneDeVue(getDroneX(), getDroneY(), intrus.getIntrusX(), intrus.getIntrusY());
        }
        return false;
    }



    public void recevoirPositionIntrus(double intrusX, double intrusY) { //Fonction qui permet de recevoir l'information de la position de l'intrus
        this.cibleIntrusX = intrusX;
        this.cibleIntrusY = intrusY;
    }
    
    public void reinitialiserComportement() { //Fonction qui reintialise le comportement des drones, ils repassent en mode "recherche"
        this.cibleIntrusX = -1;
        this.cibleIntrusY = -1;
        directionActuelle = Direction.values()[random.nextInt(Direction.values().length)];
        System.out.println("Les drones reprennent leurs comportement initial");
    }


    private void setRandomPosition() { //Mise en application de la fonction d'en dessous pour le placement des drones
        int nombreDeDrones = environnement.getDrones().size();
        double angle = 360.0 / nombreDeDrones;
        double rayon = Math.min(environnement.getScene().getWidth(), environnement.getScene().getHeight()) / 3;

        int index = environnement.getDrones().indexOf(this);
        double angleRad = Math.toRadians(angle * index);

        double centreX = environnement.getScene().getWidth() / 2;
        double centreY = environnement.getScene().getHeight() / 2;

        double newX = centreX + rayon * Math.cos(angleRad);
        double newY = centreY + rayon * Math.sin(angleRad);

        int row = (int) (newY / environnement.getEspace());
        int col = (int) (newX / environnement.getEspace());

        if (!environnement.estCelluleVerte(row, col)) {
            drone.setCenterX(newX);
            drone.setCenterY(newY);
            majChampsDeVisionDrone();
        } else {
            // Si la position initiale est invalide, choisissez une position aléatoire
            positionAleatoire();
        }
    }

    private void positionAleatoire() { //Trouve une position aléatoire pour les drones, en respectant les contraires, pas de cellule verte, X blocs d'espace entre chaque drone (géré dans env)
        int espace = environnement.getEspace();
        double newX, newY;
        boolean positionValide;

        do {
            newX = random.nextDouble() * environnement.getScene().getWidth();
            newY = random.nextDouble() * environnement.getScene().getHeight();
            positionValide = !environnement.estCelluleVerte((int) (newY / espace), (int) (newX / espace));
        } while (!positionValide);

        drone.setCenterX(newX);
        drone.setCenterY(newY);
        majChampsDeVisionDrone();
    }
    
    
    private void majChampsDeVisionDrone() { // Fonction appelé en boucle qui met à jour le champ de vision visuel des drones (les cercles bleus)
        visionField.setCenterX(drone.getCenterX());
        visionField.setCenterY(drone.getCenterY());
    }
    
    //Getters Drones, champ de vision drone, cord drone x, cord drone y, dernier temps de detec, debut du detec...

    public Circle getDrone() {
        return drone;
    }

    public Circle getVisionField() {
        return visionField;
    }

    public double getDroneX() {
        return drone.getCenterX();
    }

    public double getDroneY() {
        return drone.getCenterY();
    }
    
    public long getDernierTempsDetection() {
        return this.dernierTempsDetection;
    }


    public long getDebutDetectionIntrus() {
        return this.debutDetectionIntrus;
    }
    
    
    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    
    //Setters temps de detection, vitesse
    
    public void setDebutDetectionIntrus(long temps) {
        this.debutDetectionIntrus = temps;
    }
    
	public void setDernierTempsDetection(long temps) {
	    this.dernierTempsDetection = temps;
	}
	
    public void setVitesse(double vitesse) {
        this.VITESSE_MAX = vitesse;
    }
}
