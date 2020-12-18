package Game;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.Map;
import java.util.Set;

public class Player {

    int playerID;

    int x = 0;
    int y = 0;

    int r = 20;

    int speed = 5;
    public static int xoff = 130, yoff = 20;
    public Player(int id_){
        playerID = id_;
    }

    public void paint(Graphics2D g2d) {

        Ellipse2D shape = new Ellipse2D.Double(x,y,r,r);
        g2d.setColor(Color.red);
        g2d.fill(shape);

    }


    Map<Integer, Runnable> key_map = Map.ofEntries(
            Map.entry(87, () -> move_forward()),
            Map.entry(83, () -> move_backward()),
            Map.entry(68, () -> move_right()),
            Map.entry(65, () -> move_left())

    );

    public void digest_keys(Set<Integer> pressed_keys){

        for (int code: pressed_keys){
            key_map.get(code).run();
        }

    }


    public void move_forward(){
        y -= speed;
    }

    public void move_backward(){
        y += speed;
    }

    public void move_left(){
        x -= speed;
    }

    public void move_right(){
        x += speed;
    }

    public Rectangle get_bounds(){
        return new Rectangle(x, y, r, r);
    }

    public JSONObject getJSON(){

        JSONObject object = new JSONObject();
        object.put("playerID", playerID);
        object.put("xPos", x);
        object.put("yPos", y);
        return object;
    }

}