package puyo;

import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

public class PuyoFor1P extends JFrame {
	public PuyoFor1P() {
		setTitle("ぷよAIのとこぷよ");
		PuyoGame pg = new PuyoGame();
		Container cp = getContentPane();
		cp.add(pg);
		pg.setSize(pg.size * pg.width, pg.size * pg.height);
		pg.setBackground(Color.WHITE);
		pack();
	}

	public static void main(String[] args) {
		PuyoFor1P py = new PuyoFor1P();
		py.setDefaultCloseOperation(EXIT_ON_CLOSE);
		py.setVisible(true);
	}
}

class PuyoGame extends JPanel {
	int size = 40; // 画面サイズ
	int height = 14; // 段目
	int width = 8; // 行目
	int py, px, rotate; // 現在手情報
	int chainnum = 0; // 連鎖数
	int screen_chain = 0; // 画面表示用連鎖数
	int score = 0; // スコア
	int moves = 0; // 手数
	int p[][] = new int[height][width]; // メイン配列
	int con[][] = new int[height][width]; // 連結数配列
	ArrayList<Integer> tsumolist = new ArrayList<Integer>(); // ツモリスト

	PuyoGame() {
		setPreferredSize(new Dimension(width * size, height * size));
		setFocusable(true);
		addKeyListener(new Key_e());
		Filltsumolist();
		Select();
		// ゲームの流れ追加予定
	}

	// ツモリスト確定（ツモ補正あり）
	void Filltsumolist() {
		boolean hosei = true; // ツモ補正用

		for (int i1 = 1; i1 <= 4; i1++) {
			for (int i2 = 0; i2 < 64; i2++)
				tsumolist.add(i1);
		}

		// ツモ補正
		while (hosei) {
			hosei = false;
			Collections.shuffle(tsumolist);

			for (int i = 0; i < 6; i++) {
				if (tsumolist.get(i) == 4) {
					hosei = true;
					break;
				}
			}
		}
	}

	// 連鎖確認
	void Rensa() {
		int colornum = 0; // 色数（スコア用）
		int connectnum = 0; // 連結数（スコア用）
		int deletenum = 0; // 消去数（スコア用）
		int scoresub = 0; // スコア計算用
		int color[] = new int[4]; // 色数判定用
		boolean isDelete = true; // 連鎖確認（内部）
		boolean chaincheck = false; // 連鎖確認（外部伝達用）

		// 色数判定初期化
		for (int i = 0; i < 4; i++)
			color[i] = 1;

		// 連鎖確認（内部）
		Drop();
		while (isDelete) {
			isDelete = false;
			int connect = 0; // 連結

			// 全体確認
			for (int i = 2; i < height; i++) {
				for (int j = 0; j < width - 2; j++) {
					// con配列初期化
					for (int i2 = 2; i2 < height; i2++) {
						for (int j2 = 0; j2 < width - 2; j2++)
							con[i2][j2] = 0;
					}

					// 連結数確認
					if (p[i][j] >= 1 && p[i][j] <= 4) {
						con[i][j] = 1;
						connect = Search(i, j, 1);

						// 連鎖実行
						if (connect >= 4) {
							isDelete = true;
							// 色ボーナス
							colornum = colornum + color[p[i][j] - 1];
							color[p[i][j] - 1] = 0;
							// スコア用保存
							connectnum = connect;
							deletenum = deletenum + connect;
							connect = 0;

							Delete();
						}
					}
				}
			}
			// 連鎖確認（外部伝達用）
			if (isDelete)
				chaincheck = true;
		}
		Drop();

		// スコア修正
		if (chaincheck) {
			chainnum++;
			scoresub = Score_chain(chainnum) + Score_color(colornum)
					+ Score_connect(connectnum);
			if (scoresub == 0)
				scoresub = 1;

			score = score + deletenum * 10 * scoresub;
			screen_chain = chainnum;
		} else {
			Select();
			chainnum = 0;
		}
		repaint();
	}

	// 連結数をreturn
	int Search(int k1, int k2, int connect2) {
		// 上方向確認
		if (k1 > 2 && p[k1 - 1][k2] == p[k1][k2] && con[k1 - 1][k2] == 0) {
			con[k1 - 1][k2] = 1;
			connect2 = Search(k1 - 1, k2, connect2 + 1);
		}
		// 下方向確認
		if (k1 < height - 1 && p[k1 + 1][k2] == p[k1][k2]
				&& con[k1 + 1][k2] == 0) {
			con[k1 + 1][k2] = 1;
			connect2 = Search(k1 + 1, k2, connect2 + 1);
		}
		// 左方向確認
		if (k2 > 0 && p[k1][k2 - 1] == p[k1][k2] && con[k1][k2 - 1] == 0) {
			con[k1][k2 - 1] = 1;
			connect2 = Search(k1, k2 - 1, connect2 + 1);
		}
		// 右方向確認
		if (k2 < width - 3 && p[k1][k2 + 1] == p[k1][k2]
				&& con[k1][k2 + 1] == 0) {
			con[k1][k2 + 1] = 1;
			connect2 = Search(k1, k2 + 1, connect2 + 1);
		}
		return connect2;
	}

	// ツモ消去
	void Delete() {
		for (int i1 = 0; i1 < height; i1++) {
			for (int i2 = 0; i2 < width - 2; i2++) {
				if (con[i1][i2] > 0) {
					p[i1][i2] = 0;
				}
			}
		}
	}

	// ツモをセット
	void Select() {
		py = 2;
		px = 2;
		rotate = 0;
		moves++;

		// ツモリセット
		if (moves == 127) {
			moves = -1;
			Collections.shuffle(tsumolist);
		}

		// ツモを入れ替え
		if (p[2][2] == 0) {
			// 初期設定
			if (p[1][width - 1] == 0) {
				p[1][2] = tsumolist.get(0);
				p[2][2] = tsumolist.get(1);
				p[0][width - 1] = tsumolist.get(2);
				p[1][width - 1] = tsumolist.get(3);
				p[3][width - 1] = tsumolist.get(4);
				p[4][width - 1] = tsumolist.get(5);
			} else {
				p[1][2] = p[0][width - 1];
				p[2][2] = p[1][width - 1];
				p[0][width - 1] = p[3][width - 1];
				p[1][width - 1] = p[4][width - 1];
				p[3][width - 1] = tsumolist.get(moves * 2 + 2);
				p[4][width - 1] = tsumolist.get(moves * 2 + 3);
			}
		}
	}

	// ツモ設置
	void Drop() {
		// 14段目消去
		for (int i1 = 0; i1 < width - 2; i1++) {
			if (p[0][i1] != 0)
				p[0][i1] = 0;
		}

		// ツモ設置
		for (int i = 0; i < width - 2; i++) {
			for (int j = 0; j < height - 1; j++) {
				if (p[j][i] != 0 && p[j + 1][i] == 0) {
					p[j + 1][i] = p[j][i];
					p[j][i] = 0;
					j = 0;
				}
			}
		}
	}

	// ペイント
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		// デッドスペース
		if (p[2][2] == 0) {
			g.setColor(new Color(255, 200, 200));
			g.fillRect(2 * size, 2 * size, size, size);
		}

		// 画面全体
		for (int i1 = 0; i1 < height; i1++) {
			for (int i2 = 0; i2 < width; i2++) {
				p[i1][width - 2] = 6;
				// 画面上部
				for (int a = 0; a < 2; a++) {
					if (p[a][i2] == 0) {
						g.setColor(Color.LIGHT_GRAY);
						g.fillRect(i2 * size, a * size, size, size);
					}
				}
				// 色付け
				switch (p[i1][i2]) {
				case 1:
					g.setColor(Color.RED);
					g.fillOval(i2 * size, i1 * size, size, size);
					break;
				case 2:
					g.setColor(Color.BLUE);
					g.fillOval(i2 * size, i1 * size, size, size);
					break;
				case 3:
					g.setColor(Color.GREEN);
					g.fillOval(i2 * size, i1 * size, size, size);
					break;
				case 4:
					g.setColor(Color.YELLOW);
					g.fillOval(i2 * size, i1 * size, size, size);
					break;
				case 5:
					g.setColor(Color.GRAY);
					g.fillOval(i2 * size, i1 * size, size, size);
					break;
				case 6:
					g.setColor(Color.BLACK);
					g.fillRect(i2 * size, i1 * size, size, size);
					break;
				}
			}
		}
		// 画面表示
		g.setColor(Color.RED);
		g.drawString(screen_chain + "連鎖", 10, 20);
		g.drawString(score + "点", 10, 40);
		g.drawString(moves + "手", 10, 60);
	}

	// キー操作
	class Key_e extends KeyAdapter {
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_LEFT) {
				if (rotate == 0) {
					if (p[py][px - 1] == 0 && px > 1) {
						p[py][px - 1] = p[py][px]; // ��
						p[py - 1][px - 1] = p[py - 1][px];
						p[py][px] = 0;
						p[py - 1][px] = 0;
						px--;
						repaint();
					}
				} else if (rotate == 1) {
					if (p[py][px - 1] == 0 && px > 1) {
						p[py][px - 1] = p[py][px]; // ��
						p[py][px] = p[py][px + 1];
						p[py][px + 1] = 0;
						px--;
						repaint();
					}
				} else if (rotate == 2) {
					if (p[py + 1][px - 1] == 0 && px > 1) {
						p[py][px - 1] = p[py][px]; // ��
						p[py + 1][px - 1] = p[py + 1][px];
						p[py][px] = 0;
						p[py + 1][px] = 0;
						px--;
						repaint();
					}
				} else if (rotate == 3) {
					if (p[py][px - 2] == 0 && px - 1 > 1) {
						p[py][px - 2] = p[py][px - 1];
						p[py][px - 1] = p[py][px]; // ��
						p[py][px] = 0;
						px--;
						repaint();
					}
				}

			} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
				if (rotate == 0) {
					if (p[py][px + 1] == 0 && px < width - 2) {
						p[py][px + 1] = p[py][px]; // ��
						p[py - 1][px + 1] = p[py - 1][px];
						p[py][px] = 0;
						p[py - 1][px] = 0;
						px++;
						repaint();
					}
				} else if (rotate == 1) {
					if (p[py][px + 2] == 0 && px + 1 < width - 2) {
						p[py][px + 2] = p[py][px + 1];
						p[py][px + 1] = p[py][px]; // ��
						p[py][px] = 0;
						px++;
						repaint();
					}
				} else if (rotate == 2) {
					if (p[py + 1][px + 1] == 0 && px < width - 2) {
						p[py][px + 1] = p[py][px]; // ��
						p[py + 1][px + 1] = p[py + 1][px];
						p[py][px] = 0;
						p[py + 1][px] = 0;
						px++;
						repaint();
					}
				} else if (rotate == 3) {
					if (p[py][px + 1] == 0 && px < width - 2) {
						p[py][px + 1] = p[py][px]; // ��
						p[py][px] = p[py][px - 1];
						p[py][px - 1] = 0;
						px++;
						repaint();
					}
				}
			} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
				Rensa();
			} else if (e.getKeyCode() == KeyEvent.VK_X) {
				if (rotate == 0) {
					if (p[py][px + 1] != 0 && p[py][px - 1] != 0) {
						rotate = 4;
					} else if (p[py][px + 1] != 0 || px == width - 2) {
						p[py][px - 1] = p[py][px]; // ��
						p[py][px] = p[py - 1][px];
						p[py - 1][px] = 0;
						px--;
						rotate = 1;
					} else {
						p[py][px + 1] = p[py - 1][px];
						p[py - 1][px] = 0;
						rotate = 1;
					}
					repaint();
				} else if (rotate == 1) {
					if (p[py + 1][px] != 0 && py == 0) {
					} else if (p[py + 1][px] != 0 && py != 0) {
						p[py - 1][px] = p[py][px]; // ��
						p[py][px] = p[py][px + 1];
						p[py][px + 1] = 0;
						py--;
						rotate = 2;
					} else {
						p[py + 1][px] = p[py][px + 1];
						p[py][px + 1] = 0;
						rotate = 2;
					}
					repaint();
				} else if (rotate == 2) {
					if (p[py][px + 1] != 0 && p[py][px - 1] != 0) {
						rotate = 5;
					} else if (p[py][px - 1] != 0 || px == 1) {
						p[py][px + 1] = p[py][px];
						p[py][px] = p[py + 1][px]; // ��
						p[py + 1][px] = 0;
						px++;
						rotate = 3;
					} else {
						p[py][px - 1] = p[py + 1][px];
						p[py + 1][px] = 0;
						rotate = 3;
					}
					repaint();
				} else if (rotate == 3) {
					if (py == 0) {
					} else {
						p[py - 1][px] = p[py][px - 1];
						p[py][px - 1] = 0;
						rotate = 0;
					}
					repaint();
				} else if (rotate == 4) {
					if (p[py + 1][px] != 0) {
						int a = p[py - 1][px];
						p[py - 1][px] = p[py][px];
						p[py][px] = a;
						py--;
					} else {
						p[py + 1][px] = p[py - 1][px];
						p[py - 1][px] = 0;
					}
					rotate = 2;
					repaint();
				} else if (rotate == 5) {
					p[py - 1][px] = p[py + 1][px];
					p[py + 1][px] = 0;
					rotate = 0;
					repaint();
				}

			} else if (e.getKeyCode() == KeyEvent.VK_Z) {
				if (rotate == 0) {
					if (p[py][px + 1] != 0 && p[py][px - 1] != 0) {
						rotate = 4;
					} else if (p[py][px - 1] != 0 || px == 1) {
						p[py][px + 1] = p[py][px]; // ��
						p[py][px] = p[py - 1][px];
						p[py - 1][px] = 0;
						px++;
						rotate = 3;
					} else {
						p[py][px - 1] = p[py - 1][px];
						p[py - 1][px] = 0;
						rotate = 3;
					}
					repaint();
				} else if (rotate == 1) {
					if (py == 0) {
					} else {
						p[py - 1][px] = p[py][px + 1];
						p[py][px + 1] = 0;
						rotate = 0;
					}
					repaint();
				} else if (rotate == 2) {
					if (p[py][px + 1] != 0 && p[py][px - 1] != 0) {
						rotate = 5;
					} else if (p[py][px + 1] != 0 || px == width - 2) {
						p[py][px - 1] = p[py][px];
						p[py][px] = p[py + 1][px]; // ��
						p[py + 1][px] = 0;
						px--;
						rotate = 1;
					} else {
						p[py][px + 1] = p[py + 1][px];
						p[py + 1][px] = 0;
						rotate = 1;
					}
					repaint();
				} else if (rotate == 3) {
					if (p[py + 1][px] != 0 && py == 0) {
					} else if (p[py + 1][px] != 0 && py != 0) {
						p[py - 1][px] = p[py][px]; // ��
						p[py][px] = p[py][px - 1];
						p[py][px - 1] = 0;
						py--;
						rotate = 2;
					} else {
						p[py + 1][px] = p[py][px - 1];
						p[py][px - 1] = 0;
						rotate = 2;
					}
					repaint();
				} else if (rotate == 4) {
					if (p[py + 1][px] != 0) {
						int a = p[py - 1][px];
						p[py - 1][px] = p[py][px];
						p[py][px] = a;
						py--;
					} else {
						p[py + 1][px] = p[py - 1][px];
						p[py - 1][px] = 0;
					}
					rotate = 2;
					repaint();
				} else if (rotate == 5) {
					p[py - 1][px] = p[py + 1][px];
					p[py + 1][px] = 0;
					rotate = 0;
					repaint();
				}
			}
		}
	}

	void Put(int i1) {
		switch (i1) {
		case 0:
			p[1][1] = p[1][3];
			p[2][1] = p[2][3];
			p[1][3] = 0;
			p[2][3] = 0;
			Rensa();
			break;
		case 1:
			p[1][2] = p[1][3];
			p[2][2] = p[2][3];
			p[1][3] = 0;
			p[2][3] = 0;
			Rensa();
			break;
		case 2:
			Rensa();
			break;
		case 3:
			p[1][4] = p[1][3];
			p[2][4] = p[2][3];
			p[1][3] = 0;
			p[2][3] = 0;
			Rensa();
			break;
		case 4:
			p[1][5] = p[1][3];
			p[2][5] = p[2][3];
			p[1][3] = 0;
			p[2][3] = 0;
			Rensa();
			break;
		case 5:
			p[1][6] = p[1][3];
			p[2][6] = p[2][3];
			p[1][3] = 0;
			p[2][3] = 0;
			Rensa();
			break;
		case 6:
			p[2][1] = p[1][3];
			p[1][1] = p[2][3];
			p[1][3] = 0;
			p[2][3] = 0;
			Rensa();
			break;
		case 7:
			p[2][2] = p[1][3];
			p[1][2] = p[2][3];
			p[1][3] = 0;
			p[2][3] = 0;
			Rensa();
			break;
		case 8:
			p[0][3] = p[2][3];
			p[2][3] = p[1][3];
			p[1][3] = p[0][3];
			p[0][3] = 0;
			Rensa();
			break;
		case 9:
			p[2][4] = p[1][3];
			p[1][4] = p[2][3];
			p[1][3] = 0;
			p[2][3] = 0;
			Rensa();
			break;
		case 10:
			p[2][5] = p[1][3];
			p[1][5] = p[2][3];
			p[1][3] = 0;
			p[2][3] = 0;
			Rensa();
			break;
		case 11:
			p[2][6] = p[1][3];
			p[1][6] = p[2][3];
			p[1][3] = 0;
			p[2][3] = 0;
			Rensa();
			break;
		case 12:
			p[2][1] = p[2][3];
			p[2][2] = p[1][3];
			p[1][3] = 0;
			p[2][3] = 0;
			Rensa();
			break;
		case 13:
			p[2][2] = p[2][3];
			p[2][3] = p[1][3];
			p[1][3] = 0;
			Rensa();
			break;
		case 14:
			p[2][4] = p[1][3];
			p[1][3] = 0;
			Rensa();
			break;
		case 15:
			p[2][4] = p[2][3];
			p[2][5] = p[1][3];
			p[1][3] = 0;
			p[2][3] = 0;
			Rensa();
			break;
		case 16:
			p[2][5] = p[2][3];
			p[2][6] = p[1][3];
			p[1][3] = 0;
			p[2][3] = 0;
			Rensa();
			break;
		case 17:
			p[2][1] = p[1][3];
			p[2][2] = p[2][3];
			p[1][3] = 0;
			p[2][3] = 0;
			Rensa();
			break;
		case 18:
			p[2][2] = p[1][3];
			p[1][3] = 0;
			Rensa();
			break;
		case 19:
			p[2][4] = p[2][3];
			p[2][3] = p[1][3];
			p[1][3] = 0;
			Rensa();
			break;
		case 20:
			p[2][4] = p[1][3];
			p[2][5] = p[2][3];
			p[1][3] = 0;
			p[2][3] = 0;
			Rensa();
			break;
		case 21:
			p[2][5] = p[1][3];
			p[2][6] = p[2][3];
			p[1][3] = 0;
			p[2][3] = 0;
			Rensa();
			break;
		}
	}

	int Score_chain(int Rensa) {
		if (Rensa == 2)
			return 8;
		else if (Rensa == 3)
			return 16;
		else if (Rensa == 4)
			return 32;
		else if (Rensa == 5)
			return 64;
		else if (Rensa == 6)
			return 96;
		else if (Rensa == 7)
			return 128;
		else if (Rensa == 8)
			return 160;
		else if (Rensa == 9)
			return 192;
		else if (Rensa == 10)
			return 224;
		else if (Rensa == 11)
			return 256;
		else if (Rensa == 12)
			return 288;
		else if (Rensa == 13)
			return 320;
		else if (Rensa == 14)
			return 352;
		else if (Rensa == 15)
			return 384;
		else if (Rensa == 16)
			return 416;
		else if (Rensa == 17)
			return 448;
		else if (Rensa == 18)
			return 480;
		else if (Rensa == 19)
			return 512;
		else
			return 0;
	}

	int Score_color(int color) {
		if (color == 2)
			return 3;
		else if (color == 3)
			return 6;
		else if (color == 4)
			return 12;
		else if (color == 5)
			return 24;
		else
			return 0;
	}

	int Score_connect(int connect) {
		if (connect == 5)
			return 2;
		else if (connect == 6)
			return 3;
		else if (connect == 7)
			return 4;
		else if (connect == 8)
			return 5;
		else if (connect == 9)
			return 6;
		else if (connect == 10)
			return 7;
		else if (connect >= 11)
			return 10;
		else
			return 0;
	}
}