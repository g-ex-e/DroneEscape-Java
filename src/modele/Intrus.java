	package modele;
	
	import javafx.animation.KeyFrame;
	import javafx.animation.Timeline;
	import javafx.scene.input.KeyCode;
	import javafx.scene.input.KeyEvent;
	import javafx.scene.paint.Color;
	import javafx.scene.shape.Circle;
	import javafx.util.Duration;
	import java.util.Random;
	
	public class Intrus {
	    private Circle intrus;
	    private Environnement environnement;
	    private double posX;
	    private double posY;
	    private static final double DEPLACEMENT = 13.0;
	    private boolean enClignotement = false;
	    private double x;
	    private double y;

	
	    public void setInitialPosition() { // Fonction qui fait apparaitre l'intrus
	        Random random = new Random();
	        boolean positionValide;
	
	        do {
	            int row = random.nextInt(environnement.getTaille());
	            int col = random.nextInt(environnement.getTaille());
	
	            if (!environnement.estCelluleVerte(row, col)) {
	                // Utilisez la nouvelle fonction pour positionner l'intrus
	                positionnerIntrusAuCentre(row, col);
	                positionValide = true;
	            } else {
	                positionValide = false;
	            }
	        } while (!positionValide);
	    }
	
	
	    public void handleKeyPress(KeyEvent event) { //Déplacement clavier de l'intrus
	        KeyCode keyCode = event.getCode();
	        double deltaX = 0;
	        double deltaY = 0;

	        switch (keyCode) {
	            case UP:
	                deltaY = -DEPLACEMENT;
	                break;
	            case DOWN:
	                deltaY = DEPLACEMENT;
	                break;
	            case LEFT:
	                deltaX = -DEPLACEMENT;
	                break;
	            case RIGHT:
	                deltaX = DEPLACEMENT;
	                break;
	        }

	        double newX = posX + deltaX;
	        double newY = posY + deltaY;

	        if (estUnMoveValide(newX, newY)) {
	            posX = newX;
	            posY = newY;
	            intrus.setCenterX(posX);
	            intrus.setCenterY(posY);

	            // Révéler la zone autour de la nouvelle position de l'intrus
	            environnement.revelerZone(posX, posY, 4); // Rayon de 4 cases

	            // Vérifier si l'intrus se déplace sur la cellule bleue
	            int row = (int) (newY / environnement.getEspace());
	            int col = (int) (newX / environnement.getEspace());
	            if (environnement.getCellule(row, col) == 3) {
	                environnement.changerCelluleEnGris(row, col);
	                commencerClignotement();
	            }

	            // Vérifier si l'intrus a gagné
	            victoire();
	        }
	    }

	    private void commencerClignotement() { //Fonction qui commence le clignotement de l'intrus, si il a récupéré la donnés
	        enClignotement = true;
	        Timeline clignotement = new Timeline(
	            new KeyFrame(Duration.seconds(1),
	                e -> intrus.setFill(intrus.getFill().equals(Color.web("#1e90ff")) ? Color.BLUE : Color.web("#1e90ff"))
	            )
	        );
	        clignotement.setCycleCount(Timeline.INDEFINITE);
	        clignotement.play();
	        clignotement.setOnFinished(event -> enClignotement = false);
	    }
	    
	    
	    
	    private void positionnerIntrusAuCentre(int row, int col) { //Fonction qui s'assure de placer l'intrus au centre d'une cellule
	        double xCentre = col * environnement.getEspace() + environnement.getEspace() / 2.0;
	        double yCentre = row * environnement.getEspace() + environnement.getEspace() / 2.0;
	        posX = xCentre;
	        posY = yCentre;
	        intrus.setCenterX(posX);
	        intrus.setCenterY(posY);
	    }
	    
	    public void victoire() { //Fonction victoire, si l'intrus arrive sur une case d'extraction avec la donnés,
	        int row = (int) (posY / environnement.getEspace());
	        int col = (int) (posX / environnement.getEspace());

	        if (estEnClignotement() && environnement.getCellule(row, col) == 4) {
	            environnement.afficherVictoire();
	        }
	    }
	    
	
	
	    private boolean estUnMoveValide(double newX, double newY) { //Fonction qui détermine si c'est un move valide pour l'intrus
	        if (newX >= 0 && newX <= environnement.getScene().getWidth() &&
	            newY >= 0 && newY <= environnement.getScene().getHeight()) {
	            int row = (int) (newY / environnement.getEspace());
	            int col = (int) (newX / environnement.getEspace());
	
	            return !environnement.estCelluleVerte(row, col);
	        }
	        return false;
	    }
	    
	    //Getters Setters et init
	    
	    public Intrus(Environnement environnement) {
	        this.environnement = environnement;
	        initIntrus();
	    }
	
	    public Intrus() {
	        initIntrus();
	        
	    }
	    
	
	    public void setEnvironnement(Environnement environnement) {
	        this.environnement = environnement;
	        environnement.getScene().setOnKeyPressed(this::handleKeyPress);
	        setInitialPosition();
	    }
	
	    public Circle getIntrus() {
	        return intrus;
	    }
	
	    public double getIntrusX() {
	        return intrus.getCenterX();
	    }
	
	    public double getIntrusY() {
	        return intrus.getCenterY();
	    }
	    
	    
	    public boolean estEnClignotement() {
	        return enClignotement;
	    }
	    
	    public double getX() {
	        return this.x;
	    }

	    public double getY() {
	        return this.y;
	    }
	    
	    public void positionner(double x, double y) {
	        this.x = x;
	        this.y = y;
	        }
	
	    private void initIntrus() {
	        this.intrus = new Circle(0, 0, 5, Color.web("#1e90ff"));
	    }
	}
	
	
