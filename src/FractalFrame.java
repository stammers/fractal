import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


/**
 * The main frame holding all the other components in the GUI.
 * Also contains many of the component listeners.
 * 
 * @author Daniel
 *
 */
@SuppressWarnings("serial")
public class FractalFrame extends JFrame {
	
	/**
	 * The JPanel holding the list of user added favourites.
	 */
	private Favourites fav;
	/**
	 * The last point clicked by the user.
	 */
	private Complex selectedPoint;
	/**
	 * The JFrame which will contain a juilaSet.
	 */
	private JFrame juliaSet;
	/**
	 * The Panel showing the julia set.
	 */
	private JuliaPanel julia;
	/**
	 * Button which resets everything on screen back to default.
	 */
	private JButton reset;
	/**
	 * Holds whether the julia set will autoupdate or not.
	 */
	private boolean autoUpdate;
	/**
	 * The Panel containing the list of julia sets added to favourites.
	 */
	private JPanel favourites;
	/**
	 * The panel showing the main fractal, depending on the user choice.
	 */
	private FractalPanel panel;
	/**
	 * The panel holding all the range and iteration settings at the bottom
	 * of the screen.
	 */
	private JPanel labelSection;
	/**
	 * A Reference to the frame itself, needed for creating pop up message
	 * boxes.
	 */
	private JFrame fractalFrame;
	/**
	 * Holds the dimensions of the users screen.
	 */
	private Dimension dimensions;
	/**
	 * Creates the JFrame with a title.
	 * 
	 * @param title The JFrame's title.
	 */
	public FractalFrame(String title){
		super(title);
		autoUpdate = false;
		fractalFrame = this;
		dimensions = Toolkit.getDefaultToolkit().getScreenSize();
	}
	

	/**
	 * Initialises all the various parts of the display, adding all the
	 * labels and panels to the main frame, and adds all the listeners
	 * needed to the correct components of the GUI.
	 */
	public void init(){
		juliaSet = new JFrame();
		fav  = new Favourites();
		juliaSet.setResizable(false);
		
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(false);
		
		//creates the menu at the top
		MyMenu menu = new MyMenu();
		menu.init();
		this.setJMenuBar(menu);
		final JPanel main = new JPanel(new BorderLayout());
		this.setContentPane(main);
		
		panel = new FractalPanel();
		main.add(panel, BorderLayout.CENTER);
		
		//sets up the favourites side panel
		favourites = new JPanel(new BorderLayout());
		main.add(favourites, BorderLayout.EAST);
		JLabel favTitle = new JLabel("Favourites");
		favTitle.setHorizontalAlignment(JLabel.CENTER);
		favTitle.setFont(favTitle.getFont().deriveFont(Font.BOLD));
		JButton addFavourite = new JButton("Add Favourite");
		favourites.add(fav, BorderLayout.CENTER);
		favourites.add(addFavourite, BorderLayout.SOUTH);
		favourites.add(favTitle, BorderLayout.NORTH);
		
		addFavourite.addMouseListener(new MouseAdapter(){
			/* 
			 * Adds the last clicked point to favourites.
			 */
			@Override
			public void mouseClicked(MouseEvent e){
				if(selectedPoint != null){
					fav.addFavourite(selectedPoint);
				}else{
					JOptionPane.showMessageDialog(fractalFrame,"Select a point first");
				}
				
			}
		});
		
		panel.addMouseMotionListener(new MouseMotionAdapter(){
			/*
			 * Sets the title to the complex number the mouse is currently at. Will autoupdate
			 * the juila panel if the option is selected.
			 */
			public void mouseMoved(MouseEvent e) {
				setTitle(panel.calculatePoint(e.getX(), e.getY()).toString());
				if(autoUpdate){
					Complex selectedPoint = panel.calculatePoint(e.getX(), e.getY());
					juliaSet.setTitle("Julia Set for " + selectedPoint.toString());
					if(julia == null){
						julia = new JuliaPanel(selectedPoint, panel.getFractalType());
						juliaSet.setContentPane(julia);
					}else{
						julia.setC(selectedPoint);
						julia.repaint();
					}
					
					if(dimensions.getWidth()>=1366){
						juliaSet.setBounds(getX()+getWidth()+10, getY(), 500, 500);
					}else{
						juliaSet.setSize(500, 500);
					}
					juliaSet.setVisible(true);
				}
			}
		});

		/*
		 * creates the lower section for all the labels and size options
		 * to be placed in.
		 */
		labelSection = new JPanel(new GridLayout(2, 1));
		JPanel upperLabelSection = new JPanel(new GridBagLayout());
		JPanel lowerLabelSection = new JPanel(new GridLayout(1, 2));
		labelSection.add(upperLabelSection);
		labelSection.add(lowerLabelSection);
		GridBagConstraints c = new GridBagConstraints();
		main.add(labelSection, BorderLayout.SOUTH);
		
		JLabel realAxisLbl = new JLabel("Real Axis:", SwingConstants.CENTER);
		JLabel imaginaryAxisLbl = new JLabel("Imaginary Axis:", SwingConstants.CENTER);
		JLabel to = new JLabel("to", SwingConstants.CENTER);
		JLabel to2 = new JLabel("to", SwingConstants.CENTER);
		JLabel iterationsLbl = new JLabel("Iterations:", SwingConstants.CENTER);
		
		//formats and adapters to ensure only certain input is allowed
		DecimalFormat formatting = new DecimalFormat("##.#####");
		KeyAdapter decimalOnly = new decimalOnly();
		KeyAdapter numbersOnly = new numbersOnly();
		
		final JFormattedTextField realAxisLower = new JFormattedTextField(formatting);
		realAxisLower.setHorizontalAlignment(JTextField.CENTER);
		//limits the field to decimal characters only
		realAxisLower.addKeyListener(decimalOnly);
		
		final JFormattedTextField realAxisUpper = new JFormattedTextField(formatting);
		realAxisUpper.setHorizontalAlignment(JTextField.CENTER);
		//limits the field to decimal characters only
		realAxisUpper.addKeyListener(decimalOnly);
		
		final JFormattedTextField imaginaryAxisLower = new JFormattedTextField(formatting);
		imaginaryAxisLower.setHorizontalAlignment(JTextField.CENTER);
		//limits the field to decimal characters only
		imaginaryAxisLower.addKeyListener(decimalOnly);
		
		final JFormattedTextField imaginaryAxisUpper = new JFormattedTextField(formatting);
		imaginaryAxisUpper.setHorizontalAlignment(JTextField.CENTER);
		//limits the field to decimal characters only
		imaginaryAxisUpper.addKeyListener(decimalOnly);
		
		JButton change = new JButton("Update");
		reset = new JButton("Reset");
		
		final JTextField iterations = new JTextField();
		//limits the field to numbers only
		iterations.addKeyListener(numbersOnly);
		
		
		panel.addMouseListener(new MouseAdapter(){
			/* 
			 * Displays a Julia set for the complex number clicked on
			 * by the user, shown in a separate window.
			 */
			@Override
			public void mouseClicked(MouseEvent e){
				selectedPoint = panel.calculatePoint(e.getX(), e.getY());
				juliaSet.setTitle("Julia Set for " + selectedPoint.toString());
				if(julia == null){
					julia = new JuliaPanel(selectedPoint, panel.getFractalType());
					juliaSet.setContentPane(julia);
				}else{
					julia.setC(selectedPoint);
					julia.repaint();
				}
				if(dimensions.getWidth()>=1366){
					juliaSet.setBounds(getX()+getWidth()+10, getY(), 500, 500);
				}else{
					juliaSet.setSize(500, 500);
				}
				juliaSet.setVisible(true);
				
				
			}
		});
		
		change.addMouseListener(new MouseAdapter(){
			/* 
			 * Changes the max and min x and y values on the display and the max number of
			 * iterations to try.
			 */
			@Override
			public void mouseClicked(MouseEvent arg0) {
				panel.updatePlane(Double.valueOf(realAxisLower.getText()).doubleValue(), Double.valueOf(realAxisUpper.getText()).doubleValue(), Double.valueOf(imaginaryAxisLower.getText()).doubleValue(), Double.valueOf(imaginaryAxisUpper.getText()).doubleValue());
				panel.changeIterations(Integer.valueOf(iterations.getText()));
			}
		});
		
		reset.addActionListener(new ActionListener(){

			/* 
			 * Resets everything back to default
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				panel.updatePlane(-2, 2, -1.6, 1.6);
				panel.changeIterations(100);
				realAxisLower.setValue(-2);
				realAxisUpper.setValue(2);
				imaginaryAxisLower.setValue(-1.6);
				imaginaryAxisUpper.setValue(1.6);
				iterations.setText("100");
			}
			
		});
		
		//Formats the layout of all the buttons and labels in the lower panel.
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx=0.1;
		c.gridwidth = 1;
		c.gridy = 0;
		
		c.gridx = 0;
		upperLabelSection.add(realAxisLbl, c);
		
		c.gridx = 1;
		upperLabelSection.add(realAxisLower, c);
		
		c.gridx = 2;
		upperLabelSection.add(to, c);
		
		c.gridx = 3;
		upperLabelSection.add(realAxisUpper, c);

		c.gridx = 4;
		upperLabelSection.add(imaginaryAxisLbl, c);

		c.gridx = 5;
		upperLabelSection.add(imaginaryAxisLower, c);

		c.gridx = 6;
		upperLabelSection.add(to2, c);

		c.gridx = 7;
		upperLabelSection.add(imaginaryAxisUpper, c);

		c.gridx = 8;
		upperLabelSection.add(iterationsLbl, c);

		c.gridx = 9;
		upperLabelSection.add(iterations, c);
		
		lowerLabelSection.add(change);
		lowerLabelSection.add(reset);
		
		
		//sets the upper and lower limits of what is seen on screen
		realAxisLower.setValue(-2);
		realAxisUpper.setValue(2);
		imaginaryAxisLower.setValue(-1.6);
		imaginaryAxisUpper.setValue(1.6);
		iterations.setText("100");		
		this.setVisible(true);
	}
	
	/**
	 * Limits the characters which can be entered to only digits, minus sign and a decimal point.
	 * 
	 * @author Daniel
	 * 
	 */
	class decimalOnly extends KeyAdapter{
		/**
		 * If the character entered is not a digit, decimal point, or minus sign
		 * it is ignored.
		 * 
		 * @see java.awt.event.KeyAdapter#keyTyped(java.awt.event.KeyEvent)
		 */
		public void keyTyped(KeyEvent e) {
			char c = e.getKeyChar();
			if (!Character.isDigit(c) && !(c == '-') && !(c == '.'))
				e.consume();
		}
	}
	
	/**
	 * Limits the characters which can be entered to only digits.
	 * 
	 * @author Daniel
	 *
	 */
	class numbersOnly extends KeyAdapter{
		/**
		 * If the character entered is not a digit it is ignored.
		 * 
		 * @see java.awt.event.KeyAdapter#keyTyped(java.awt.event.KeyEvent)
		 */
		public void keyTyped(KeyEvent e) {
			char c = e.getKeyChar();
			if (!Character.isDigit(c))
				e.consume();
		}
	}
	/**
	 * Holds a list containing all the complex numbers the user
	 * has added to as their favourites. Also allows these favourites
	 * to be viewed.
	 * 
	 * @author Daniel
	 *
	 */
	class Favourites extends JPanel implements ListSelectionListener{

		/**
		 * The list holding the favourite complex numbers.
		 */
		private JList<Complex> list;
	    private DefaultListModel<Complex> favourite;
		
		/**
		 * Creates the panel and adds a list to hold all the favourites.
		 * Also creates a listener to allow favourites to be viewed.
		 * 
		 * @param julia The JFrame where favourites are to be displayed when
		 * 		  selected.
		 */
		public Favourites(){
			super(new BorderLayout());
			favourite = new DefaultListModel<Complex>();
			
			//Create the list and put it in a scroll pane.
	        list = new JList<Complex>(favourite);
	        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	        list.setSelectedIndex(0);
	        list.addListSelectionListener(this);
	        list.setVisibleRowCount(5);
	        JScrollPane listScrollPane = new JScrollPane(list);
	        
	        add(listScrollPane, BorderLayout.CENTER);
	        
	        list.addMouseListener(new MouseAdapter(){

				/* 
				 * Shows the julia set when a favourite complex number is clicked.
				 */
				@Override
				public void mouseClicked(MouseEvent arg0) {
					if(!favourite.isEmpty()){
						int index = list.getSelectedIndex();
						Complex constant = (Complex) favourite.get(index);
						juliaSet.setTitle("Julia Set for " + constant.toString());
						if(julia == null){
							julia = new JuliaPanel(constant, panel.getFractalType());
							juliaSet.setContentPane(julia);
							
						}else{
							julia.setC(constant);
							julia.repaint();
						}
						if(dimensions.getWidth()>=1366){
							juliaSet.setBounds(fractalFrame.getX()+fractalFrame.getWidth()+10, fractalFrame.getY(), 500, 500);
						}else{
							juliaSet.setSize(500, 500);
						}
						juliaSet.setVisible(true);
						
					}
					
				}
	        	
	        });
		}
		
		/**
		 * Adds a complex number to the favourites if it
		 * isn't already a favourite.
		 * 
		 * @param c The complex number to be added.
		 */
		public void addFavourite(Complex c){
			if(!exists(c)){
				favourite.addElement(c);
			}
		}
		
		/**
		 * Checks if a given complex number is already a favourite.
		 * 
		 * @param c The given complex number to check.
		 * @return True if the number is already a favourite.
		 */
		public boolean exists(Complex c){
			return favourite.contains(c);
		}

		@Override
		public void valueChanged(ListSelectionEvent arg0) {}
		
		
	}
	/**
	 * Contains all the items and their associated listeners for the menu bar.
	 * 
	 * @author Daniel
	 *
	 */
	class MyMenu extends JMenuBar {

		/**
		 * The Menu Bar itself.
		 */
		JMenu menu;
		/**
		 * Menu containing options for number of threads being used.
		 */
		JMenu threading;
		/**
		 * A checkbox to select whether the favourites panel is shown.
		 */
		JCheckBoxMenuItem favouritesShow;
		/**
		 * A checkbox to select whether the size and iteration options are shown.
		 */
		JCheckBoxMenuItem sizeShow;
		/**
		 * A checkbox to select whether the julia panel autoupdates as the
		 * mouse moves.
		 */
		JCheckBoxMenuItem autoJulia;
		/**
		 * A checkbox to select whether zooming in is animated or not.
		 */
		JCheckBoxMenuItem zoomAni;
		/**
		 * Radiobutton to select mandelbrot set to be drawn.
		 */
		JRadioButtonMenuItem mandelbrot;
		/**
		 * Radiobutton to select burning ship set to be drawn.
		 */
		JRadioButtonMenuItem burningShip;
		/**
		 * Radiobutton to select tricorn set to be drawn.
		 */
		JRadioButtonMenuItem triCorn;
		/**
		 * A submenu containing the options for multibrot set.
		 */
		JMenu subMenu;
		/**
		 * Radiobutton to select multibrot set with a power of 3 to be drawn.
		 */
		JRadioButtonMenuItem multi3;
		/**
		 * Radiobutton to select multibrot set with a power of 4 to be drawn.
		 */
		JRadioButtonMenuItem multi4;
		/**
		 * Radiobutton to select multibrot set with a power of 5 to be drawn.
		 */
		JRadioButtonMenuItem multi5;
		/**
		 * Radiobutton to select multibrot set with a power of 6 to be drawn.
		 */
		JRadioButtonMenuItem multi6;
		/**
		 * Radiobutton to select to use one thread only.
		 */
		JRadioButtonMenuItem singleThread;
		/**
		 * Radiobutton to select to use multiple threads.
		 */
		JRadioButtonMenuItem multiThread;
		
		/**
		 * Initialises the menu bar. Creates all the subsections and adds all the
		 * listeners to the correct parts.
		 */
		public void init(){
			menu = new JMenu("Options");
			this.add(menu);
			
			threading = new JMenu("Thread Options");
			this.add(threading);
			
			//Options to change the number of threads being used to render the fractal and julia set.
			ButtonGroup numThreads = new ButtonGroup();
			singleThread = new JRadioButtonMenuItem("Single");
			multiThread = new JRadioButtonMenuItem("Multi");
			numThreads.add(singleThread);
			numThreads.add(multiThread);
			threading.add(singleThread);
			threading.add(multiThread);
			multiThread.setSelected(true);
			
			singleThread.addItemListener(new ItemListener(){

				/* 
				 * If selected, sets the main fractal and julia set, if being viewed, to use only
				 * one thread.
				 */
				@Override
				public void itemStateChanged(ItemEvent arg0) {
					if(singleThread.isSelected()){
						panel.setThread(0);
						if(julia !=null){
							julia.setThread(0);
						}
					}
				}
				
			});
			
			multiThread.addItemListener(new ItemListener(){

				/* 
				 * If selected, sets the main fractal and julia set, if being viewed, to use 
				 * multiple threaded when rendering.
				 */
				@Override
				public void itemStateChanged(ItemEvent e) {
					if(multiThread.isSelected()){
						panel.setThread(1);
						if(julia !=null){
							julia.setThread(1);
						}
					}
					
				}
				
			});
			
			
			//options to show favourites list
			favouritesShow = new JCheckBoxMenuItem("Show Favourites?");
			favouritesShow.setMnemonic(KeyEvent.VK_F);
			favouritesShow.setSelected(true);
			menu.add(favouritesShow);
			
			favouritesShow.addItemListener(new ItemListener(){

				/* 
				 * If selected shows the favourites panel, otherwise hides it
				 */
				@Override
				public void itemStateChanged(ItemEvent e) {
					if(favouritesShow.isSelected()){
						favourites.setVisible(true);
					}else{
						favourites.setVisible(false);
					}
					
				}
				
			});
			
			//options to show size and iterations settings
			sizeShow = new JCheckBoxMenuItem("Show Range Settings?");
			sizeShow.setMnemonic(KeyEvent.VK_R);
			sizeShow.setSelected(true);
			menu.add(sizeShow);
			
			sizeShow.addItemListener(new ItemListener(){

				/* 
				 * If selected shows the bottom panel, otherwise hides it.
				 */
				@Override
				public void itemStateChanged(ItemEvent arg0) {
					if(sizeShow.isSelected()){
						labelSection.setVisible(true);
					}else{
						labelSection.setVisible(false);
					}
					
				}
				
			});
			
			//option to autoupdate julia set display as mouse moves
			autoJulia = new JCheckBoxMenuItem("Auto Update Julia Sets?");
			autoJulia.setMnemonic(KeyEvent.VK_J);
			menu.add(autoJulia);
			autoJulia.addItemListener(new ItemListener(){

				/*
				 * If selected allows the julia panel to update as the mouse moves.
				 */
				@Override
				public void itemStateChanged(ItemEvent e) {
					if(autoJulia.isSelected()){
						autoUpdate = true;
					}else{
						autoUpdate = false;
					}
					
				}
			});
			
			//option to enable or disable zoom animations
			zoomAni = new JCheckBoxMenuItem("Show zoom animation?");
			zoomAni.setMnemonic(KeyEvent.VK_Z);
			menu.add(zoomAni);
			zoomAni.setSelected(true);
			
			zoomAni.addItemListener(new ItemListener(){

				/* 
				 * If selected shows the animation when zooming, otherwise disables it.
				 */
				@Override
				public void itemStateChanged(ItemEvent arg0) {
					if(zoomAni.isSelected()){
						panel.setAnimate(true);
					}else{
						panel.setAnimate(false);
					}
					
				}
				
			});
			
			//options to change the type of set being drawn
			menu.addSeparator();
			ButtonGroup fractalType = new ButtonGroup();
			mandelbrot = new JRadioButtonMenuItem("Mandelbrot Set");
			mandelbrot.setSelected(true);
			burningShip = new JRadioButtonMenuItem("Burning Ship Set");
			triCorn = new JRadioButtonMenuItem("TriCorn Set");
			fractalType.add(mandelbrot);
			fractalType.add(burningShip);
			fractalType.add(triCorn);
			menu.add(mandelbrot);
			menu.add(burningShip);
			menu.add(triCorn);
			
			mandelbrot.addItemListener(new ItemListener(){

				/*
				 * If selected changes the set being drawn to mandelbrot set and changes the julia set to use
				 * the mandelbrot formula.
				 */
				@Override
				public void itemStateChanged(ItemEvent arg0) {
					if(mandelbrot.isSelected()){
						reset.doClick();
						panel.setFractalType(0);
						if(julia !=null){
							julia.setFractalType(0);
							julia.repaint();
						}
						panel.repaint();
					}
				}
				
			});
			
			burningShip.addItemListener(new ItemListener(){

				/*
				 * If selected changes the set being drawn to burning ship set and changes the julia set to use
				 * the burning ship formula.
				 */
				@Override
				public void itemStateChanged(ItemEvent e) {
					if(burningShip.isSelected()){
						reset.doClick();
						panel.setFractalType(1);
						if(julia !=null){
							julia.setFractalType(1);
							julia.repaint();
						}
						panel.repaint();
						
					}
					
				}
				
			});
			
			triCorn.addItemListener(new ItemListener(){

				/*
				 * If selected changes the set being drawn to tricorn set and changes the julia set to use
				 * the tricorn formula.
				 */
				@Override
				public void itemStateChanged(ItemEvent e) {
					if(triCorn.isSelected()){
						reset.doClick();
						panel.setFractalType(2);
						if(julia !=null){
							julia.setFractalType(2);
							julia.repaint();
						}
						panel.repaint();
					}
					
				}
				
			});
			
			//submenu for multibrot set options
			subMenu = new JMenu("Multibrot Set (z^d +c)");
			menu.add(subMenu);
			
			multi3 = new JRadioButtonMenuItem("d=3");
			subMenu.add(multi3);
			fractalType.add(multi3);
			
			multi3.addItemListener(new ItemListener(){

				/*
				 * If selected changes the set being drawn to multibrot set with power 3 and changes the julia set 
				 * to use appropriate formula.
				 */
				@Override
				public void itemStateChanged(ItemEvent e) {
					if(multi3.isSelected()){
						reset.doClick();
						panel.setFractalType(3);
						if(julia !=null){
							julia.setFractalType(3);
							julia.repaint();
						}
						panel.repaint();
					}
					
				}
				
			});
			
			multi4 = new JRadioButtonMenuItem("d=4");
			subMenu.add(multi4);
			fractalType.add(multi4);
			
			multi4.addItemListener(new ItemListener(){

				/*
				 * If selected changes the set being drawn to multibrot set with power 4 and changes the julia set 
				 * to use appropriate formula.
				 */
				@Override
				public void itemStateChanged(ItemEvent e) {
					if(multi4.isSelected()){
						reset.doClick();
						panel.setFractalType(4);
						if(julia !=null){
							julia.setFractalType(4);
							julia.repaint();
						}
						panel.repaint();
						
					}
					
				}
				
			});
			
			multi5 = new JRadioButtonMenuItem("d=5");
			subMenu.add(multi5);
			fractalType.add(multi5);
			
			multi5.addItemListener(new ItemListener(){

				/*
				 * If selected changes the set being drawn to multibrot set with power 5 and changes the julia set 
				 * to use appropriate formula.
				 */
				@Override
				public void itemStateChanged(ItemEvent e) {
					if(multi5.isSelected()){
						reset.doClick();
						panel.setFractalType(5);
						if(julia !=null){
							julia.setFractalType(5);
							julia.repaint();
						}
						panel.repaint();
						
					}
					
				}
				
			});
			
			multi6 = new JRadioButtonMenuItem("d=6");
			subMenu.add(multi6);
			fractalType.add(multi6);
			
			multi6.addItemListener(new ItemListener(){

				/*
				 * If selected changes the set being drawn to multibrot set with power 6 and changes the julia set 
				 * to use appropriate formula.
				 */
				@Override
				public void itemStateChanged(ItemEvent e) {
					if(multi6.isSelected()){
						reset.doClick();
						panel.setFractalType(6);
						if(julia !=null){
							julia.setFractalType(6);
							julia.repaint();
						}
						panel.repaint();
					}
					
				}
				
			});
			
			menu.addSeparator();
			
			/*
			 * options for saving both julia set and main fractal as either
			 * a jpg image or a png image.
			 */
			
			JMenu saveFractal = new JMenu("Save Fractal");
			JMenu saveJulia = new JMenu("Save Julia Set");
			menu.add(saveFractal);
			menu.add(saveJulia);
			
			JMenuItem saveFractalPNG = new JMenuItem("PNG");
			JMenuItem saveFractalJPG = new JMenuItem("JPG");
			JMenuItem saveJuliaPNG = new JMenuItem("PNG");
			JMenuItem saveJuliaJPG = new JMenuItem("JPG");
			
			saveFractal.add(saveFractalPNG);
			saveFractal.add(saveFractalJPG);
			
			saveJulia.add(saveJuliaPNG);
			saveJulia.add(saveJuliaJPG);
			
			saveFractalPNG.addActionListener(new ActionListener(){

				@SuppressWarnings("rawtypes")
				@Override
				public void actionPerformed(ActionEvent arg0) {
					/**
					 * Saves the current buffered fractal image as a png image file.
					 */
					new SwingWorker(){

						@Override
						protected Object doInBackground() throws Exception {
							BufferedImage fractal = panel.getFractal();
							if(fractal !=null){
								try {
								    File file = new File("fractal.png");
								    ImageIO.write(fractal, "png", file);
								} catch (IOException e) {
									//Shows a pop-up error message
									JOptionPane.showMessageDialog(fractalFrame,"Error creating image!");
								}
							}
							return null;
						}
						
					}.execute();					
				}
			});	
			
			saveFractalJPG.addActionListener(new ActionListener(){

				@SuppressWarnings("rawtypes")
				@Override
				public void actionPerformed(ActionEvent arg0) {
					
					/*
					 * Saves the current fractal buffered image as a jpg image file.
					 */
					new SwingWorker(){

						@Override
						protected Object doInBackground() throws Exception {
							BufferedImage fractal = panel.getFractal();
							if(fractal !=null){
								try {
								    File file = new File("fractal.jpg");
								    ImageIO.write(fractal, "jpg", file);
								} catch (IOException e) {
									//Shows a pop-up error message
									JOptionPane.showMessageDialog(fractalFrame,"Error creating image!");
								}
							}
							return null;
						}
						
					}.execute();
				}
			});	
			
			saveJuliaPNG.addActionListener(new ActionListener(){

				@SuppressWarnings("rawtypes")
				@Override
				public void actionPerformed(ActionEvent arg0) {
					/*
					 * Saves the current buffered julia set image as a png image file.
					 */
					new SwingWorker(){

						@Override
						protected Object doInBackground() throws Exception {
							if(julia != null){
								BufferedImage juliaSet = julia.getJulia();
								try {
								    File file = new File("julia.png");
								    ImageIO.write(juliaSet, "png", file);
								} catch (IOException e) {
									//Shows a pop-up error message
									JOptionPane.showMessageDialog(fractalFrame,"Error creating image!");
								}
							}
							return null;
						}
						
					}.execute();
				}
			});	
			
			saveJuliaJPG.addActionListener(new ActionListener(){

				@SuppressWarnings("rawtypes")
				@Override
				public void actionPerformed(ActionEvent arg0) {
					/*
					 * Saves the current julia set buffered image as a jpg image file.
					 */
					new SwingWorker(){

						@Override
						protected Object doInBackground() throws Exception {
							if(julia != null){
								BufferedImage juliaSet = julia.getJulia();
							try {
								    File file = new File("julia.jpg");
								    ImageIO.write(juliaSet, "jpg", file);
								} catch (IOException e) {
									//Shows a pop-up error message
									JOptionPane.showMessageDialog(fractalFrame,"Error creating image!");
								}
							}
							return null;
						}
						
					}.execute();
				}
			});	
			
		}
		
		
	}

}
