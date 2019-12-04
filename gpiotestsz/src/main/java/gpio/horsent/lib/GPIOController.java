package gpio.horsent.lib;

/**
 * Created by zhangyazhou on 2019/4/9.
 */

public class GPIOController {
    public static native int setGpioState(int gpioIndex, int value);
    public static native String getGpioState(int gpioIndex);
}