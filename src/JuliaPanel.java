import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.SwingWorker;


/**
 * The panel which displays the Julia set and contains all the calculations
 * needed to draw it.
 * @author Daniel
 *
 */
@SuppressWarnings("serial")
public class JuliaPanel extends JPanel{
	
	/**
	 * The numerical value of each pixel across.
	 */
	private double pixelValueX;
	/**
	 * The numerical value of each pixel down.
	 */
	private double pixelValueY;
	/**
	 * The maximum number of iterations to go up to.
	 */
	private int max;
	/**
	 * The base complex number.
	 */
	private Complex c;
	/**
	 * The X coordinate of the centre of the screen.
	 */
	private int middleXCo;
	/**
	 * The Y coordinate of the centre of the screen.
	 */
	private int middleYCo;
	/**
	 * The buffered image showing the julia set if drawn.
	 */
	private BufferedImage julia;
	/**
	 * One of five threads which paints a given area of a given buffered image.
	 */
	private JuliaWorker section1;
	/**
	 * One of five threads which paints a given area of a given buffered image.
	 */
	private JuliaWorker section2;
	/**
	 * One of five threads which paints a given area of a given buffered image.
	 */
	private JuliaWorker section3;
	/**
	 * One of five threads which paints a given area of a given buffered image.
	 */
	private JuliaWorker section4;
	/**
	 * One of five threads which paints a given area of a given buffered image.
	 */
	private JuliaWorker section5;
	/**
	 * An int representation of the type of fractal formula to be used.
	 */
	private int fractalType;
	/**
	 * An int representation of the number of threads being used on the buffered
	 * image. A 0 means single thread, while 1 means multi threads.
	 */
	private int threadType;
	
	/**
	 * Creates a JuliaPanel, with default values for the max number
	 * of iterations and the fractal formula to be used. Sets the base
	 * complex number to the number given.
	 * 
	 * @param c The given complex number.
	 */
	public JuliaPanel(Complex c, int fractalType){
		this.c = c;
		max = 100;
		this.fractalType = fractalType;
	}
	
	/**
	 * Sets the base complex number to the number given.
	 * 
	 * @param c The given complex number.
	 */
	public void setC(Complex c){
		this.c = c;
	}
	
	/**
	 * Sets the type of fractal formula that should be used on the
	 * Julia set.
	 * @param fractalType An int representation of the fractal type.
	 */
	public void setFractalType(int fractalType){
		this.fractalType = fractalType;
	}

	/** 
	 * Paints the buffered image containing the Julia Set to the screen. First creates the
	 * buffered image and then once the threads have finished it it's painted to the screen.
	 * 
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	protected void paintComponent(Graphics g){
		super.paintComponent(g);
		int height = this.getHeight();
		int width = this.getWidth();
		pixelValues(height, width);
		julia = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		generate(julia);
		g.drawImage(julia, 0, 0, null);
	}
	
	/**
	 * Generates the Julia set buffered image. Will either use one thread for the
	 * whole image or five threads to each paint a section of the buffered image depending
	 * on the thread type option. Gives each thread the section of the image for it 
	 * to paint and starts each thread or for single threaded loops over the whole screen
	 * going down the screen first for each pixel across.
	 * 
	 * @param julia The buffered image being generated.
	 */
	private void generate(BufferedImage julia){
		int width = this.getWidth()/5;
		int height = this.getHeight();
		switch(threadType){
		case 0:
			//loops for every pixel on the screen, going down the screen first for every pixel across
					for(int x = 0; x < this.getWidth(); x++){
						for(int y = 0; y < this.getHeight(); y++){
							int	paintColour = juliaIterations(((x-middleXCo)*pixelValueX), ((middleYCo-y)*pixelValueY));
							Color myColour = (paintColour==max) ? Color.BLACK : new Color(255, 255-((int) (paintColour*7) % 255), 0);
							julia.setRGB(x, y, myColour.getRGB());
						}
					}
			break;
		case 1:
			section1 = new JuliaWorker(0, width, height);
			section1.execute();
			section2 = new JuliaWorker(width, width*2, height);
			section2.execute();
			section3 = new JuliaWorker(width*2, width*3, height);
			section3.execute();
			section4 = new JuliaWorker(width*3, width*4, height);
			section4.execute();
			section5 = new JuliaWorker(width*4, this.getWidth(), height);
			section5.execute();
			//waits until all the threads have finished
			while(!(section1.finish && section2.finish && section3.finish && section4.finish && section5.finish)){
			}
			break;
		}
		
		
	}
	
	/**
	 * Allows the buffered image of the Julia Set to be accessed.
	 * 
	 * @return The Julia set buffered image.
	 */
	public BufferedImage getJulia(){
		return julia;
	}
	
	/**
	 * Calculates the value of each pixel in both the x and y axis' and 
	 * calculates the coordinates of the middle of the Panel
	 *  
	 * @param height The height of the JPanel
	 * @param width The width of the JPanel
	 */
	public void pixelValues(int height, int width) {
		pixelValueX = 4.0/width;
		pixelValueY = 3.2/height;
		middleXCo = width / 2 + 1;
		middleYCo = height / 2 + 1;
	}
	
	/**
	 * Iterates over the given Complex Number using the formula selected by the switch case
	 * statement for the correct set until the given number diverges. The number of iterations is kept track of.
	 * 
	 * @param real The real part of the current Complex Number.
	 * @param imaginary The imaginary part of the current Complex number.
	 * @return the number of iterations before complex number diverges.
	 */
	private int juliaIterations(double real, double imaginary){
		Complex previous = new Complex(real, imaginary);
		int iterations = 0;
		Complex next;
		while(previous.modulusSquared()<4 && iterations < max){
			switch (fractalType) {
			case 0: //ordinary mandelbrot set
				next = previous.square().add(c);
				break;
			case 1: //burning ship set
				next = previous.burningShip().add(c);
				break;
			case 2: //tricorn set
				next = previous.triCorn().add(c);
				break;
			case 3: //multibrot d=3
				next = previous.square().multiply(previous).add(c);
				break;
			case 4: //multibrot d=4
				next = previous.square().square().add(c);
				break;
			case 5: //multibrot d=5
				next = previous.square().square().multiply(previous).add(c);
				break;
			case 6: //multibrot d=6
				next = previous.square().square().multiply(previous.square()).add(c);
				break;
			default:
				next = previous.square().add(c);
				break;
			}		
			iterations++;
			previous = next;
		}
		return iterations;
	}
	/**
	 * Changes the thread option to be used to the given integer.
	 * 
	 * @param i The thread int representation given.
	 */
	public void setThread(int i) {
		this.threadType = i;
	}
	
	/**
	 * Thread to paint a section of the julia set to the Buffered Image.
	 * 
	 * @author Daniel
	 *
	 */
	@SuppressWarnings("rawtypes")
	class JuliaWorker extends SwingWorker{
		/**
		 * The upper x pixel to go up to.
		 */
		private int limit;
		/**
		 * The height to iterate over.
		 */
		private int height;
		/**
		 * The lower x pixel to start at.
		 */
		private int lower;
		/**
		 * Contains whether the thread has finished.
		 */
		private volatile boolean finish = false;
		/**
		 * The graphics used to colour in the julia set image.
		 */
		private Graphics2D drawer;
		
		/**
		 * Creates the thread, and gives the limits of the area it should colour.
		 * 
		 * @param lower The lower x pixel to start from.
		 * @param limit The upper x pixel limit to end at.
		 * @param height The height of the image to paint.
		 */
		public JuliaWorker(int lower, int limit, int height){
			this.limit = limit;
			this.height = height;
			this.lower = lower;
			drawer = julia.createGraphics();
		}
		/**
		 * Loops through the section given to colour, calculates the correct colour according
		 * to the number of iterations, and colours that pixel in. Sets a boolean to true when
		 * completely finished.
		 * @see javax.swing.SwingWorker#doInBackground()
		 */
		@Override
		protected Object doInBackground() throws Exception {
			for(int x = lower; x < limit; x++){
				for(int y = 0; y < height; y++){
					int	paintColour = juliaIterations(((x-middleXCo)*pixelValueX), ((middleYCo-y)*pixelValueY));
					Color myColour = (paintColour==max) ? Color.BLACK : new Color(255, 255-((int) (paintColour*7) % 255), 0);
					drawer.setColor(myColour);
					drawer.drawLine(x, y, x, y);
				}
			}
			this.finish = true;
			return null;
		}
		
	}
}
