import java.util.*;
import java.awt.Point;

public class PlayerAI extends ClientAI {


	Queue<Node> queue;
	Queue<Move> moveQ;
	private int hCounter;
	private int vCounter;
	private int tileValue[][];
	private Turret[][][] inReachTurret;
	private Bullet[][][] inReachBullet;

	public PlayerAI() {
		queue = new LinkedList<Node>();
		moveQ = new LinkedList<Move>();
		hCounter = 0;
		vCounter = 0;
		
		tileValue = new int[1000][1000];
		inReachTurret = new Turret[1000][1000][4];
		inReachBullet = new Bullet[1000][1000][4];
	}

	private int counter = 0;

	@Override
	public Move getMove(Gameboard gameboard, Opponent opponent, Player player) throws NoItemException, MapOutOfBoundsException {
		// System.out.format("Turn NUm:%d\n", gameboard.getCurrentTurnNumber());
		// Turret t = gameboard.getTurrets().get(2);
		// System.out.format("Turret: cool:%d fire:%d isFiringNextTurn:%b\n",t.getCooldownTime(),t.getFireTime(),t.isFiringNextTurn());
			buildBoardValue(gameboard,player, opponent);
			int maxVal = highestValueTile(gameboard);
			// System.out.format("maxVal:%d\n", maxVal);
			Move myMove = bfs(gameboard, player, opponent, player.getX(), player.getY(), player.getDirection(), maxVal);
			return myMove;
		// }
		
		
		//System.out.println("x:"+player.getX()+" y:"+player.getY()+" safe: "+bulletSafe(gameboard,player.getX(),player.getY()));
		
	}
	//Returns the highest value tile from the gameboard
	private int highestValueTile(Gameboard gameboard) {
		int maxVal = 0;
		for (int i=0;i<gameboard.getWidth();i++) {
			for (int j=0;j<gameboard.getHeight();j++) {
				if (tileValue[i][j]>maxVal) {
					maxVal = tileValue[i][j];
				}
			}
		}
		return maxVal;
	}

	//Build the board with predetermined values for each object in the game
	private void buildBoardValue(Gameboard gameboard, Player player, Opponent opponent) {

		//resets the tilevalue, inReachTurrent and inReachBullet values
		for (int i=0;i<gameboard.getWidth();i++) {
			for (int j=0;j<gameboard.getHeight();j++) {
				tileValue[i][j] = 0;
				for (int k=0;k<4;k++) {
					inReachTurret[i][j][k] = null;
					inReachBullet[i][j][k] = null;
				}
			}
		}

		ArrayList<PowerUp> allPowerUps = gameboard.getPowerUps();

		//set the value for each powertype
		for (int i=0;i<allPowerUps.size();i++) {
			switch(allPowerUps.get(i).getPowerUpType()) {
				case SHIELD:
					tileValue[allPowerUps.get(i).getX()][allPowerUps.get(i).getY()] = 50;
					break;
				case LASER:
					tileValue[allPowerUps.get(i).getX()][allPowerUps.get(i).getY()] = 45;
					break;
				case TELEPORT:
					tileValue[allPowerUps.get(i).getX()][allPowerUps.get(i).getY()] = 40;
					break;
			}
		}

		ArrayList<Turret> allTurrets = gameboard.getTurrets();

		//sets the value according to condition of the bot 
		for (int i=0;i<allTurrets.size();i++) {
			Turret t = allTurrets.get(i);
			tileValue[t.getX()][t.getY()] = -1000;
			if (!t.isDead()) {
				Boolean[] goDir = {true, true, true, true};

				int shieldVal = 0;
				if(player.isShieldActive()) {
					shieldVal = 100;
				} else if (player.getLaserCount() > 0) {
					shieldVal = 60;
				} else if (player.getShieldCount() > 0) {
					shieldVal = 43;
				} else {
					shieldVal = -10;
				}

				for (int j=1;j<=5;j++) {
					try {
						int nx, ny;
						nx = (t.getX()+j)%gameboard.getWidth();
						ny = t.getY();
						if (gameboard.isWallAtTile(nx, ny))
							goDir[0] = false;
						else if (goDir[0]) {
							tileValue[nx][ny] = shieldVal;
							inReachTurret[nx][ny][returnNextIndex(inReachTurret[nx][ny])] = t;
						}
						nx = (t.getX()-j+gameboard.getWidth())%gameboard.getWidth();
						ny = t.getY();
						if (gameboard.isWallAtTile(nx, ny))
							goDir[1] = false;
						else if (goDir[1]) {
							tileValue[nx][ny] = shieldVal;
							inReachTurret[nx][ny][returnNextIndex(inReachTurret[nx][ny])] = t;
						}
						nx = t.getX();
						ny = (t.getY()+j)%gameboard.getHeight();
						if (gameboard.isWallAtTile(nx, ny))
							goDir[2] = false;
						else if (goDir[2]) {
							tileValue[nx][ny] = shieldVal;
							inReachTurret[nx][ny][returnNextIndex(inReachTurret[nx][ny])] = t;
						}
						nx = t.getX();
						ny = (t.getY()-j+gameboard.getHeight())%gameboard.getHeight();
						if (gameboard.isWallAtTile(nx, ny))
							goDir[3] = false;
						else if (goDir[3]) {
							tileValue[nx][ny] = shieldVal;
							inReachTurret[nx][ny][returnNextIndex(inReachTurret[nx][ny])] = t;
						}
					} catch (Exception e) {}
				}
			}
		}

		//Adjust the tile values to incorporate whether the opponent has laser or not
		Boolean[] goDir = {true, true, true, true};
		if (opponent.getLaserCount() >= 1) {
			try {
				for (int j=1;j<=5;j++) {
					int nx, ny;
					nx = (opponent.getX()+j)%gameboard.getWidth();
					ny = opponent.getY();
					if (gameboard.isWallAtTile(nx, ny))
						goDir[0] = false;
					else if (goDir[0]) {
						if (nx==player.getX() && ny==player.getY())
							tileValue[nx][ny] = -4;
						else 
							tileValue[nx][ny] = -1;
					}
					nx = (opponent.getX()-j+gameboard.getWidth())%gameboard.getWidth();
					ny = opponent.getY();
					if (gameboard.isWallAtTile(nx, ny))
						goDir[1] = false;
					else if (goDir[1]) {
						if (nx==player.getX() && ny==player.getY())
							tileValue[nx][ny] = -4;
						else 
							tileValue[nx][ny] = -1;
					}
					nx = opponent.getX();
					ny = (opponent.getY()+j)%gameboard.getHeight();
					if (gameboard.isWallAtTile(nx, ny))
						goDir[2] = false;
					else if (goDir[2]) {
						if (nx==player.getX() && ny==player.getY())
							tileValue[nx][ny] = -4;
						else 
							tileValue[nx][ny] = -1;
					}
					nx = opponent.getX();
					ny = (opponent.getY()-j+gameboard.getHeight())%gameboard.getHeight();
					if (gameboard.isWallAtTile(nx, ny))
						goDir[3] = false;
					else if (goDir[3]) {
						if (nx==player.getX() && ny==player.getY())
							tileValue[nx][ny] = -4;
						else 
							tileValue[nx][ny] = -1;
					}
				}
			} catch (Exception e) {}
		}

		//modifies the gameboard tile vales to incorporate bullets
		for (int i=0;i<gameboard.getBullets().size();i++) {
			Bullet b = gameboard.getBullets().get(i);
			int h = gameboard.getHeight();
			int w = gameboard.getWidth();
			int nx, ny;
			switch(b.getDirection()) {
				case UP:
					nx = b.getX();
					ny = (b.getY()+h-1)%h;
					if (nx==player.getX() && ny==player.getY())
							tileValue[nx][ny] = -4;
					else 
						tileValue[nx][ny] = -2;
					inReachBullet[nx][ny][returnNextIndex(inReachBullet[nx][ny])] = b;
					break;
				case RIGHT:
					nx = (b.getX()+1)%w;
					ny = b.getY();
					if (nx==player.getX() && ny==player.getY())
							tileValue[nx][ny] = -4;
					else 
						tileValue[nx][ny] = -2;
					inReachBullet[nx][ny][returnNextIndex(inReachBullet[nx][ny])] = b;
					break;
				case DOWN:
					nx = b.getX();
					ny = (b.getY()+1)%h;
					if (nx==player.getX() && ny==player.getY())
							tileValue[nx][ny] = -4;
					else 
						tileValue[nx][ny] = -2;
					inReachBullet[nx][ny][returnNextIndex(inReachBullet[nx][ny])] = b;
					break;
				case LEFT:
					nx = (b.getX()+w-1)%w;
					ny = b.getY();
					if (nx==player.getX() && ny==player.getY())
							tileValue[nx][ny] = -4;
					else 
						tileValue[nx][ny] = -2;
					inReachBullet[nx][ny][returnNextIndex(inReachBullet[nx][ny])] = b;
					break;
			}
		}

		ArrayList<Wall> allWalls = gameboard.getWalls();

		for (int i=0;i<allWalls.size();i++) {
			tileValue[allWalls.get(i).getX()][allWalls.get(i).getY()] = -1000;
		}

	}

	//returns the availiable next index of an array
	private int returnNextIndex(Object[] temp) {
		for (int i=0;i<4;i++) {
			if (temp[i]==null) {
				return i;
			}
		}
		return -1;
	}

	
	//augments bfs algorithm to navigate the board and decides the move
	private Move bfs (Gameboard gameboard, Player player,Opponent opponent, int x, int y, Direction dir, int maxVal) {
		Node record[][][] = new Node[gameboard.getWidth()][gameboard.getHeight()][4];
		Node nextMove = null;
		Node lastMove = null;
		
		// System.out.println(dir);

		int d=0;
		switch(dir) {
			case UP:
				d= 0;
				break;
			case DOWN:
				d= 2;
				break;
			case LEFT:
				d= 3;
				break;
			case RIGHT:
				d= 1;
				break;
		}

		queue.clear();
		queue.add(new Node(x,y,null,0, d));

		while(!queue.isEmpty()) {
			Node curNode = queue.remove();

			// System.out.println("CurNode: " + curNode.toString());

			try {
				if (gameboard.isWallAtTile(curNode.x, curNode.y))
					continue;
				else if (gameboard.isTurretAtTile(curNode.x, curNode.y))
					continue;
				else if (opponent.getX()==curNode.x && opponent.getY()==curNode.y)
					continue;
			} catch(Exception e) {

			}

			if (tileValue[curNode.x][curNode.y] == maxVal) {
				// System.out.println("Target: " + curNode.toString());
				nextMove = getPath(curNode);
				lastMove = curNode;
				break;
			} else if (tileValue[curNode.x][curNode.y] > 40 && curNode.steps <= 5) {
				// System.out.println("Target: " + curNode.toString());
				nextMove = getPath(curNode);
				lastMove = curNode;
				break;
			}

			Node newNode = null;
			int curDir = -1;
			switch(curNode.d) {
				case UP:
					// System.out.println("Facing UP!");
					newNode = new Node(curNode.x,(curNode.y-1+gameboard.getHeight())%gameboard.getHeight(),curNode,curNode.steps+1,0);
					// System.out.println("Declared this node: " + newNode.toString());
					if (record[newNode.x][newNode.y][newNode.getDirInt()] == null) {
						queue.add(newNode);
						record[newNode.x][newNode.y][newNode.getDirInt()] = newNode;
						// System.out.println("Pushed: " + newNode.toString());
					}
					curDir = 0;
					break;
				case RIGHT:
					// System.out.println("Facing Right!");
					newNode = new Node((curNode.x+1+gameboard.getWidth())%gameboard.getWidth(),curNode.y,curNode,curNode.steps+1,1);
					// System.out.println("Declared this node: " + newNode.toString());
					if (record[newNode.x][newNode.y][newNode.getDirInt()] == null) {
						queue.add(newNode);
						record[newNode.x][newNode.y][newNode.getDirInt()] = newNode;
						// System.out.println("Pushed: " + newNode.toString());
					}
					curDir = 1;
					break;
				case DOWN:
					newNode = new Node(curNode.x,(curNode.y+1+gameboard.getHeight())%gameboard.getHeight(),curNode,curNode.steps+1,2);
					if (record[newNode.x][newNode.y][newNode.getDirInt()] == null) {
						queue.add(newNode);
						record[newNode.x][newNode.y][newNode.getDirInt()] = newNode;
					}
					curDir = 2;
					break;
				case LEFT:
					newNode = new Node((curNode.x-1+gameboard.getWidth())%gameboard.getWidth(),curNode.y,curNode,curNode.steps+1,3);
					if (record[newNode.x][newNode.y][newNode.getDirInt()] == null) {
						queue.add(newNode);
						record[newNode.x][newNode.y][newNode.getDirInt()] = newNode;
					}
					curDir = 3;
					break;
			}



			for (int i=1;i<=3;i++){
				int fuDir = (curDir+i)%4;
				switch(fuDir) {
					case 0:
						newNode = new Node(curNode.x,(curNode.y-1+gameboard.getHeight())%gameboard.getHeight(),curNode,curNode.steps+2,0);
						if (record[newNode.x][newNode.y][newNode.getDirInt()] == null) {
							queue.add(newNode);
							record[newNode.x][newNode.y][newNode.getDirInt()] = newNode;
							// System.out.println("Pushed: " + newNode.toString());
						}
						break;
					case 1:
						newNode = new Node((curNode.x+1+gameboard.getWidth())%gameboard.getWidth(),curNode.y,curNode,curNode.steps+2,1);
						if (record[newNode.x][newNode.y][newNode.getDirInt()] == null) {
							queue.add(newNode);
							record[newNode.x][newNode.y][newNode.getDirInt()] = newNode;
							// System.out.println("Pushed: " + newNode.toString());
						}
						break;
					case 2:
						newNode = new Node(curNode.x,(curNode.y+1+gameboard.getHeight())%gameboard.getHeight(),curNode,curNode.steps+2,2);
						if (record[newNode.x][newNode.y][newNode.getDirInt()] == null) {
							queue.add(newNode);
							record[newNode.x][newNode.y][newNode.getDirInt()] = newNode;
							// System.out.println("Pushed: " + newNode.toString());
						}
						break;
					case 3:
						newNode = new Node((curNode.x-1+gameboard.getWidth())%gameboard.getWidth(),curNode.y,curNode,curNode.steps+2,3);
						if (record[newNode.x][newNode.y][newNode.getDirInt()] == null) {
							queue.add(newNode);
							record[newNode.x][newNode.y][newNode.getDirInt()] = newNode;
							// System.out.println("Pushed: " + newNode.toString());
						}
						break;
				}
			}
		}

		// System.out.println("Nextmove: " + nextMove.toString());
		if (nextMove.steps == 1 && tileValue[nextMove.x][nextMove.y] == -1) {
			if (player.getShieldCount() >= 1) {
				return Move.SHIELD;
			} else {
				return Move.NONE;
			}
		} else if (nextMove.steps == 1 && tileValue[nextMove.x][nextMove.y] == -2) {
			for (int i=0;i<4 && inReachBullet[nextMove.x][nextMove.y][i] != null;i++) {
				Bullet b = inReachBullet[nextMove.x][nextMove.y][i];
				if (player.getDirection() == Direction.opposite(b.getDirection())) {
					return Direction.directionToMovement(Direction.clockwise(player.getDirection()));
				} else {
					return Move.NONE;
				}
			}
		} else if (lastMove.steps <= 1) {
			Turret t = inReachTurret[lastMove.x][lastMove.y][0];
			if (tileValue[lastMove.x][lastMove.y] == 60 && t.getCooldownTime() >= 2) {
				// System.out.format("Turret x:%d y:%d cool:%d fire:%d\n", t.getX(), t.getY(), t.getCooldownTime(), t.getFireTime());
				int cycle = t.getCooldownTime() + t.getFireTime();
				int fuStatus1 = gameboard.getCurrentTurnNumber()%cycle + 1;
				int fuStatus2 = gameboard.getCurrentTurnNumber()%cycle + 2;
				if (lastMove.steps == 1) {
					if (fuStatus1 > t.getFireTime() && fuStatus1 <= cycle
						&& fuStatus2 > t.getFireTime() && fuStatus2 <= cycle) {
						return Move.FORWARD;
					} else {
						return Move.NONE;
					}
				} else if (lastMove.steps == 0) {
					return Move.LASER;
				}
			} else if (tileValue[lastMove.x][lastMove.y]==43 || (t!=null && t.getCooldownTime() <= 1 && player.getShieldCount() >= 1)) {
				// Turret t = inReachTurret[lastMove.x][lastMove.y][0];
				// System.out.format("Turret x:%d y:%d cool:%d fire:%d\n", t.getX(), t.getY(), t.getCooldownTime(), t.getFireTime());
				int cycle = t.getCooldownTime() + t.getFireTime();
				int fuStatus1 = gameboard.getCurrentTurnNumber()%cycle+1;
				int fuStatus2 = gameboard.getCurrentTurnNumber()%cycle+2;
				if (lastMove.steps == 1 && !player.isShieldActive()) {
					if (!t.isFiringNextTurn()) {
						return Move.FORWARD;
					} else if (fuStatus2 <= t.getFireTime()) {
						return Move.SHIELD;
					} else if (fuStatus2 > t.getFireTime() && fuStatus2 <= t.getCooldownTime()) {
						return Move.NONE;
					}
				}  else if (player.isShieldActive()) {
					return Move.FORWARD;
				} else if (lastMove.steps == 0) {
					if (t.isFiringNextTurn()) {
						return Move.SHIELD;
					} else {
						return Move.NONE;
					}
				}
			} else if (tileValue[lastMove.x][lastMove.y] > 40 && tileValue[lastMove.x][lastMove.y] <= 50){
				return Move.FORWARD;
			} else if (tileValue[lastMove.x][lastMove.y]==100) {
				return Move.FORWARD;
			} else if (tileValue[player.getX()][player.getY()] < 0) {
				return Move.FORWARD;
			}
		}  else {
			if (nextMove.steps == 2) {
				return Direction.directionToMovement(nextMove.d);
			} else {
				Turret t = inReachTurret[nextMove.x][nextMove.y][0];
				if (t==null || !t.isFiringNextTurn()) {
					// System.out.println("1");
					return Move.FORWARD;
				} else
					return Move.NONE;
			}
		}

		return Move.NONE;
		
		// for(int x=0;x<gameboard.getWidth();x++) {
		// 	for(int y=0;y<gameboard.getHeight();y++) {
		// 		record[x][y][] = new Node(0,1, null,-1, -1)
		// 	}
		// }
		
	}

	//gets path of the next move
	private Node getPath(Node child) {
		if (child.pNode == null)
			return child;
		if (child.pNode.pNode != null)
			return getPath(child.pNode);
		else 
			return child;
	}
	//calculate the distance of the input
	private int distance(int xx, int yy, int length) {
		int x=xx;
		int y=yy;
		if(xx>yy) {
			x=yy;
			y=xx;
		}
		if(x<=y) {
			if(Math.abs(y-x) < Math.abs(x+length-y)) {
				return Math.abs(y-x);
			} else {
				return Math.abs(x+length-y);
			}
		}
		return -1;
	}
	//node for the tree
	public class Node {
		public int x;
		public int y;
		public Node pNode;
		public int steps;
		public Direction d;
	
		public  Node(int x, int y, Node pNode, int steps, int d) {
			this.x=x;
			this.y=y;
			this.pNode=pNode;
			switch(d) {
				case 0:
					this.d = Direction.UP;
					break;
				case 1:
					this.d = Direction.RIGHT;
					break;
				case 2:
					this.d = Direction.DOWN;
					break;
				case 3:
					this.d = Direction.LEFT;
					break;
			}
			this.steps = steps;
		}

		public String toString() {
			return String.format("x:%d y:%d steps:%d d:%d", this.x, this.y, this.steps, this.getDirInt());
		}

		public int getDirInt() {
			switch(this.d) {
				case UP:
					return 0;
				case DOWN:
					return 2;
				case LEFT:
					return 3;
				case RIGHT:
					return 1;
			}
			return -1;
		}
	}
}
