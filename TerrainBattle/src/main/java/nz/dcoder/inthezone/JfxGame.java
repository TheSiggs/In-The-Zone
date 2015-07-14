package nz.dcoder.inthezone;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.event.EventHandler;
//import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
//import javafx.scene.image.Image;
//import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
//import javafx.scene.transform.Scale;
//import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class JfxGame extends Application {

	static double viewX = 0;
	static double viewY = 0;
	static double viewDX = 0;
	static double viewDY = 0;

	@Override
	public void start(Stage primaryStage) {
		//board size
		int nx = 10;
		int ny = 10;
		int dimondsPerPage = 6;
		// root
		Group root = new Group();
		Rectangle2D screenRect = Screen.getPrimary().getBounds();
		double screenWidth = screenRect.getWidth();
		double screenHeight = screenRect.getHeight();
		double tileSize = (screenWidth / dimondsPerPage) / Math.sqrt(2.0);
		Scene scene = new Scene(root, screenWidth, screenHeight, Color.BLACK);
		primaryStage.setFullScreen(true);
		primaryStage.setScene(scene);
		// tiles
		Group tiles = new Group();
		for (int y = 0; y < ny; ++y) {
			for (int x = 0; x < nx; ++x) {
				double xCoord = x * tileSize;
				double yCoord = y * tileSize;
				Color color = (x + y) % 2 == 0 ? Color.rgb(0, 0, 0, 0.8) : Color.rgb(255, 255, 255, 0.8);
				Rectangle rect = new Rectangle(xCoord, yCoord, tileSize, tileSize);
				rect.setFill(color);
				tiles.getChildren().add(rect);
			}
		}
		// location
		final double x = 0.5*(screenWidth - (tileSize * nx));
		final double y = 0.5*(screenHeight - (tileSize * nx));

		// transform
		double halfBoardWidth = (tileSize * nx) / 2.0;
		double halfBoardHeight = (tileSize * ny) / 2.0;
		
		Group boardNode = new Group();
		Affine tilesAffine = new Affine();
		boardNode.getChildren().add(tiles); // tiles
		root.getChildren().add(boardNode); // root
		tilesAffine.append(new Translate(x, y + screenHeight/2.0)); // location
		tilesAffine.append(new Rotate(45, halfBoardWidth, halfBoardHeight)); // rotate
		tilesAffine.prependScale(1, 0.5); // perspective squish
		boardNode.getTransforms().add(tilesAffine); // apply transform

		primaryStage.show();
		primaryStage.show();
		
		viewX = -x;
		viewY = -y;
    
		scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
			  //System.out.println(event.getCode());
			  if(event.getCode()==KeyCode.W){accellY(16);}
			  if(event.getCode()==KeyCode.S){accellY(-16);}
			  if(event.getCode()==KeyCode.A){accellX(16);}
			  if(event.getCode()==KeyCode.D){accellX(-16);}
			  
				boardNode.setTranslateX(x+viewX);
		    boardNode.setTranslateY(y+viewY);
			}
		});
	}

  public static void accellX(double delta){
    viewDX = delta;
    viewX+=viewDX;
  }
  public static void accellY(double delta){
    viewDY = delta;
    viewY+=viewDY;
  }


	public static void main(String[] args) {
		launch(args);
	}
	
}