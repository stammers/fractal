import javax.swing.SwingUtilities;


/**
 * The starting class, contains main and will draw the main frame
 * of the GUI.
 * @author Daniel
 *
 */
public class FractalGUI{


	/**
	 * Creates the main GUI frame, and calls the initialise method on it, setting 
	 * up all the components, listeners, etc.
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater ( 
				 new Runnable () {
				     public void run() {
				    	FractalFrame frame = new FractalFrame("Mandelbrot Set");
				 		frame.setBounds(0, 0, 850, 640);
				 		frame.init();
				 		
				     }
				 });
		

	}

}
