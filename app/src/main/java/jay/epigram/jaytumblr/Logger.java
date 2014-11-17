package jay.epigram.jaytumblr;

import android.util.Log;

/**
 * Created by jay on 14. 11. 17..
 */
public class Logger {
    public String tag;

    public Logger(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void info(String log) {
        Log.i(tag, log);
    }
}
