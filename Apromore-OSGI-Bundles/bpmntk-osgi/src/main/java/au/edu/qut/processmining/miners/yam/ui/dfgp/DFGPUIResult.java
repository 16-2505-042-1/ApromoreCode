package au.edu.qut.processmining.miners.yam.ui.dfgp;

/**
 * Created by Adriano on 23/01/2017.
 */
public class DFGPUIResult {

    public static final double FREQUENCY_THRESHOLD = 1.0;
    public static final double PARALLELISMS_THRESHOLD = 0.10;

    private double frequencyThreshold;
    private double parallelismsThreshold;

    public DFGPUIResult() {
        frequencyThreshold = FREQUENCY_THRESHOLD;
        parallelismsThreshold = PARALLELISMS_THRESHOLD;
    }

    public double getFrequencyThreshold() {
        return frequencyThreshold;
    }
    public void setFrequencyThreshold(double frequencyThreshold) {
        this.frequencyThreshold = frequencyThreshold;
    }

    public double getParallelismsThreshold() {
        return parallelismsThreshold;
    }
    public void setParallelismsThreshold(double parallelismsThreshold) {
        this.parallelismsThreshold = parallelismsThreshold;
    }
}
