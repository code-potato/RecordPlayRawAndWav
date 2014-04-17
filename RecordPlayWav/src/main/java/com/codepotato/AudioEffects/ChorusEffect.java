package com.codepotato.AudioEffects;

/**
 * Created by michael on 4/17/14.
 */
public class ChorusEffect extends TimeBasedEffect
{
    private SinWave sin;
    private double minDelay; // ms
    private double depth; // 0-30 ms
    private double rate; // 0-20 hz

    public ChorusEffect()
    {
        rate = 2.;
        depth = 30.;
        minDelay = 30.;

        delayTime = minDelay + depth;
        wetGain = .5;
        dryGain = .7;
        feedbackGain = 0.4;
        delaySamples = convertMilliSecsToSamples(delayTime);

        delay = new Delay(2 * delaySamples); //delay buffer is twice delay time
        delay.setDelayAmt(delaySamples);
        delay.setDryGain(dryGain);
        delay.setWetGain(wetGain);
        delay.setFeedbackGain(feedbackGain);

        sin = new SinWave(rate, Math.PI/2., sampleRate);
    }

    @Override
    public double tick(double inputSample) {
        double tempDelay = depth/2*(sin.tick()+1)+minDelay;
        tempDelay = convertMilliSecsToSamples(tempDelay);
        delay.setDelayLineDelay(tempDelay);
        return delay.tick(inputSample);
    }

}
