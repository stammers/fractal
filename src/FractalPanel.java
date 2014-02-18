import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;




/**
 * The panel which displays the Mandelbrot set itself and contains all the  
 * calculations related to it.
 * 
 * @author Daniel
 * 
 */
@SuppressWarnings("serial")
public class FractalPanel extends JPanel {
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
	 * The X coordinate of the centre of the screen.
	 */
	private int middleXCo;
	/**
	 * The Y coordinate of the centre of the screen.
	 */
	private int middleYCo;
	/**
	 * The numerical value of the centre of the screen across.
	 */
	private double middleXVal;
	/**
	 * The numerical value of the centre of the screen down.
	 */
	private double middleYVal;
	/**
	 * The range across the real (x) axis.
	 */
	private double realAxis;
	/**
	 * The range across the imaginary (y) axis.
	 */
	private double imaginaryAxis;
	/**
	 * The bufferedImage showing the Mandelbrot fractal if drawn.
	 */
	private BufferedImage fractal;
	/**
	 * Holds whether the user is currently selecting an area to zoom.
	 */
	private boolean zooming = false;
	/**
	 * The zoom box showing where the user is selecting to zoom.
	 */
	private Rectangle zoomBox = null;
	/**
	 * The colour of the zoom highlighting rectangle
	 */
	private Color zoom = new Color(0.0f, 0.0f, 1.0f, 0.5f);
	/**
	 * One of five threads which paints a given area of a given buffered image.
	 */
	private FractalWorker section1;
	/**
	 * One of five threads which paints a given area of a given buffered image.
	 */
	private FractalWorker section2;
	/**
	 * One of five threads which paints a given area of a given buffered image.
	 */
	private FractalWorker section3;
	/**
	 * One of five threads which paints a given area of a given buffered image.
	 */
	private FractalWorker section4;
	/**
	 * One of five threads which paints a given area of a given buffered image.
	 */
	private FractalWorker section5;
	/**
	 * An int representation of the fractalType currently being shown.
	 */
	private int fractalType;
	/**
	 * An int representation of the number of threads being used on the buffered
	 * image. A 0 means single thread, while 1 means multi threads.
	 */
	private int threadType;
	/**
	 * Holds whether zooming in on the fractal is animated or not.
	 */
	private boolean zoomAnimate;


	/**
	 * Creates a Default FractalPanel with default values for max, realAxis and 
	 * imaginaryAxis. Adds the mouse listener for zooming, and sets default
	 * values for the thread type, fractal type and sets to use zoom animations.
	 */
	public FractalPanel(){
		max = 100;
		realAxis = 4.0;
		imaginaryAxis = 3.2;
		middleYVal = 0.0;
		middleXVal = 0.0;
		FractalMouseListener zoomer = new FractalMouseListener();
		this.addMouseListener(zoomer);
		this.addMouseMotionListener(zoomer);
		fractalType = 0;
		threadType = 0;
		zoomAnimate = true;
	}

	/**
	 * Sets the type of fractal that should be displayed on screen.
	 * @param fractalType An int representation of the fractal type.
	 */
	public void setFractalType(int fractalType){
		this.fractalType = fractalType;
	}
	
	/**
	 * Returns the number equivalent of the fractal type being displayed
	 * on the screen.
	 * 
	 * @return int equivalent of fractal type.
	 */
	public int getFractalType(){
		return this.fractalType;
	}

	/** 
	 * Paints the buffered image of the fractal onto the screen,
	 * will also paint the zoom box on the screen if zooming in.
	 * First creates the buffered image and then once the threads 
	 * have finished it it's painted to the screen.
	 * Doesn't update the buffered image as the zoom box is being
	 * drawn.
	 * 
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	protected void paintComponent(Graphics g){
		super.paintComponent(g);
		int height = getHeight();
		int width = getWidth();
		pixelValues(height, width);

		/*if its not zooming then the buffered image can be redrawn. If it's zooming nothing will change so
		 * for speed the buffered image doesn't need to be updated.
		 */
		
		if(!zooming){
			fractal = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			generate(fractal);

		}
		
		//draws the image to the screen.
		g.drawImage(fractal, 0, 0, null);
		Graphics2D g2d = (Graphics2D) g;

		//if it's zooming draw the zoom box
		if(zooming){
			g2d.setColor(zoom);
			g2d.fill(zoomBox);			
		}	
	}

	/**
	 * Allows the buffered image of the fractal to be accessed.
	 * 
	 * @return The fractal buffered image.
	 */
	public BufferedImage getFractal(){
		return fractal;
	}
	
	/**
	 * Generates the Mandelbrot fractal buffered image. Will either use one thread for the
	 * whole image or five threads to each paint a section of the buffered image depending
	 * on the thread type option. Gives each thread the section of the image for it 
	 * to paint and starts each thread or for single threaded loops over the whole screen
	 * going down the screen first for each pixel across.
	 * 
	 * @param fractal The buffered image being generated.
	 */
	private void generate(BufferedImage fractal) {
		int width = this.getWidth()/5;
		int height = this.getHeight();
		
		switch(threadType){
		case 0:
			//loops for every pixel on the screen, going down the screen first for every pixel across
			for(int x = 0; x < this.getWidth(); x++){
				for(int y = 0; y < this.getHeight(); y++){
					double paintColour = mandelbrotIterations(((x-middleXCo)*pixelValueX)+middleXVal, ((middleYCo-y)*pixelValueY)+middleYVal);
					//int smoothShaded = (int) (paintColour + 1 - Math.log10(Math.log10(Math.sqrt(previous.modulusSquared()))) / Math.log10(2));
					Color myColour = (paintColour==max) ? Color.BLACK : new Color(255, 255-((int) (paintColour*7) % 255), 0);
					/*
					Color myColour = (paintColour==max) ? Color.BLACK :new Color(255, 255-((int) (smoothShaded*7) % 255), 0);
					*/
					fractal.setRGB(x, y, myColour.getRGB());
				
				}
			}
			break;
		case 1:
			//each thread is given a section of the buffered image to work with
			section1 = new FractalWorker(0, width, height);
			section1.execute();
			section2 = new FractalWorker(width, width*2, height);
			section2.execute();
			section3 = new FractalWorker(width*2, width*3, height);
			section3.execute();
			section4 = new FractalWorker(width*3, width*4, height);
			section4.execute();
			section5 = new FractalWorker(width*4, getWidth(), height);
			section5.execute();
			//waits until all the threads have finished creating their part of the image.
			while(!(section1.finish && section2.finish && section3.finish && section4.finish && section5.finish)){
			}
			break;
			
		}
		
		

	}

	/**
	 * Calculates the value of each pixel in both the x and y axis' and 
	 * calculates the coordinates of the middle of the Panel.
	 *  
	 * @param height The height of the JPanel.
	 * @param width The width of the JPanel.
	 */
	public void pixelValues(int height, int width) {
		pixelValueX = realAxis/width;
		pixelValueY = imaginaryAxis/height;
		middleXCo = width / 2 + 1;
		middleYCo = height / 2 + 1;
	}

	/**
	 * Finds the real number equivalent of an x pixel value and returns
	 * it.
	 * 
	 * @param x The x pixel value.
	 * @return The real number.
	 */
	public double xValue(int x){
		return ((x-middleXCo)*pixelValueX)+middleXVal;
	}

	/**
	 * Finds the imaginary number equivalent of a y pixel value and returns
	 * it.
	 * 
	 * @param y The y pixel value.
	 * @return The imaginary number.
	 */
	public double yValue(int y){
		return ((middleYCo-y)*pixelValueY)+middleYVal;
	}

	/**
	 * Iterates over the given Complex Number using the formula selected by the switch case
	 * statement for the correct set until the given number diverges. The number of iterations is kept track of.
	 * 
	 * @param real The real part of the current Complex Number.
	 * @param imaginary The imaginary part of the current Complex number.
	 * @return the number of iterations before complex number diverges.
	 */
	private double mandelbrotIterations(double real, double imaginary){
		Complex current = new Complex(real, imaginary);
		int iterations = 0;
		Complex next;
		Complex previous = current;
		while(previous.modulusSquared()<4 && iterations < max){
			switch (fractalType) {
			case 0: //ordinary mandelbrot set
				next = previous.square().add(current);
				break;
			case 1: //burning ship set
				next = previous.burningShip().add(current);
				break;
			case 2: //tricorn set
				next = previous.triCorn().add(current);
				break;
			case 3: //multibrot d=3
				next = previous.square().multiply(previous).add(current);
				break;
			case 4: //multibrot d=4
				next = previous.square().square().add(current);
				break;
			case 5: //multibrot d=5
				next = previous.square().square().multiply(previous).add(current);
				break;
			case 6: //multibrot d=6
				next = previous.square().square().multiply(previous.square()).add(current);
				break;
			default:
				next = previous.square().add(current);
				break;
			}		
			iterations++;
			previous = next;
		}
		return iterations;

		
	}
	

	/**
	 * Updates the values for the plane, both imaginary and real, recalculates the 
	 * value in the centre of the screen and redraws the Mandelbrot set using 
	 * these new values.
	 * 
	 * @param xLower The lower bound of the real axis.
	 * @param xUpper The upper bound of the real axis.
	 * @param yLower The lower bound of the imaginary axis.
	 * @param yUpper The upper bound of the imaginary axis.
	 */
	public void updatePlane(double xLower, double xUpper, double yLower, double yUpper){
		realAxis = xUpper - xLower;
		imaginaryAxis = yUpper - yLower;
		middleXVal = (xUpper + xLower)/2;
		middleYVal = (yUpper + yLower)/2;
		repaint();
	}

	/**
	 * Changes the maximum number of iterations to try in the Mandelbrot Set and 
	 * redraws the Mandelbrot set.
	 * 
	 * @param max The maximum number of iterations.
	 */
	public void changeIterations(int max){
		this.max = max;
		repaint();
	}

	/**
	 * Calculates the real and imaginary parts of a complex number from a point
	 * on the screen given.
	 * 
	 * @param x The x co-ordinate of the screen point.
	 * @param y The y co-ordinate of the screen point.
	 * @return The Complex number equivalent of the given screen point.
	 */
	public Complex calculatePoint(int x, int y){
		double real = ((x-middleXCo)*pixelValueX)+middleXVal;
		double imaginary = ((middleYCo-y)*pixelValueY)+middleYVal;
		return new Complex(real, imaginary);
	}
	
	/**
	 * Changes the thread option to be used to the given integer.
	 * 
	 * @param i The int representation of the thread type given.
	 */
	public void setThread(int i) {
		this.threadType = i;
	}
	
	/**
	 * Changes the option to use zoom animations or not.
	 * 
	 * @param animate The boolean option.
	 */
	public void setAnimate(boolean animate){
		this.zoomAnimate = animate;
	}
	
	/**
	 * Used when the user is zooming in to calculate points
	 * and create the zoom box to be shown on screen.
	 * 
	 * @author Daniel
	 *
	 */
	public class FractalMouseListener extends MouseAdapter {

		/**
		 * The initial point the user clicks.
		 */
		private Point mouseStart = null;
		/**
		 * The final x co-ordinate when the user releases the
		 * mouse.
		 */
		private int finalX;
		/**
		 * The final y co-ordinate when the user releases the
		 * mouse.
		 */
		private int finalY;

		/**
		 * Stores the point when the mouse is pressed.
		 * 
		 * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
		 */
		@Override
		public void mousePressed(MouseEvent e) {
			mouseStart = e.getPoint();
		}

		/**
		 * Calculates the distance from the start point to where
		 * the mouse currently is in both x and y axis and creates
		 * a new rectangle using these points.
		 * 
		 * @see java.awt.event.MouseAdapter#mouseDragged(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseDragged(MouseEvent e) {
			zooming = true;
			int x = Math.min(mouseStart.x, e.getPoint().x);
			int y = Math.min(mouseStart.y, e.getPoint().y);
			int width = Math.abs(mouseStart.x - e.getPoint().x);
			int height = Math.abs(mouseStart.y - e.getPoint().y);
			zoomBox = new Rectangle(x, y, width, height);
			repaint();

		}

		/**
		 * Zooms the display in if an area was selected and removes the zoom box
		 * from the screen. The zoom box can be dragged in any direction.
		 * 
		 *  @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseReleased(MouseEvent e) {
			finalX = e.getX();
			finalY = e.getY();
			zooming = false;
			final double differenceXRight;
			final double differenceXLeft;
			final double differenceYTop;
			final double differenceYBottom;
			//doesn't trigger if it was just a mouse click
			if(finalX != mouseStart.x && finalY != mouseStart.y){
				//can zoom with the user dragging the rectangle in any direction
				zoomBox = null;
				final double upperX = middleXVal + (realAxis/2);
				final double lowerX = middleXVal - (realAxis/2);
				final double topY = middleYVal+(imaginaryAxis/2);
				final double bottomY = middleYVal - (imaginaryAxis/2);
				if(finalX > mouseStart.x){
					differenceXRight = upperX-xValue(finalX);
					differenceXLeft = xValue(mouseStart.x) - lowerX;
					if(finalY > mouseStart.y){
						//uses zoom animation if option is selected
						if(zoomAnimate){
							differenceYTop = topY-yValue(mouseStart.y);
							differenceYBottom = yValue(finalY) - bottomY;
							updatePlane(lowerX+(differenceXLeft/2), upperX - (differenceXRight/2), bottomY+(differenceYBottom/2), topY-(differenceYTop/2));
							SwingUtilities.invokeLater(new Runnable(){
								@Override
								public void run() {
									updatePlane(lowerX+differenceXLeft, upperX - differenceXRight, bottomY+differenceYBottom, topY-differenceYTop);
								}	
							});
						}else{
							//no zoom animation
							updatePlane(xValue(mouseStart.x), xValue(finalX), yValue(finalY), yValue(mouseStart.y));	
						}	
					}else{
						//uses zoom animation if option is selected
						if(zoomAnimate){
							differenceYTop = topY-yValue(finalY);
							differenceYBottom = yValue(mouseStart.y) - bottomY;
							updatePlane(lowerX+(differenceXLeft/2), upperX - (differenceXRight/2), bottomY+(differenceYBottom/2), topY-(differenceYTop/2));
							SwingUtilities.invokeLater(new Runnable(){
								@Override
								public void run() {
									updatePlane(lowerX+differenceXLeft, upperX - differenceXRight, bottomY+differenceYBottom, topY-differenceYTop);
								}	
							});
						}else{
							//no zoom animation
							updatePlane(xValue(mouseStart.x), xValue(finalX), yValue(mouseStart.y), yValue(finalY));

						}
						
					}	
				}else{
					differenceXRight = (middleXVal + (realAxis/2))-xValue(mouseStart.x);
					differenceXLeft = xValue(finalX) -(middleXVal - (realAxis/2));
					if(finalY > mouseStart.y){
						//uses zoom animation if option is selected
						if(zoomAnimate){
							differenceYTop = topY-yValue(mouseStart.y);
							differenceYBottom = yValue(finalY) - bottomY;
							updatePlane(lowerX+(differenceXLeft/2), upperX - (differenceXRight/2), bottomY+(differenceYBottom/2), topY-(differenceYTop/2));
							SwingUtilities.invokeLater(new Runnable(){
								@Override
								public void run() {
									updatePlane(lowerX+differenceXLeft, upperX - differenceXRight, bottomY+differenceYBottom, topY-differenceYTop);
								}	
							});	
						}else{
							//no zoom animation
							updatePlane(xValue(finalX), xValue(mouseStart.x), yValue(finalY), yValue(mouseStart.y));
						}
						
					}else{
						
						if(zoomAnimate){
							//uses zoom animation if option is selected
							differenceYTop = topY-yValue(finalY);
							differenceYBottom = yValue(mouseStart.y) - bottomY;
							updatePlane(lowerX+(differenceXLeft/2), upperX - (differenceXRight/2), bottomY+(differenceYBottom/2), topY-(differenceYTop/2));
							SwingUtilities.invokeLater(new Runnable(){
								@Override
								public void run() {
									updatePlane(lowerX+differenceXLeft, upperX - differenceXRight, bottomY+differenceYBottom, topY-differenceYTop);
								}	
							});
						}else{
							//no zoom animation
							updatePlane(xValue(finalX), xValue(mouseStart.x), yValue(mouseStart.y), yValue(finalY));
						}
					}

				}
			}
			
		}
	}

	/**
	 * Thread to paint a section of the fractal to the Buffered Image.
	 * 
	 * @author Daniel
	 *
	 */
	@SuppressWarnings("rawtypes")
	class FractalWorker extends SwingWorker{
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
		private volatile boolean  finish = false;
		/**
		 * The graphics used to colour in the fractal image.
		 */
		private Graphics2D drawer;

		/**
		 * Creates the thread, and gives the limits of the area it should colour.
		 * 
		 * @param lower The lower x pixel to start from.
		 * @param limit The upper x pixel limit to end at.
		 * @param height The height of the image to paint.
		 */
		public FractalWorker(int lower, int limit, int height){
			this.limit = limit;
			this.height = height;
			this.lower = lower;
			drawer = fractal.createGraphics();
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
					double paintColour = mandelbrotIterations(((x-middleXCo)*pixelValueX)+middleXVal, ((middleYCo-y)*pixelValueY)+middleYVal);
					Color myColour = (paintColour==max) ? Color.BLACK : new Color(255, 255-((int) (paintColour*7) % 255), 0);
					drawer.setColor(myColour);
					drawer.drawLine(x, y, x, y);
				}
			}
			this.finish = true;
			//drawer.dispose();
			return null;
		}

	}
}
