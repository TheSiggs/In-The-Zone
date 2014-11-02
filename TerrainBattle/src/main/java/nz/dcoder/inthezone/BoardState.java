/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.dcoder.inthezone;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author informatics-palmerson
 */
public class BoardState implements Serializable {

	private int board[][];

	public BoardState() {
		this.board = new int[5][5];
		for (int x = 0; x < board.length; ++x) {
			for (int y = 0; y < board[x].length; ++y) {
				board[x][y] = 0;
			}
		}
	}

	public void set(int x, int y, int value) {
		board[x][y] = value;
	}

	public int get(int x, int y) {
		return board[x][y];
	}

	public void randomise() {
		for (int x = 0; x < board.length; ++x) {
			for (int y = 0; y < board[x].length; ++y) {
				board[x][y] = (int) Math.round(Math.random());
			}
		}
	}

	public static void main(String args[]) {
		BoardState boardState = new BoardState();
		boardState.randomise();
		boardState.save();
		BoardState loaded = new BoardState().load();
		String bsWritten = boardState.toString();
		String bsRead = loaded.toString();
		System.out.println(bsRead);
		System.out.println(bsRead.equals(bsWritten) ? "EQUAL" : "DIFF");

		BoardState readState = new BoardState();
		readState.load("board.map");
	}

	public void save() {
		try (FileOutputStream fileOut =
				new FileOutputStream("/tmp/boardState.ser");
				ObjectOutputStream out =
				new ObjectOutputStream(fileOut)) {
			out.writeObject(this);
			System.out.printf("Serialized data is saved in /tmp/boardState.ser");
		} catch (IOException i) {
			throw new RuntimeException(i);
		}
	}

	public BoardState load() {
		BoardState myBoardState = null;
		try (FileInputStream fileIn = new FileInputStream("/tmp/boardState.ser");
         ObjectInputStream in = new ObjectInputStream(fileIn)) {
         myBoardState = (BoardState) in.readObject();
         in.close();
         fileIn.close();
		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		return myBoardState;
	}

	public void load(String mapFile) {
		List<String> lines = null;
		Path path = FileSystems.getDefault().getPath("", mapFile);
		try {
			lines = Files.readAllLines(path, Charset.defaultCharset());
		} catch (IOException ex) {
			Logger.getLogger(BoardState.class.getName()).log(Level.SEVERE, null, ex);
		}
		for (String line : lines) {
			System.out.println(line);
		}
	}

	public String toString() {
		String ret = "";
		for (int x = 0; x < board.length; x++) {
			if (x > 0) {
				ret += "\n";
			}
			for (int y = 0; y < board[x].length; y++) {
				if (y > 0) {
					ret += ",";
				}
				ret += board[x][y];
			}
		}
		return ret;
	}
	
}
