import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JApplet;

public class Bong extends JApplet implements Runnable, KeyListener {
	private Thread thread = null;
	boolean threadSuspended = false;
	private double x, dx, y, dy;
	private int xSize, ySize;
	private double paddleXL, paddleYL, paddleXR, paddleYR;
	private double paddleSize;
	private String message;
	private final Font font = new Font("Monospaced", Font.PLAIN, 15);
	private int scoreR = 0, scoreL = 0;
	private int difficulty = 2;
	public final int terms = 5; // 勝敗条件
	public final double Cspeed = 1.8;	//スマッシュ時のボールスピードの倍率
    private double ballx = 6.2;
    private double bally = 4.0;
	private AudioClip edge, just;
	private int cnt = 0, max = 0;
	private final int speed = 17;

	private Image img;
	private Graphics offg;
	private int width, height;

	@Override
	public void init() {
		resize(500, 320);
		Dimension size = getSize();
		width = size.width;
		height = size.height;
		xSize = width;
		ySize = height - 80;
		paddleSize = 30;
		message = "Game started!";
		setFocusable(true);
		addKeyListener(this);

		edge = Applet.newAudioClip(getClass().getResource("edge.wav"));
		just = Applet.newAudioClip(getClass().getResource("just.wav"));

		img = createImage(width, height);
		offg = img.getGraphics();
	}

	private void initialize() {
		x = xSize / 2;
		y = ySize / 2;
		dx = 6.4;
		dy = 4.0;
		paddleYL = paddleYR = ySize / 2;
		paddleXL = 30;
		paddleXR = xSize - 30;
		pose();
	}

	@Override
	public void paint(Graphics g) {
		offg.clearRect(0, 0, width, height);

		offg.setColor(Color.BLACK);
		offg.drawRect(0, 0, xSize - 1, ySize - 1);
		offg.setColor(Color.MAGENTA.darker());
		offg.fillOval((int) (x - 3), (int) (y - 3), 6, 6);

		offg.setColor(Color.RED);
		offg.fillRect((int) (paddleXL - 2), (int) (paddleYL - paddleSize / 2), 4, (int) paddleSize);
		offg.setColor(Color.BLUE);
		offg.fillRect((int) (paddleXR - 2), (int) (paddleYR - paddleSize / 2), 4, (int) paddleSize);
		offg.setColor(Color.YELLOW);
        offg.fillRect((int)((paddleXR-2)), (int)(paddleYR-3), 4, (int)6);
        offg.fillRect((int)((paddleXL-2)), (int)(paddleYL-3), 4, (int)6);
		
		offg.setFont(font);
		offg.setColor(Color.GREEN.darker());
		offg.drawString(message, 5, ySize + 12);
		offg.drawString("Rally:" + cnt + " L:" + scoreL + " R:" + scoreR, 200, ySize + 12);
		offg.setColor(Color.RED.darker());
		offg.drawString("Left:  Z(D), W(U)", 5, ySize + 24);
		offg.setColor(Color.BLUE.darker());
		offg.drawString("Right: M(D), I(U)", 5, ySize + 36);
		offg.setColor(Color.ORANGE.darker());
		offg.drawString("Start: SPACE", 5, ySize + 48);
		offg.setColor(Color.GRAY.darker());
		offg.drawString("Difficulty:" + difficulty, 5, ySize + 60);

		g.drawImage(img, 0, 0, this);
	}

	public void run() {
		Thread thisThread = Thread.currentThread();
		while (thread == thisThread) {
			initialize();
			requestFocus();
			while (true) {
				x += dx;
				y += dy;
				if ((x - paddleXL) * (x - dx - paddleXL) <= 0) {
					double rY = y + dy * (paddleXL - x) / dx;
					if ((rY - paddleYL + paddleSize / 2) * (rY - paddleYL - paddleSize / 2) <= 0) {
                        if ((rY-paddleYL+3)*(rY-paddleYL-3)<=0) {
                            dx = ballx*Cspeed;
                            dy = bally*Cspeed;
                            just.play();
                        } else { 
                            dx = ballx;
                            dy = bally;
                        }
						x = 2 * paddleXL - x;
						dx *= -1;
                        ballx *= -1;
						message = "";
						edge.play();
						offg.drawString("Rally:" + ++cnt, 90, ySize + 12); repaint();
					}
				}
				if (x < 0) {
					x = -x;
					ballx *= -1;
					dx = ballx;
					dy = bally;
					scoreR++; // スコア処理
					rally();
					pose();
				}
				if ((x - paddleXR) * (x - dx - paddleXR) <= 0) {
					double rY = y + dy * (paddleXR - x) / dx;
					if ((rY - paddleYR + paddleSize / 2) * (rY - paddleYR - paddleSize / 2) <= 0) {
                        if ((rY-paddleYR+3)*(rY-paddleYR-3)<=0) {
                            dx = ballx*Cspeed;
                            dy = bally*Cspeed;
                            just.play();
                        } else { 
                            dx = ballx;
                            dy = bally;
                        }
						x = 2 * paddleXR - x;
						dx *= -1;
						ballx *= -1;
						message = "";
						edge.play();
						offg.drawString("Rally:" + ++cnt, 90, ySize + 12); repaint();
					}
				}
				if (x > xSize) {
					x = 2 * xSize - x;
					ballx *= -1;
					dx = ballx;
					dy = bally;
					scoreL++; // スコア処理
					rally();
					pose();
				}
				if (y < 0) {
					y = -y;
					dy *= -1;
					bally *= -1;
				}
				if (y > ySize) {
					y = 2 * ySize - y;
					dy *= -1;
					bally *= -1;
				}
				repaint();
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {}
			}
		}
	}

	@Override
	public void start() {
		if (thread == null) {
			thread = new Thread(this);
			thread.start();
		}
	}

	@Override
	public void stop() {
		thread = null;
	}

	public void pose() {
		x = xSize / 2;
		y = ySize / 2; // 球を中心へ
		paddleYL = paddleYR = ySize / 2;
		paddleXL = 30;
		paddleXR = xSize - 30; // パドル位置を戻す
		winloss_judg();
		repaint();
		threadSuspended = true;
		while (threadSuspended) { // threadSuspendedがtrueの間は
			synchronized (this) {
				try {
					wait(); // プロセスを休止する
				} catch (InterruptedException e) {}
			}
		}
	}
	
	public void rally() {
		max = ( max < cnt ) ? cnt : max;
		cnt = 0;
	}

	public synchronized void myResume() {
		notify(); // スレッドを再開する
	}

	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		if (threadSuspended == false) {
			switch (key) {
			case 'W': if ( paddleYL - speed >= 0 ) { paddleYL -= speed; } break;
			case 'Z': if ( paddleYL + speed <= ySize ) { paddleYL += speed; } break;
			case 'I': if ( paddleYR - speed >= 0 ) { paddleYR -= speed; } break;
			case 'M': if ( paddleYR + speed <= ySize ) { paddleYR += speed; } break;
			}
		} else {
			switch (key) {
			case KeyEvent.VK_SPACE:
				threadSuspended = false;
				myResume();
				break;
			case KeyEvent.VK_UP:
				if (difficulty < 3) {
					difficulty += 1;
					paddleSize = 50 - difficulty * 10;
					repaint();
				}
				break;
			case KeyEvent.VK_DOWN:
				if (difficulty > 1) {
					difficulty -= 1;
					paddleSize = 50 - difficulty * 10;
					repaint();
				}
				break;
			}
		}
	}

	public void winloss_judg() { // 勝敗判定
		if (scoreL >= terms) {
			message = "L won!" + " Max:" + max;
			scoreL = scoreR = 0;
		} else if (scoreR >= terms) {
			message = "R won!" + " Max:" + max;
			scoreL = scoreR = 0;
		} else {
			message = "";
		}
	}

	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}
}