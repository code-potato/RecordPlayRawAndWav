package com.codepotato.AudioEffects;

/**
 * Created by michael on 4/12/14.
 */
abstract public class TimeBasedEffect extends Effect
{
    protected Delay delay;
    protected double delayTime; // in milliseconds

    protected int delaySamples;
    protected double wetGain;
    protected double dryGain;
    protected double feedbackGain;

    public double getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(double delayTime) {
        this.delayTime = delayTime;
        delaySamples = convertMilliSecsToSamples(this.delayTime);
        delay.setDelayAmt(delaySamples);
    }

    public double getWetGain() {
        return wetGain;
    }

    public void setWetGain(double wetGain) {
        this.wetGain = wetGain;
        delay.setWetGain(this.wetGain);
    }

    public double getDryGain() {
        return dryGain;
    }

    public void setDryGain(double dryGain) {
        this.dryGain = dryGain;
        delay.setDryGain(this.dryGain);
    }

    public double getFeedbackGain() {
        return feedbackGain;
    }

    public void setFeedbackGain(double feedbackGain) {
        this.feedbackGain = feedbackGain;
        delay.setFeedbackGain(this.feedbackGain);
    }

    protected int convertMilliSecsToSamples(double milliSecs)
    {
        return (int) (milliSecs * sampleRate / 1000);
    }
}
