
/**
 * A Complex number, made up of a real and an imaginary part. Allows basic operations
 * to be carried out on itself, such as adding or squaring. 
 * 
 * @author Daniel
 *
 */
public class Complex {

	/**
	 * The real part of the complex number.
	 */
	private double real;
	/**
	 * The imaginary part of the complex number.
	 */
	private double imaginary;
	
	/**
	 * Creates a complex number with given numbers for the real and imaginary parts.
	 * 
	 * @param real Given real number.
	 * @param imaginary Given imaginary number.
	 */
	public Complex(double real, double imaginary){
		this.real = real;
		this.imaginary = imaginary;
	}
	
	/**
	 * Creates a default Complex number of 0+0i.
	 */
	public Complex(){
		this(0, 0);
	}
	
	/**
	 * Returns the real part of the complex number.
	 * 
	 * @return The real number part.
	 */
	public double getReal(){
		return real;
	}
	
	/**
	 * Returns the imaginary part of the complex number.
	 * 
	 * @return The imaginary part.
	 */
	public double getImaginary(){
		return imaginary;
	}
	
	/**
	 * Squares the current complex number.
	 * 
	 * @return The squared result.
	 */
	public Complex square(){
		return new Complex((real*real) - (imaginary*imaginary), (real*imaginary) + (real*imaginary));
	}
	
	/**
	 * Squares the modulus of the current complex number.
	 * 
	 * @return The square of the modulus.
	 */
	public double modulusSquared(){
		return (real * real) + (imaginary * imaginary);
	}
	
	/**
	 * Adds the given complex number to the current number, returning
	 * the result.
	 * 
	 * @param d The given complex number to add on
	 * @return The resulting complex number
	 */
	public Complex add(Complex d){
		return new Complex(real + d.getReal(), imaginary + d.getImaginary());
	}
	
	/** 
	 * Formats the complex number into a string in the form x+yi, where x and y are
	 * numbers.
	 * 
	 * @return The string representation
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return Double.toString(real) + " + " + Double.toString(imaginary) + "i";
	}
	
	/**
	 * Squares the current complex number but using the absolute values for the real
	 * and imaginary parts of the number.
	 * 
	 * @return The squared result.
	 */
	public Complex burningShip(){
		return new Complex(Math.abs(real), -1*Math.abs(imaginary)).square();
	}
	
	/**
	 * Squares the current complex number but using the triCorn formula, where all the 
	 * imaginary parts are multipled by -1.
	 * 
	 * @return The resulting complex number.
	 */
	public Complex triCorn(){
		return new Complex((real*real) - ((-1*imaginary)*(-1*imaginary)), (real*(-1*imaginary)) + (real*(-1*imaginary)));
	}
	
	/**
	 * Multiplies the current complex number by the given complex number and returns
	 * the results.
	 * 
	 * @param m The given complex number to be multiplied by.
	 * @return The resulting complex number.
	 */
	public Complex multiply(Complex m){
		return new Complex((real*m.getReal()) - (imaginary*m.getImaginary()), (real*m.getImaginary()) + (m.getReal()*imaginary));
	}
	
}
