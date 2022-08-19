package bot;

import java.util.ArrayList;

public class GoogleResponse {

    public Data data;

    public static class Data {
        public ArrayList<Translation> translations;
    }

    public static class Translation {
        public String translatedText;
        public String detectedSourceLanguage;
    }
}
