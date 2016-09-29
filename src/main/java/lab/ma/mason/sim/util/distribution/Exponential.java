/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */
package lab.ma.mason.sim.util.distribution;
import lab.ma.mason.ec.util.MersenneTwisterFast;

/**
 * Exponential Distribution (aka Negative Exponential Distribution); See the <A HREF="http://www.cern.ch/RD11/rkb/AN16pp/node78.html#SECTION000780000000000000000"> math definition</A>
 * <A HREF="http://www.statsoft.com/textbook/glose.html#Exponential Distribution"> animated definition</A>.
 * <p>
 * <tt>p(x) = lambda*exp(-x*lambda)</tt> for <tt>x &gt;= 0</tt>, <tt>lambda &gt; 0</tt>.
 * <p>
 * Instance methods operate on a user supplied uniform random number generator; they are unsynchronized.
 * <dt>
 * Static methods operate on a default uniform random number generator; they are synchronized.
 * <p>
 *
 * @author wolfgang.hoschek@cern.ch
 * @version 1.0, 09/24/99
 */
public class Exponential extends AbstractContinousDistribution { 
    private static final long serialVersionUID = 1;

    protected double lambda;

/**
 * Constructs a Negative Exponential distribution.
 */
    public Exponential(double lambda, MersenneTwisterFast randomGenerator) {
        setRandomGenerator(randomGenerator);
        setState(lambda);
        }
/**
 * Returns the cumulative distribution function.
 */
    public double cdf(double x) {
        if (x <= 0.0) return 0.0;
        return 1.0 - Math.exp(-x * lambda);
        }
/**
 * Returns a random number from the distribution.
 */
    public double nextDouble() {
        return nextDouble(lambda);
        }
/**
 * Returns a random number from the distribution; bypasses the internal state.
 */
    public double nextDouble(double lambda) {
        return - Math.log(randomGenerator.nextDouble()) / lambda;
        }
/**
 * Returns the probability distribution function.
 */
    public double pdf(double x) {
        if (x < 0.0) return 0.0;
        return lambda*Math.exp(-x*lambda);
        }
/**
 * Sets the mean.
 */
    public void setState(double lambda) {
        this.lambda = lambda;
        }

/**
 * Returns a String representation of the receiver.
 */
    public String toString() {
        return this.getClass().getName()+"("+lambda+")";
        }
    }
