import java.awt.AWTException;
import java.util.Scanner;
import java.util.ArrayList;

class Move {
	int linInit, colInit;
	int linFin, colFin;
	
	public Move () {
		
	}
	
	public Move (Move m) {
		this.linInit = m.linInit;
		this.colInit = m.colInit;
		this.linFin = m.linFin;
		this.colFin = m.colFin;
	}
	
	public String toString() {
		return "(" + this.linInit + "," + this.colInit + ")" + " --> " + "(" + this.linFin + "," + this.colFin + ")";
	}
}

class Pair <X, Y> {
	X first;
	Y second;
	
	public Pair (X first, Y second) {
		this.first = first;
		this.second = second;
	}
}

public class Board {
	int[][] board;
	char side;
	
	boolean[][] castling;
	
	int[][] en_passant;
	
	final static int MAX = 500;
    public static int INF = 123456789;          ////////////////////////////////////////////////
	
	public Board() {
		this.board = new int[8][8];
		this.en_passant = new int[8][8];
		this.castling = new boolean[2][2];
	}
	
	public void set() {
		this.board[0][0] = this.board[0][7] = -1;
		this.board[0][1] = this.board[0][6] = -2;
		this.board[0][2] = this.board[0][5] = -3;
		this.board[0][3] = -4;
		this.board[0][4] = -5;

		for (int i = 0; i < 8; i++) {
			this.board[1][i] = -6;
			this.board[6][i] = 6;
		}
		
		for(int i = 2; i < 6; i++) {
			for(int j = 0; j < 8; j++) {
				this.board[i][j] = 0;
			}
		}
		
		this.board[7][0] = this.board[7][7] = 1;
		this.board[7][1] = this.board[7][6] = 2;
		this.board[7][2] = this.board[7][5] = 3;
		this.board[7][3] = 4;
		this.board[7][4] = 5;	
	}
	
	public void white() {
		this.side = 'w';
	}
	
	public void black() {
		this.side = 'b';
	}

	
	public void printBoard () {
		for (int i = 0; i < 8; i++) {
			for(int j = 0; j < 8; j++) {
				if(this.board[i][j] >= 0)
					System.out.print("+" + this.board[i][j] + " ");
				else 
					System.out.print(this.board[i][j] + " ");
			}
			System.out.println();
		}
	}
	
	public void printBoardEnPassant () {
		for (int i = 0; i < 8; i++) {
			for(int j = 0; j < 8; j++) {
				if(this.en_passant[i][j] >= 0)
					System.out.print("+" + this.en_passant[i][j] + " ");
				else 
					System.out.print(this.en_passant[i][j] + " ");
			}
			System.out.println();
		}
	}
	
	// verifica daca regele poate face rocada cu unul dintre turnuri
	public boolean castlingOk_King () {
		// rege alb
		if (this.side == 'w') {
			return this.castling[0][0] || this.castling[0][1];
		}
		return this.castling[1][0] || this.castling[1][1];
	}
	
	public int validCommand (String command) {
		if(command.charAt(0) >= 'a' && command.charAt(0) <= 'h' && 
				command.charAt(1) >= '1' && command.charAt(1) <= '8' && 
					command.charAt(2) >= 'a' && command.charAt(2) <= 'h' && 
						command.charAt(3) >= '1' && command.charAt(3) <= '8') {
			
			int [] positions = convertToPositions(command);
			int i_init = positions[0], j_init = positions[1], i_fin = positions[2], j_fin = positions[3];
			
			if(this.board[i_fin][j_fin] * this.board[i_init][j_init] <= 0)
				return 1;
		}	
			
		return 0;
	}
	
	public int[] convertToPositions (String move){
		int i_init = 0, j_init = 0, i_fin = 0, j_fin = 0;
		i_init = move.charAt(1) - '0';
		i_fin = move.charAt(3) - '0';
		
		j_init = move.charAt(0) - 'a';
		j_fin = move.charAt(2) - 'a';
		int[] positions = new int[4];
	
		positions[0] = 8 - i_init;
		positions[1] = j_init;
		positions[2] = 8 - i_fin;
		positions[3] = j_fin;
		
		return positions;	
	}
	
	public void moveBoard (String move) {
		int [] positions = convertToPositions(move);
		int piece, i_init = positions[0], j_init = positions[1], i_fin = positions[2], j_fin = positions[3];
					
		// promotie pion alb
		if (this.board[i_init][j_init] == 6 && i_fin == 0) {
			this.board[i_init][j_init] = 0;
			this.board[i_fin][j_fin] = 4;
			return;
		}
		
		// promotie pion negru
		if (this.board[i_init][j_init] == -6 && i_fin == 7) {
			this.board[i_init][j_init] = 0;
			this.board[i_fin][j_fin] = -4;
			return;
		}
		
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				// daca era posibilitate de en_passant si nu s-a folosit, o sterg
				if (this.en_passant[i][j] == 1 && i_fin != i && j_fin != j) {
					this.en_passant[i][j] = 0;
				}
				else {
					// en_passant, capturez pionul
					if (this.en_passant[i][j] == 1 && i_fin == i && j_fin == j) {
						// pion alb
						if (this.board[i_init][j_init] == 6 && i_init == 3) {
							if (j_init - 1 >= 0 && this.board[i_init][j_init - 1] == -6) {
								// capturez pion negru
								this.board[i_init][j_init - 1] = 0;
								continue;
							}
							if (j_init + 1 < 8 && this.board[i_init][j_init + 1] == -6) {
								// capturez pion negru
								this.board[i_init][j_init + 1] = 0;
								continue;
							}					
						}
						else {
							// pion negru
							if (this.board[i_init][j_init] == -6 && i_init == 4) {
								if (j_init - 1 >= 0 && this.board[i_init][j_init - 1] == 6) {
									// capturez pion alb
									this.board[i_init][j_init - 1] = 0;
									continue;
								}
								if (j_init + 1 < 8 && this.board[i_init][j_init + 1] == 6) {
									// capturez pion alb
									this.board[i_init][j_init + 1] = 0;
									continue;
								}
							}
						}
					}
				}
			}
		}
		
		// verific en_passant
		if (this.board[i_init][j_init] == 6 && i_fin - i_init == -2) {
			this.en_passant[i_init - 1][j_init] = 1;
		}
		
		else {
			if (this.board[i_init][j_init] == -6 && i_fin - i_fin == 2) {
				this.en_passant[i_init+ 1][j_init] = 1;
			}
		}
		
		
		// rocada alb
		if (this.board[i_init][j_init] ==  5 && i_init == i_fin && i_fin == 7) {
			if (j_fin - j_init == 2) {
				this.board[i_init][j_init] = 0;
				this.board[i_init][j_fin] = 5;
				this.board[i_init][7] = 0;
				this.board[i_init][j_init + 1] = 1;
				return;
			} else if (j_init - j_fin == 2) {
				this.board[i_init][j_init] = 0;
				this.board[i_init][j_fin] = 5;
				this.board[i_init][0] = 0;
				this.board[i_init][j_init - 1] = 1;
				return;
			}	
		}
		
		// rocada negru
		if (this.board[i_init][j_init] ==  -5 && i_init == i_fin && i_fin == 0) {
			if (j_fin - j_init == 2) {
				this.board[i_init][j_init] = 0;
				this.board[i_init][j_fin] = -5;
				this.board[i_init][7] = 0;
				this.board[i_init][j_init + 1] = -1;
				return;
			} else if (j_init - j_fin == 2) {
				this.board[i_init][j_init] = 0;
				this.board[i_init][j_fin] = -5;
				this.board[i_init][0] = 0;
				this.board[i_init][j_init - 1] = -1;
				return;
			}	
		}
		
		piece = this.board[i_init][j_init];
		this.board[i_init][j_init] = 0;
		this.board[i_fin][j_fin] = piece;
	}
	
	public int eval (int sign, Board b) {
		Pair <Boolean, Integer> endGame = gameOver (sign);
		if (endGame.first == true) {
			if (endGame.second == -sign)
				return -INF;
			else {
				return INF;
			}
		}
		
		
		int score = 0;
		for(int i = 0; i < 8; i++) {
			for(int j = 0; j < 8; j++) {
				if (b.board[i][j] * sign > 0) {
					//pion
					if (Math.abs(b.board[i][j]) == 6) {
						score += 10;
					} 
					//cal
					if (Math.abs(b.board[i][j]) == 2) {
						score += 30;
					}
					//nebun
					if (Math.abs(b.board[i][j]) == 3) {
						score += 30;
					}
					//tura
					if (Math.abs(b.board[i][j]) == 1) {
						score += 50;
					}
					//regina
					if (Math.abs(b.board[i][j]) == 4) {
						score += 100;
					}
				}
				else {
					//pion
					if (Math.abs(b.board[i][j]) == 6) {
						score -= 10;
					} 
					//cal
					if (Math.abs(b.board[i][j]) == 2) {
						score -= 30;
					}
					//nebun	
					if (Math.abs(b.board[i][j]) == 3) {
						score -= 30;
					}
					//tura
					if (Math.abs(b.board[i][j]) == 1) {
						score -= 50;
					}
					//regina
					if (Math.abs(b.board[i][j]) == 4) {
						score -= 100;
					}
				}
			}
		}
		
		ArrayList<Move> move_sign = generateMoves(sign, b.board);
		ArrayList<Move> move_not_sign = generateMoves(-sign, b.board);
		int mobility_score = move_sign.size() - move_not_sign.size();

		return score + mobility_score;
	}
	
	// returneaza pozitia regelui pe tabla
	public int[] findKing (int[][] board, int sign) {
		int[] ret = new int[2];
		ret[0] = ret[1] = -1;
		
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if (board[i][j] * sign == 5) {
					ret[0] = i;
					ret[1] = j;
					break;
				}
			}
			if (ret[0] != -1 && ret[1] != -1) {
				break;
			}
		}
		return ret;
	}
	
	public ArrayList<Move> generateMoves(int sign, int[][] board) {
		ArrayList<Move> moves = new ArrayList<Move>();
		Move m;
		
		//copie a tablei
		Board cop;
		
		boolean check;
		
		int[] kingPos;
		
		// gasesc fiecare piesa pe tabla si ii generez mutarile posibile 
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if (board[i][j] * sign > 0) {
					// pion
					if (Math.abs(board[i][j]) == 6) {
						//pion nemutat negru, verific daca pot muta peste 2 casute
						if (sign == -1 && i == 1) {
							if (board[i + 2][j] == 0 && board[i + 1][j] == 0) {
								cop = clone();
								m = new Move();
								m.linInit = i;
								m.colInit = j;
								m.linFin = i + 2;
								m.colFin = j;
								applyMove(m, cop);
								
								kingPos = findKing(cop.board, sign);
								
								check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
								
								if (check) {
									moves.add(m);
								}
							}
						}
						//pion nemutat alb
						else {
							if (sign == 1 && i == 6) {
								if (board[i - 2][j] == 0 && board[i - 1][j] == 0) {
									cop = clone();
									m = new Move();
									m.linInit = i;
									m.colInit = j;
									m.linFin = i - 2;
									m.colFin = j;
									applyMove(m, cop);
									
									kingPos = findKing(cop.board, sign);
									
									check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
									
									if (check) {
										moves.add(m);
									}
								}
							}
						}
						if (sign == -1) {
							if (i + 1 < 8) {
								if (board[i + 1][j] == 0) {
									cop = clone();
									m = new Move();
									m.linInit = i;
									m.colInit = j;
									m.linFin = i + 1;
									m.colFin = j;
									applyMove(m, cop);
									
									kingPos = findKing(cop.board, sign);
									
									check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
									
									if (check) {
										moves.add(m);
									}
								}
							}
							if (i + 1 < 8 && j - 1 >= 0) {
								if(board[i + 1][j - 1] * sign < 0) {
									cop = clone();
									m = new Move();
									m.linInit = i;
									m.colInit = j;
									m.linFin = i + 1;
									m.colFin = j - 1;
									applyMove(m, cop);
									
									kingPos = findKing(cop.board, sign);
									
									check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
									
									if (check) {
										moves.add(m);
									}
								}
							}
							if(i + 1 < 8 && j + 1 < 8) {
								if(board[i + 1][j + 1] * sign < 0) {
									cop = clone();
									m = new Move();
									m.linInit = i;
									m.colInit = j;
									m.linFin = i + 1;
									m.colFin = j + 1;
									applyMove(m, cop);
									
									kingPos = findKing(cop.board, sign);
									
									check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
									
									if (check) {
										moves.add(m);
									}
								}
							}
						}
						else {
							if (sign == 1) {
								if ( i - 1 >= 0) {
									if ( board[i - 1][j] == 0) {
										cop = clone();
										m = new Move();
										m.linInit = i;
										m.colInit = j;
										m.linFin = i - 1;
										m.colFin = j;
										applyMove(m, cop);
										
										kingPos = findKing(cop.board, sign);
										
										check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
										
										if (check) {
											moves.add(m);
										}
									}
								}
								if( i - 1 >= 0 && j - 1 >= 0) {
									if(board[i - 1][j - 1] * sign < 0) {
										cop = clone();
										m = new Move();
										m.linInit = i;
										m.colInit = j;
										m.linFin = i - 1;
										m.colFin = j - 1;
										applyMove(m, cop);
										
										kingPos = findKing(cop.board, sign);
										
										check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
										
										if (check) {
											moves.add(m);
										}
									}
								}
								if( i - 1 >= 0 && j + 1 < 8) {
									if(board[i - 1][j + 1] * sign < 0) {
										cop = clone();
										m = new Move();
										m.linInit = i;
										m.colInit = j;
										m.linFin = i - 1;
										m.colFin = j + 1;
										applyMove(m, cop);
										
										kingPos = findKing(cop.board, sign);
										
										check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
										
										if (check) {
											moves.add(m);
										}
									}
								}
							}
						}	
					}
					
					//tura
					if (Math.abs(board[i][j]) == 1) {
						
						//mutare tura in stanga
						for (int t = j - 1; t >= 0 ; t--) {
							if (board[i][t] * sign > 0)     
								break; 
							
							if (board[i][t] * sign < 0) {   
								cop = clone();
								m = new Move();
								m.linInit = i;
								m.colInit = j;
								m.linFin = i;
								m.colFin = t;
								applyMove(m, cop);
								
								kingPos = findKing(cop.board, sign);
								
								check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
								
								if (check) {
									moves.add(m);
								}
								break; 
							}
							
							m = new Move();
							cop = clone();
							m = new Move();
							m.linInit = i;
							m.colInit = j;
							m.linFin = i;
							m.colFin = t;
							applyMove(m, cop);
							
							kingPos = findKing(cop.board, sign);
							
							check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
							
							if (check) {
								moves.add(m);
							}
						}
						
						//mutare tura in jos
						for (int t = i + 1; t < 8 ; t++) {
							if (board[t][j] * sign > 0)     
								break; 
							
							if (board[t][j] * sign < 0) {   
								cop = clone();
								m = new Move();
								m.linInit = i;
								m.colInit = j;
								m.linFin = t;
								m.colFin = j;
								applyMove(m, cop);
								
								kingPos = findKing(cop.board, sign);
								
								check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
								
								if (check) {
									moves.add(m);
								}
								break; 
							}
							
							cop = clone();
							m = new Move();
							m.linInit = i;
							m.colInit = j;
							m.linFin = t;
							m.colFin = j;
							applyMove(m, cop);
							
							kingPos = findKing(cop.board, sign);
							
							check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
							
							if (check) {
								moves.add(m);
							}
						}

						//mutare tura in dreapta
						for (int t = j + 1; t < 8 ; t++) {
							if (board[i][t] * sign > 0)     
								break; 
							
							if (board[i][t] * sign < 0) {   
								cop = clone();
								m = new Move();
								m.linInit = i;
								m.colInit = j;
								m.linFin = i;
								m.colFin = t;
								applyMove(m, cop);
								
								kingPos = findKing(cop.board, sign);
								
								check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
								
								if (check) {
									moves.add(m);
								}
								break; 
							}
							
							cop = clone();
							m = new Move();
							m.linInit = i;
							m.colInit = j;
							m.linFin = i;
							m.colFin = t;
							applyMove(m, cop);
							
							kingPos = findKing(cop.board, sign);
							
							check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
							
							if (check) {
								moves.add(m);
							}
						}
						
						//mutare tura in sus
						for (int t = i - 1; t >= 0 ; t--) {
							if (board[t][j] * sign > 0)     
								break; 
							
							if (board[t][j] * sign < 0) {   
								cop = clone();
								m = new Move();
								m.linInit = i;
								m.colInit = j;
								m.linFin = t;
								m.colFin = j;
								applyMove(m, cop);
								
								kingPos = findKing(cop.board, sign);
								
								check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
								
								if (check) {
									moves.add(m);
								}
								break; 
							}
							
							cop = clone();
							m = new Move();
							m.linInit = i;
							m.colInit = j;
							m.linFin = t;
							m.colFin = j;
							applyMove(m, cop);
							
							kingPos = findKing(cop.board, sign);
							
							check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
							
							if (check) {
								moves.add(m);
							}
						}
					}

					// cal
					if (Math.abs(board[i][j]) == 2) {
						
						//cal mutat stanga foarte sus
						if (((i - 2 >= 0) && (j - 1 >= 0)) && (board[i - 2][j - 1] * sign <= 0)) {
							cop = clone();
							m = new Move();
							m.linInit = i;
							m.colInit = j;
							m.linFin = i - 2;
							m.colFin = j - 1;
							applyMove(m, cop);
							
							kingPos = findKing(cop.board, sign);
							
							check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
							
							if (check) {
								moves.add(m);
							}
						}

						//cal mutat stanga sus
						if (((i - 1 >= 0) && (j - 2 >= 0)) && (board[i - 1][j - 2] * sign <= 0)) {
							cop = clone();
							m = new Move();
							m.linInit = i;
							m.colInit = j;
							m.linFin = i - 1;
							m.colFin = j - 2;
							applyMove(m, cop);
							
							kingPos = findKing(cop.board, sign);
							
							check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
							
							if (check) {
								moves.add(m);
							}
						}
						
						//cal mutat stanga jos
						if (((i + 1 < 8) && (j - 2 >= 0)) && (board[i + 1][j - 2] * sign <= 0)) {
							cop = clone();
							m = new Move();
							m.linInit = i;
							m.colInit = j;
							m.linFin = i + 1;
							m.colFin = j - 2;
							applyMove(m, cop);
							
							kingPos = findKing(cop.board, sign);
							
							check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
							
							if (check) {
								moves.add(m);
							}
						}
						
						//cal mutat stanga foarte jos
						if (((i + 2 < 8) && (j - 1 >= 0)) && (board[i + 2][j - 1] * sign <= 0)) {
							cop = clone();
							m = new Move();
							m.linInit = i;
							m.colInit = j;
							m.linFin = i + 2;
							m.colFin = j - 1;
							applyMove(m, cop);
							
							kingPos = findKing(cop.board, sign);
							
							check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
							
							if (check) {
								moves.add(m);
							}
						}
						
						//cal mutat dreapta foarte jos
						if (((i + 2 < 8) && (j + 1 < 8)) && (board[i + 2][j + 1] * sign <= 0)) {
							cop = clone();
							m = new Move();
							m.linInit = i;
							m.colInit = j;
							m.linFin = i + 2;
							m.colFin = j + 1;
							applyMove(m, cop);
							
							kingPos = findKing(cop.board, sign);
							
							check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
							
							if (check) {
								moves.add(m);
							}
						}
						
						//cal mutat dreapta jos
						if (((i + 1 < 8) && (j + 2 < 8)) && (board[i + 1][j + 2] * sign <= 0)) {
							cop = clone();
							m = new Move();
							m.linInit = i;
							m.colInit = j;
							m.linFin = i + 1;
							m.colFin = j + 2;
							applyMove(m, cop);
							
							kingPos = findKing(cop.board, sign);
							
							check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
							
							if (check) {
								moves.add(m);
							}
						}
						
						//cal mutat dreapta sus
						if (((i - 1 >= 0) && (j + 2 < 8)) && (board[i - 1][j + 2] * sign <= 0)) {
							cop = clone();
							m = new Move();
							m.linInit = i;
							m.colInit = j;
							m.linFin = i - 1;
							m.colFin = j + 2;
							applyMove(m, cop);
							
							kingPos = findKing(cop.board, sign);
							
							check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
							
							if (check) {
								moves.add(m);
							}
						}
						
						//cal mutat dreapta foarte sus
						if (((i - 2 >= 0) && (j + 1 < 8)) && (board[i - 2][j + 1] * sign <= 0)) {
							cop = clone();
							m = new Move();
							m.linInit = i;
							m.colInit = j;
							m.linFin = i - 2;
							m.colFin = j + 1;
							applyMove(m, cop);
							
							kingPos = findKing(cop.board, sign);
							
							check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
							
							if (check) {
								moves.add(m);
							}
						}
					}
			
					//regina
					if (Math.abs(board[i][j]) == 4) {
						//mutare regina in stanga
						for (int t = j - 1; t >= 0 ; t--) {
							if (board[i][t] * sign > 0)     
								break; 
							
							if (board[i][t] * sign < 0) {   
								cop = clone();
								m = new Move();
								m.linInit = i;
								m.colInit = j;
								m.linFin = i;
								m.colFin = t;
								applyMove(m, cop);
								
								kingPos = findKing(cop.board, sign);
								
								check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
								
								if (check) {
									moves.add(m);
								}
								break; 
							}
							
							cop = clone();
							m = new Move();
							m.linInit = i;
							m.colInit = j;
							m.linFin = i;
							m.colFin = t;
							applyMove(m, cop);
							
							kingPos = findKing(cop.board, sign);
							
							check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
							
							if (check) {
								moves.add(m);
							}
						}
						
						//mutare regina in jos
						for (int t = i + 1; t < 8 ; t++) {
							if (board[t][j] * sign > 0)     
								break; 
							
							if (board[t][j] * sign < 0) {   
								cop = clone();
								m = new Move();
								m.linInit = i;
								m.colInit = j;
								m.linFin = t;
								m.colFin = j;
								applyMove(m, cop);
								
								kingPos = findKing(cop.board, sign);
								
								check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
								
								if (check) {
									moves.add(m);
								}
								break; 
							}
							
							cop = clone();
							m = new Move();
							m.linInit = i;
							m.colInit = j;
							m.linFin = t;
							m.colFin = j;
							applyMove(m, cop);
							
							kingPos = findKing(cop.board, sign);
							
							check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
							
							if (check) {
								moves.add(m);
							}
						}

						//mutare regina in dreapta
						for (int t = j + 1; t < 8 ; t++) {
							if (board[i][t] * sign > 0)     
								break; 
							
							if (board[i][t] * sign < 0) {   
								cop = clone();
								m = new Move();
								m.linInit = i;
								m.colInit = j;
								m.linFin = i;
								m.colFin = t;
								applyMove(m, cop);
								
								kingPos = findKing(cop.board, sign);
								
								check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
								
								if (check) {
									moves.add(m);
								}
								break;
							}
							
							cop = clone();
							m = new Move();
							m.linInit = i;
							m.colInit = j;
							m.linFin = i;
							m.colFin = t;
							applyMove(m, cop);
							
							kingPos = findKing(cop.board, sign);
							
							check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
							
							if (check) {
								moves.add(m);
							}
						}
						
						//mutare regina in sus
						for (int t = i - 1; t >= 0 ; t--) {
							if (board[t][j] * sign > 0)     
								break; 
							
							if (board[t][j] * sign < 0) {   
								cop = clone();
								m = new Move();
								m.linInit = i;
								m.colInit = j;
								m.linFin = t;
								m.colFin = j;
								applyMove(m, cop);
								
								kingPos = findKing(cop.board, sign);
								
								check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
								
								if (check) {
									moves.add(m);
								}
								break; 
							}
							
							cop = clone();
							m = new Move();
							m.linInit = i;
							m.colInit = j;
							m.linFin = t;
							m.colFin = j;
							applyMove(m, cop);
							
							kingPos = findKing(cop.board, sign);
							
							check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
							
							if (check) {
								moves.add(m);
							}
						}
						
						//mutare regina diagonala secundara jos
						for(int k = 1; k < 8; k++) {
		
							if (i + k < 8 && j - k >= 0) {
								if(board[i + k][j - k] * sign > 0) 
									break;
								
								if(board[i + k][j - k] * sign < 0) {
									cop = clone();
									m = new Move();
									m.linInit = i;
									m.colInit = j;
									m.linFin = i + k;
									m.colFin = j - k;
									applyMove(m, cop);
									
									kingPos = findKing(cop.board, sign);
									
									check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
									
									if (check) {
										moves.add(m);
									}
									break;
								}
								cop = clone();
								m = new Move();
								m.linInit = i;
								m.colInit = j;
								m.linFin = i + k;
								m.colFin = j - k;
								applyMove(m, cop);
								
								kingPos = findKing(cop.board, sign);
								
								check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
								
								if (check) {
									moves.add(m);
								}
							}
						}
						
						//mutare regina diagonala secundara sus
						for(int k = 1; k < 8; k++) {
							
							if(i - k >= 0 && j + k < 8) {
								if(board[i - k][j + k] * sign > 0)
									break;
								
								if(board[i - k][j + k] * sign < 0) {
									cop = clone();
									m = new Move();
									m.linInit = i;
									m.colInit = j;
									m.linFin = i - k;
									m.colFin = j + k;
									applyMove(m, cop);
									
									kingPos = findKing(cop.board, sign);
									
									check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
									
									if (check) {
										moves.add(m);
									}
									break;
								}
								
								cop = clone();
								m = new Move();
								m.linInit = i;
								m.colInit = j;
								m.linFin = i - k;
								m.colFin = j + k;
								applyMove(m, cop);
								
								kingPos = findKing(cop.board, sign);
								
								check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
								
								if (check) {
									moves.add(m);
								}
							}
						}
						
						//mutare regina diag principala sus
						for(int k = 1; k < 8; k++) {
							
							if(i - k >= 0 && j - k >= 0) {
								if(board[i - k][j - k] * sign > 0)
									break;
								
								if(board[i - k][j - k] * sign < 0) {
									cop = clone();
									m = new Move();
									m.linInit = i;
									m.colInit = j;
									m.linFin = i - k;
									m.colFin = j - k;
									applyMove(m, cop);
									
									kingPos = findKing(cop.board, sign);
									
									check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
									
									if (check) {
										moves.add(m);
									}
									break;
								}
								
								cop = clone();
								m = new Move();
								m.linInit = i;
								m.colInit = j;
								m.linFin = i - k;
								m.colFin = j - k;
								applyMove(m, cop);
								
								kingPos = findKing(cop.board, sign);
								
								check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
								
								if (check) {
									moves.add(m);
								}
							}
						}
						
						//mutare regina diag principala jos
						for(int k = 1; k < 8; k++) {
						
							if( i + k < 8 && j + k < 8) {
								if(board[i + k][j + k] * sign > 0)
									break;
								
								if(board[i + k][j + k] * sign < 0) {
									cop = clone();
									m = new Move();
									m.linInit = i;
									m.colInit = j;
									m.linFin = i + k;
									m.colFin = j + k;
									applyMove(m, cop);
									
									kingPos = findKing(cop.board, sign);
									
									check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
									
									if (check) {
										moves.add(m);
									}
									break;
								}
								
								cop = clone();
								m = new Move();
								m.linInit = i;
								m.colInit = j;
								m.linFin = i + k;
								m.colFin = j + k;
								applyMove(m, cop);
								
								kingPos = findKing(cop.board, sign);
								
								check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
								
								if (check) {
									moves.add(m);
								}
							}
						}		
					}
		
					
					//nebunul
					if(Math.abs(board[i][j]) == 3) {
						//nebun mutat pe diagonala secundara jos
						for(int k = 1; k < 8; k++) {
		
							if (i + k < 8 && j - k >= 0) {
								if(board[i + k][j - k] * sign > 0) 
									break;
								if(board[i + k][j - k] * sign < 0) {
									cop = clone();
									m = new Move();
									m.linInit = i;
									m.colInit = j;
									m.linFin = i + k;
									m.colFin = j - k;
									applyMove(m, cop);
									
									kingPos = findKing(cop.board, sign);
									
									check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
									
									if (check) {
										moves.add(m);
									}
									break;
								}
								
								cop = clone();
								m = new Move();
								m.linInit = i;
								m.colInit = j;
								m.linFin = i + k;
								m.colFin = j - k;
								applyMove(m, cop);
								
								kingPos = findKing(cop.board, sign);
								
								check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
								
								if (check) {
									moves.add(m);
								}
							}
						}
						//nebun mutat pe diagonala secundara sus
						for(int k = 1; k < 8; k++) {
				
							if(i - k >= 0 && j + k < 8) {
								if(board[i - k][j + k] * sign > 0)
									break;
								
								if(board[i - k][j + k] * sign < 0) {
									cop = clone();
									m = new Move();
									m.linInit = i;
									m.colInit = j;
									m.linFin = i - k;
									m.colFin = j + k;
									applyMove(m, cop);
									
									kingPos = findKing(cop.board, sign);
									
									check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
									
									if (check) {
										moves.add(m);
									}
									break;
								}
								
								cop = clone();
								m = new Move();
								m.linInit = i;
								m.colInit = j;
								m.linFin = i - k;
								m.colFin = j + k;
								applyMove(m, cop);
								
								kingPos = findKing(cop.board, sign);
								
								check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
								
								if (check) {
									moves.add(m);
								}
							}
						}
						
						//nebun mutat pe diag principala sus
						for(int k = 1; k < 8; k++) {
							
							if(i - k >= 0 && j - k >= 0) {
								if(board[i - k][j - k] * sign > 0)
									break;
								
								if(board[i - k][j - k] * sign < 0) {
									cop = clone();
									m = new Move();
									m.linInit = i;
									m.colInit = j;
									m.linFin = i - k;
									m.colFin = j - k;
									applyMove(m, cop);
									
									kingPos = findKing(cop.board, sign);
									
									check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
									
									if (check) {
										moves.add(m);
									}
									break;
								}
								
								cop = clone();
								m = new Move();
								m.linInit = i;
								m.colInit = j;
								m.linFin = i - k;
								m.colFin = j - k;
								applyMove(m, cop);
								
								kingPos = findKing(cop.board, sign);
								
								check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
								
								if (check) {
									moves.add(m);
								}
							}
						}
						
						//nebun mutat pe diag principala jos
						for(int k = 1; k < 8; k++) {
							
							if( i + k < 8 && j + k < 8) {
								if(board[i + k][j + k] * sign > 0) 
									break;
								
								if(board[i + k][j + k] * sign < 0) {
									cop = clone();
									m = new Move();
									m.linInit = i;
									m.colInit = j;
									m.linFin = i + k;
									m.colFin = j + k;
									applyMove(m, cop);
									
									kingPos = findKing(cop.board, sign);
									
									check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
									
									if (check) {
										moves.add(m);
									}
									break;
								}
								
								cop = clone();
								m = new Move();
								m.linInit = i;
								m.colInit = j;
								m.linFin = i + k;
								m.colFin = j + k;
								applyMove(m, cop);
								
								kingPos = findKing(cop.board, sign);
								
								check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
								
								if (check) {
									moves.add(m);
								}
							}
						}
					}
					
					//regele
					if(Math.abs(board[i][j]) == 5) {
						
						if (i - 1 >= 0 && j - 1 >= 0 && board[i-1][j-1] * sign <= 0) {
							cop = clone();
							m = new Move();
							m.linInit = i;
							m.colInit = j;
							m.linFin = i - 1;
							m.colFin = j - 1;
							applyMove(m, cop);
							
							kingPos = findKing(cop.board, sign);
							
							check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
							
							if (check) {
								moves.add(m);
							}
						}
						if (j - 1 >= 0 && board[i][j-1] * sign <= 0) {
							cop = clone();
							m = new Move();
							m.linInit = i;
							m.colInit = j;
							m.linFin = i;
							m.colFin = j - 1;
							applyMove(m, cop);
							
							kingPos = findKing(cop.board, sign);
							
							check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
							
							if (check) {
								moves.add(m);
							}
						}
						if (i + 1 < 8 && j - 1 >= 0 && board[i+1][j-1] * sign <= 0) {
							cop = clone();
							m = new Move();
							m.linInit = i;
							m.colInit = j;
							m.linFin = i + 1;
							m.colFin = j - 1;
							applyMove(m, cop);
							
							kingPos = findKing(cop.board, sign);
							
							check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
							
							if (check) {
								moves.add(m);
							}
						}
						if (i + 1 < 8 && board[i+1][j] * sign <= 0) {
							cop = clone();
							m = new Move();
							m.linInit = i;
							m.colInit = j;
							m.linFin = i + 1;
							m.colFin = j;
							applyMove(m, cop);
							
							kingPos = findKing(cop.board, sign);
							
							check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
							
							if (check) {
								moves.add(m);
							}
						}
						if (i + 1 < 8 && j + 1 < 8 && board[i+1][j+1] * sign <= 0) {
							cop = clone();
							m = new Move();
							m.linInit = i;
							m.colInit = j;
							m.linFin = i + 1;
							m.colFin = j + 1;
							applyMove(m, cop);
							
							kingPos = findKing(cop.board, sign);
							
							check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
							
							if (check) {
								moves.add(m);
							}
						}
						if (j + 1 < 8 && board[i][j+1] * sign <= 0) {
							cop = clone();
							m = new Move();
							m.linInit = i;
							m.colInit = j;
							m.linFin = i;
							m.colFin = j + 1;
							applyMove(m, cop);
							
							kingPos = findKing(cop.board, sign);
							
							check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
							
							if (check) {
								moves.add(m);
							}
						}
						if (i - 1 >= 0 && j + 1 < 8 && board[i-1][j+1] * sign <= 0) {
							cop = clone();
							m = new Move();
							m.linInit = i;
							m.colInit = j;
							m.linFin = i - 1;
							m.colFin = j + 1;
							applyMove(m, cop);
							
							kingPos = findKing(cop.board, sign);
							
							check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
							
							if (check) {
								moves.add(m);
							}
						}
						if (i - 1 >= 0 && board[i-1][j] * sign <= 0) {
							cop = clone();
							m = new Move();
							m.linInit = i;
							m.colInit = j;
							m.linFin = i - 1;
							m.colFin = j;
							applyMove(m, cop);
							
							kingPos = findKing(cop.board, sign);
							
							check = check_valid_position_king (kingPos[0], kingPos[1], sign, cop.board);
							
							if (check) {
								moves.add(m);
							}
						}
					}
				}
			}
		}
		
		return moves;
	}
	
	public boolean check_valid_position_king (int i, int j, int sign, int[][] board) {
		
		//verifica tura sus
		for (int t = 1; t < 8; t++) {
			if (i - t >= 0) {
				if ((board[i - t][j] == (-1) * sign) || (board[i - t][j] == (-1) * sign * 4)) {
					return false;
				}
				if (board[i - t][j] != 0)
					break;
			}
			else break;
		}
		
		//verifica tura jos
		for (int t = 1; t < 8; t++) {
			if (i + t < 8) {
				if ((board[i + t][j] == (-1) * sign) || (board[i + t][j] == (-1) * sign * 4)) {
					return false;
				}
					
				if (board[i + t][j] != 0)
					break;
			}
			else break;
		}
		
		//verifica tura stanga
		for (int t = 1; t < 8; t++) {
			if (j - t >= 0) {
				if ((board[i][j - t] == (-1) * sign) || (board[i][j - t] == (-1) * sign * 4)) {
					return false;
				}
				if (board[i][j - t] != 0)
					break;
			}
			else break;
		}
		
		//verifica tura dreapta
		for (int t = 1; t < 8; t++) {
			if (j + t < 8) {
				if ((board[i][j + t] == (-1) * sign) || (board[i][j + t] == (-1) * sign * 4)) {
					return false;
				}
					
				if (board[i][j + t] != 0)
					break;
			}
			else break;
		}
		
		//verifica cal stanga foarte sus
		if ((i - 2 >= 0) && (j - 1 >= 0) && (board[i - 2][j - 1] == (-1) * sign * 2)) {
			return false;
		}
		
		//verifica cal stanga sus
		if ((i - 1 >= 0) && (j - 2 >= 0) && (board[i - 1][j - 2] == (-1) * sign * 2)) {
			return false;
		}
		
		//verifica cal stanga jos
		if ((i + 1 < 8) && (j - 2 >= 0) && (board[i + 1][j - 2] == (-1) * sign * 2)) {
			return false;
		}
				
		//verifica cal stanga foarte sus
		if ((i + 2 < 8) && (j - 1 >= 0) && (board[i + 2][j - 1] == (-1) * sign * 2)) {
			return false;
		}
		
		//verifica cal dreapta foarte jos
		if ((i + 2 < 8) && (j + 1 < 8) && (board[i + 2][j + 1] == (-1) * sign * 2)) {
			return false;
		}
		
		//verifica cal dreapta jos
		if ((i + 1 < 8) && (j + 2 < 8) && (board[i + 1][j + 2] == (-1) * sign * 2)) {
			return false;
		}
		
		//verifica cal dreapta sus
		if ((i - 1 >= 0) && (j + 2 < 8) && (board[i - 1][j + 2] == (-1) * sign * 2)) {
			return false;
		}
				
		//verifica cal dreapta foarte sus
		if ((i - 2 >= 0) && (j + 1 < 8) && (board[i - 2][j + 1] == (-1) * sign * 2)) {
			return false;
		}
		
		//verifica nebun diagonala principala sus
		for (int t = 1; t < 8; t++) {
			if ((i - t >= 0) && (j - t >= 0)) {
				if ((board[i - t][j - t] == (-1) * sign * 3) || (board[i - t][j - t] == (-1) * sign * 4)) {
					return false;
				}
				if (board[i - t][j - t] != 0)
					break;
			}
			else break;
		}
		
		//verifica nebun diagonala secundara jos
		for (int t = 1; t < 8; t++) {
			if ((i + t < 8) && (j - t >= 0)) {
				if ((board[i + t][j - t] == (-1) * sign * 3) || (board[i + t][j - t] == (-1) * sign * 4)) {
					return false;
				}
				if (board[i + t][j - t] != 0)
					break;
			}
			else break;
		}
		
		//verifica nebun diagonala principala jos
		for (int t = 1; t < 8; t++) {
			if ((i + t < 8) && (j + t < 8)) {
				if ((board[i + t][j + t] == (-1) * sign * 3) || (board[i + t][j + t] == (-1) * sign * 4)) {
					return false;
				}
				if (board[i + t][j + t] != 0)
					break;
			}
			else break;
		}
		
		//verifica nebun diagonala secundara sus
		for (int t = 1; t < 8; t++) {
			if ((i - t >= 0) && (j + t < 8)) {
				if ((board[i - t][j + t] == (-1) * sign * 3) || (board[i - t][j + t] == (-1) * sign * 4)) {
					return false;
				}
				if (board[i - t][j + t] != 0)
					break;
			}
			else break;
		}
		
		//verifica pion negru stanga sus
		if ((i - 1 >= 0) && (j - 1 >= 0) && (board[i-1][j-1] == -6) && (sign == 1)) {
			return false;
		}
		
		//verifica pion negru dreapta sus
		if ((i - 1 >= 0) && (j + 1 < 8) && (board[i-1][j+1] == -6) && (sign == 1)) {
			return false;
		}
		
		//verifica pion alb stanga jos
		if ((i + 1 < 8) && (j - 1 >= 0) && (board[i+1][j-1] == 6) && (sign == -1)) {
			return false;
		}
		
		//verifica pion alb dreapta jos
		if ((i + 1 < 8) && (j + 1 < 8) && (board[i+1][j+1] == 6) && (sign == -1)) {
			return false;
		}
		
		//verifica regele stanga sus
		if((i - 1 >= 0) && (j - 1 >= 0) && (board[i - 1][j - 1] == (-1) * sign * 5)) {
			return false;
		}

		//verifica regele stanga
		if((j - 1 >= 0) && (board[i][j - 1] == (-1) * sign * 5)) {
			return false;
		}
		
		//verifica regele stanga jos
		if((i + 1 < 8) && (j - 1 >= 0) && (board[i + 1][j - 1] == (-1) * sign * 5)) {
			return false;
		}
		
		//verifica regele jos
		if((i + 1 < 8) && (board[i + 1][j] == (-1) * sign * 5)) {
			return false;
		}
		
		//verifica regele dreapta jos
		if((i + 1 < 8) && (j + 1 < 8) && (board[i + 1][j + 1] == (-1) * sign * 5)) {
			return false;
		}
		
		//verifica regele dreapta
		if((j + 1 < 8) && (board[i][j + 1] == (-1) * sign * 5)) {
			return false;
		}
		
		//verifica regele dreapta sus
		if((i - 1 >= 0) && (j + 1 < 8) && (board[i - 1][j + 1] == (-1) * sign * 5)) {
			return false;
		}
		
		//verifica regele sus
		if((i - 1 >= 0) &&(board[i - 1][j] == (-1) * sign * 5)) {
			return false;
		}
		return true;
	}
	
	public void applyMove(Move move, Board b) {
		if (move == null) {
			return;
		}
		
		// verific promotie pion alb
		if (b.board[move.linInit][move.colInit] == 6 && move.linFin == 0 ) {
			// devina regina
			b.board[move.linFin][move.colFin] = 4;
			b.board[move.linInit][move.colInit] = 0;
			return;
		}
		
		// verific promotie pion alb
		if (b.board[move.linInit][move.colInit] == -6 && move.linFin == 7 ) {
			// devina regina
			b.board[move.linFin][move.colFin] = -4;
			b.board[move.linInit][move.colInit] = 0;
			return;
		}
		
		// daca era posibilitate de en_passant si nu s-a folosit, o sterg
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if (b.en_passant[i][j] == 1 && move.linFin != i && move.colFin != j) {
					b.en_passant[i][j] = 0;
				}
			
			else {
				// en_passant, capturez pionul
				if (b.en_passant[i][j] == 1 && move.linFin == i && move.colFin == j) {
					// pion alb
					if (b.board[move.linInit][move.colInit] == 6 && move.linInit == 3) {
						if (move.colInit - 1 >= 0 && b.board[move.linInit][move.colInit - 1] == -6) {
							// capturez pion negru
							b.board[move.linInit][move.colInit - 1] = 0;
							continue;
						}
						if (move.colInit + 1 < 8 && b.board[move.linInit][move.colInit + 1] == -6) {
							// capturez pion negru
							b.board[move.linInit][move.colInit + 1] = 0;
							continue;
						}					
					}
					else {
						// pion negru
						if (b.board[move.linInit][move.colInit] == -6 && move.linInit == 4) {
							if (move.colInit - 1 >= 0 && b.board[move.linInit][move.colInit - 1] == 6) {
								// capturez pion alb
								b.board[move.linInit][move.colInit - 1] = 0;
								continue;
							}
							if (move.colInit + 1 < 8 && b.board[move.linInit][move.colInit + 1] == 6) {
								// capturez pion alb
								b.board[move.linInit][move.colInit + 1] = 0;
								continue;
							}
						}
					}
				}
			}
			}
		}
		
		// verific en_passant
		if (b.board[move.linInit][move.colInit] == 6 && move.linFin - move.linInit == -2) {
			b.en_passant[move.linInit - 1][move.colInit] = 1;
		}
		
		else {
			if (b.board[move.linInit][move.colInit] == -6 && move.linFin - move.linInit == 2) {
				b.en_passant[move.linInit + 1][move.colInit] = 1;
			}
		}
		
		b.board[move.linFin][move.colFin] = this.board[move.linInit][move.colInit];
		b.board[move.linInit][move.colInit] = 0;
	}
	
	public int[][] cloneBoard() {
		int[][]cop = new int[8][8];
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				cop[i][j] = this.board[i][j];
			}
		}
		return cop;
	}
	
	public Board clone() {
		Board cop = new Board();
		cop.board = this.cloneBoard();
		cop.side = this.side;
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				cop.castling[i][j] = this.castling[i][j];
			}
		}
		
		return cop;
	}
	
	public Pair<Boolean, Integer> gameOver (int sign) {

		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if (board[i][j] * sign == 5) {
					
					//daca regele meu e in sah, verific daca exista mutari care sa il salveze
					if (check_valid_position_king (i, j, sign, this.board) == false) {
						ArrayList<Move> moves = generateMoves (sign, this.board);
						
						// daca nu exista mutari, am pierdut
						if (moves.size() == 0) {
							//System.out.println ("Regele meu in sah, am pierdut");
							return new Pair<Boolean, Integer> (true, -sign);
						}
					}
				}
				
				// regele adversar
				if (board[i][j] * sign == -5) {
					if (check_valid_position_king (i, j, -sign, this.board) == false) {
						ArrayList<Move> moves = generateMoves (-sign, this.board);
						
						// daca nu exista mutari ale adversarului, am castigat
						if (moves.size() == 0) {
							//System.out.println ("Regele adversar in sah, am castigat");
							return new Pair<Boolean, Integer> (true, sign);
						}
					}
				}
			}
		}
		return new Pair<Boolean, Integer> (false, 0);
	}
	
	public Pair <Integer, Move> negamax (Board b, int depth, int sign) {
		if (b.gameOver(sign).first || depth == 0) {
			return new Pair <Integer, Move> (b.eval(sign, b), null);
		}
		
		Move bestMove = null;
		int scoreMax = -INF; 
		
		int score;
		Pair<Integer, Move> rez;
		Board cop;

		ArrayList<Move> moves = b.generateMoves(sign, b.board);
		
		for (Move m : moves) {
			cop = b.clone ();
			cop.applyMove(m, cop);
			System.out.println(m + " " + depth);
			rez = cop.negamax(cop, depth - 1, -sign);
			score = -rez.first;
			if (score >= scoreMax) {
				scoreMax = score;
				System.out.println(score + " " + depth);
				if(depth == 3) {
					bestMove = new Move(m);
					System.out.println(bestMove + "aaaaaaaaaaaaaaaaaaaaaaaa");
				}

			}
		}
		
		return new Pair <Integer, Move> (scoreMax, bestMove);
	}
			
	public static void main(String[] args) throws AWTException, InterruptedException {
			
		Board b = new Board();
		b.set();
		b.black();
		
		Scanner input = new Scanner(System.in);
		String com;
		
		int sign = b.side == 'b'? -1 : 1;
			
		while(true) {
			com = input.nextLine();
				
			if(com.contains("protover")) {
				System.out.println("feature sigint=0");
				}
			if(com.contains("new")) {
				b.set();
				b.black();
				sign = -1;
			}
			if(com.contains("white")) {
				b.white();
				sign = 1;
			}
			if(com.contains("black")) {
				b.black();
				sign = -1;
			}
			if(com.contains("force")) {
				com = input.nextLine();
				
				while(true) {
					if(com.contains("black")) {
						b.black();
						com = input.nextLine();
						break;
					}
					if(com.contains("white")) {
						b.white();
						com = input.nextLine();
						break;
					}
					if(com.charAt(0) >= 'a' && com.charAt(0) <= 'h' && com.charAt(1) >= '1' && com.charAt(1) <= '8')
						b.moveBoard(com);
					
					com = input.nextLine();
				}
			}
			if(com.contains("go")) {
				
				Pair <Integer, Move> move = b.negamax(b, 3, sign);
				if (move.second == null) {
					System.out.println ("resign");
					continue;
				}
				
				b.applyMove(move.second, b);
				
				int[] rez = new int[4];
				
				rez[0] = 8 - move.second.linInit;		// linInit
				rez[1] = 'a' + move.second.colInit;		// colInit
				
				rez[2] = 8 - move.second.linFin;		// linFin
				rez[3] = 'a' + move.second.colFin;		// colFin
				
				System.out.println("move " + (char) rez[1] + rez[0] + (char) rez[3] + rez[2]);
				continue;
			}
			
			if(com.charAt(0) >= 'a' && com.charAt(0) <= 'h' && com.charAt(1) >= '1' && com.charAt(1) <= '8') {
				b.moveBoard(com);
				b.printBoard();
				
				Pair <Integer, Move> move = b.negamax(b, 3, sign);
				if (move.second == null) {
					System.out.println ("resign");
					continue;
				}
				
				b.applyMove(move.second, b);
				
				int[] rez = new int[4];
				
				rez[0] = 8 - move.second.linInit;		// linInit
				rez[1] = 'a' + move.second.colInit;		// colInit
				
				rez[2] = 8 - move.second.linFin;		// linFin
				rez[3] = 'a' + move.second.colFin;		// colFin
				
				
				System.out.println("move " + (char) rez[1] + rez[0] + (char) rez[3] + rez[2]);
			}
			
			if(com.contains("quit")) {
				System.exit(0);
			}
		}
	}
}