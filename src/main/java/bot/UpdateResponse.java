package bot;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class UpdateResponse {
    public boolean ok;
    public ArrayList<Result> result;

    public static class Chat{
        public int id;
        public String first_name;
        public String type;
    }

    public static class Entity{
        public int offset;
        public int length;
        public String type;
    }

    public static class From{
        public int id;
        public boolean is_bot;
        public String first_name;
        public String language_code;
    }

    public static class Message{
        public int message_id;
        public From from;

        public Chat chat;
        public int date;
        public String text;
        public ArrayList<Entity> entities;
    }

    public static class Result{
        public int update_id;
        public Message message;
    }





}
