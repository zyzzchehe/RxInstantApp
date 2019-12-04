package gpio.horsent.lib;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;
import androidx.annotation.Nullable;
import android.view.View;

/**
 * Created by zhangyazhou on 2019/4/9.
 */

public class MainActivity extends Activity {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.activity_main);
    }

    public void setClick(View view) {

    }

    public void getClick(View view) {
    }
}
