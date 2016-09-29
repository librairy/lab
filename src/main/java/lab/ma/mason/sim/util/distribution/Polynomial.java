/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */
package lab.ma.mason.sim.util.distribution;

/**
 * Polynomial functions.
 */
public class Polynomial extends Constants {
    private static final long serialVersionUID = 1;

/**
 * Makes this class non instantiable, but still let's others inherit from it.
 */
    protected Polynomial() {}
/**
 * Evaluates the given polynomial of degree <tt>N</tt> at <tt>x</tt>, assuming coefficient of N is 1.0.
 * Otherwise same as <tt>polevl()</tt>.
 * <pre>
 *                     2          N
 * y  =  C  + C x + C x  +...+ C x
 *        0    1     2          N
 *
 * where C  = 1 and hence is omitted from the array.
 *        N
 *
 * Coefficients are stored in reverse order:
 *
 * coef[0] = C  , ..., coef[N-1] = C  .
 *            N-1                   0
 *
 * Calling arguments are otherwise the same as polevl().
 * </pre>
 * In the interest of speed, there are no checks for out of bounds arithmetic.
 *
 * @param x argument to the polynomial.
 * @param coef the coefficients of the polynomial.
 * @param N the degree of the polynomial.
 */
    public static double p1evl( double x, double coef[], int N ) throws ArithmeticException {
        double ans;

        ans = x + coef[0];

        for(int i=1; i<N; i++) { ans = ans*x+coef[i]; }

        return ans;
        }
/**
 * Evaluates the given polynomial of degree <tt>N</tt> at <tt>x</tt>.
 * <pre>
 *                     2          N
 * y  =  C  + C x + C x  +...+ C x
 *        0    1     2          N
 *
 * Coefficients are stored in reverse order:
 *
 * coef[0] = C  , ..., coef[N] = C  .
 *            N                   0
 * </pre>
 * In the interest of speed, there are no checks for out of bounds arithmetic.
 *
 * @param x argument to the polynomial.
 * @param coef the coefficients of the polynomial.
 * @param N the degree of the polynomial.
 */
    public static double polevl( double x, double coef[], int N ) throws ArithmeticException {
        double ans;
        ans = coef[0];

        for(int i=1; i<=N; i++) ans = ans*x+coef[i];

        return ans;
        }
    }
