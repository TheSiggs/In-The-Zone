package nz.dcoder.inthezone;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class JfxGame extends Application {

	@Override
	public void start(Stage primaryStage) {
		int nx = 10;
		int ny = 10;
		Group root = new Group();
		Rectangle2D screenRect = Screen.getPrimary().getBounds();
		double screenWidth = screenRect.getWidth();
		double screenHeight = screenRect.getHeight();
		double tileSize = (screenWidth / nx) / Math.sqrt(2.0);
		Scene scene = new Scene(root, screenWidth, screenHeight, Color.BLACK);
		primaryStage.setFullScreen(true);
		
		primaryStage.setScene(scene);
		//double tileSize = 64.0;

		Group tiles = new Group();
		Color blackColor = Color.rgb(0, 0, 0, 0.8);
		Color whiteColor = Color.rgb(255, 255, 255, 0.8);
		for (int y = 0; y < ny; ++y) {
			for (int x = 0; x < nx; ++x) {
				double xCoord = x * tileSize;
				double yCoord = y * tileSize;
				Color color = (x + y) % 2 == 0 ? blackColor : whiteColor;
				Rectangle rect = new Rectangle(xCoord, yCoord, tileSize, tileSize);
				rect.setFill(color);
				tiles.getChildren().add(rect);
			}
		}
		Image grassTile = new Image("file:///home/denz/NetBeansProjects/ColorfulCircles/grass_texture.jpg");
		ImageView grassTileImageView = new ImageView(grassTile);
		grassTileImageView.setFitWidth(tileSize*nx);
		grassTileImageView.setFitHeight(tileSize*ny);
		grassTileImageView.setOpacity(0.7);
		tiles.getChildren().add(grassTileImageView);

		double middle = (tileSize * nx) / 2.0;
		double middle2 = (tileSize * nx * Math.sqrt(2.0)) / 2.0;
		double middleWidth = screenWidth / 2.0;
		double middleHeight = screenHeight / 2.0;
		double x = middleWidth - middle;
		double y = middleHeight - middle;
		double x2 = middleWidth - middle2;
		Image image = new Image("file:///home/denz/NetBeansProjects/ColorfulCircles/zan_fit.png");
		ImageView imageView = new ImageView(image);
		double imageWidth = image.getWidth();
		double imageHeight = image.getHeight();
		double halfBoardWidth = (tileSize * nx) / 2.0;
		double halfBoardHeight = (tileSize * ny) / 2.0;
		double scale = (tileSize * 3.0) / (imageHeight * 2.0);//tileSize * 2.0 * (imageHeight/screenHeight);
		Group boardNode = new Group();
		boardNode.getChildren().add(tiles);
		//tiles.getTransforms().add(new Translate(x, y));
		//tiles.getTransforms().add(new Rotate(45.0, tileSize * (nx/2), tileSize * (ny/2)));
		//boardNode.getTransforms().add(new Scale(1.0, 0.5, middleWidth, middleHeight));
		////boardNode.getTransforms().add(new Translate(x, y))
		root.getChildren().add(boardNode);
		Affine tilesAffine = new Affine();
		double halfTileSize = tileSize/2;
		double myWidth = imageWidth * scale;
		double myHeight = imageHeight * scale;
		tilesAffine.append(new Translate(x, y + screenHeight/2.0));
		tilesAffine.append(new Rotate(45, halfBoardWidth, halfBoardHeight));
		tilesAffine.prependScale(1, 0.5);
		//tilesAffine.append(new Translate(0, screenHeight/4));
		double squashedHalfBoardWidth = (tileSize * Math.sqrt(2.0))*5; //halfBoardWidth * Math.sqrt(2);
		double squashedHalfBoardHeight = halfBoardHeight * Math.sqrt(2);
		//tilesAffine.append(new Translate(x2, y));
		//tilesAffine.appendScale(1.0, 0.5, halfBoardWidth, halfBoardHeight);
		//tilesAffine.append(squash);
		//tilesAffine.append(new Translate(middleWidth, 0.0));
		//tilesAffine.append(new Rotate(45.0, 0.0, 0.0));
		//tilesAffine.append(new Scale(1, 0.5));
		//tilesAffine.append(new Translate(x, y));
		//tilesAffine.append(new Translate(-myWidth/2.0, -myHeight));
		//tilesAffine.append(new Rotate(45.0, tileSize * (nx/2), tileSize * (ny/2)));
		//tilesAffine.append(new Scale(1.0, 0.5, middleWidth, middleHeight));
		boardNode.getTransforms().add(tilesAffine);

		//double scale = 0.125; //tileSize * 2.0 * (imageHeight/screenHeight);

		//System.out.println("scale = "+scale2);
		imageView.getTransforms().add(new Scale(scale, scale));
		imageView.setSmooth(true);
		//imageView.setTranslateX(0);
		//imageView.setTranslateY(-200);
		Group character = new Group();
		character.getChildren().add(imageView);
		/*
		character.getTransforms().add(new Translate(x, -y));
		imageView.getTransforms().add(new Rotate(-45.0));
		character.getTransforms().add(new Rotate(45.0, 0, 0));
		*/
		System.out.println("xy = "+ x +","+ y);

		Affine trans = new Affine();
		//trans.append(new Translate(-myWidth/2.0, -myHeight));
		//trans.append(new Translate(middleWidth, 0.0));
		//trans.append(new Rotate(45.0, 0.0, 0.0));
		//trans.append(new Scale(1, 0.5));

		//trans.append(new Translate(x, y));
		//trans.append(new Translate(-myWidth/2.0, -myHeight));
		//trans.append(new Rotate(45.0, tileSize * (nx/2), tileSize * (ny/2)));
		//trans.append(new Scale(1.0, 0.5, middleWidth, middleHeight));

		for (Transform t : tiles.getTransforms()) {
			trans.append(t);
		}
		for (Transform t : boardNode.getTransforms()) {
			trans.append(t);
		}
		System.out.println("trans = "+ trans);

		//Point2D offset = trans.transform(4 * halfTileSize, 0);
		Point2D offset = trans.transform(tileSize + myWidth/2, myHeight);
		character.getTransforms().add(new Translate(offset.getX(), offset.getY()));
		root.getChildren().add(character);

		/*
		Group circles = new Group();
		circles.setEffect(new BoxBlur(10, 10, 3));
		for (int i = 0; i < 30; i++) {
			Circle circle = new Circle(150, Color.web("white", 0.05));
			circle.setStrokeType(StrokeType.OUTSIDE);
			circle.setStroke(Color.web("white", 0.16));
			circle.setStrokeWidth(4);
			circles.getChildren().add(circle);
		}
		Rectangle colors = new Rectangle(scene.getWidth(), scene.getHeight(),
				new LinearGradient(0f, 1f, 1f, 0f, true, CycleMethod.NO_CYCLE, new Stop[]{
					new Stop(0, Color.web("#f8bd55")),
					new Stop(0.14, Color.web("#c0fe56")),
					new Stop(0.28, Color.web("#5dfbc1")),
					new Stop(0.43, Color.web("#64c2f8")),
					new Stop(0.57, Color.web("#be4af7")),
					new Stop(0.71, Color.web("#ed5fc2")),
					new Stop(0.85, Color.web("#ef504c")),
					new Stop(1, Color.web("#f2660f")),}));
		colors.widthProperty().bind(scene.widthProperty());
		colors.heightProperty().bind(scene.heightProperty());
		Group blendModeGroup
				= new Group(new Group(new Rectangle(scene.getWidth(), scene.getHeight(),
										Color.BLACK), circles), colors);
		colors.setBlendMode(BlendMode.OVERLAY);
		root.getChildren().add(blendModeGroup);
		Timeline timeline = new Timeline();
		for (Node circle : circles.getChildren()) {
			timeline.getKeyFrames().addAll(
					new KeyFrame(Duration.ZERO, // set start position at 0
							new KeyValue(circle.translateXProperty(), random() * 800),
							new KeyValue(circle.translateYProperty(), random() * 600)
					),
					new KeyFrame(new Duration(40000), // set end position at 40s
							new KeyValue(circle.translateXProperty(), random() * 800),
							new KeyValue(circle.translateYProperty(), random() * 600)
					)
			);
		}
// play 40s of animation
		timeline.play();
				*/
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}