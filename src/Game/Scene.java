package Game;

import Game.Entities.Player;
import Game.GUI.BasicElement;
import Game.Listeners.CoinCollectListener;
import Game.Multiplayer.Client;
import Game.Multiplayer.ClientListener;
import Game.Objects.Coin;
import Game.Sounds.Sound;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Scene{

    public int x;
    public int y;

    public ArrayList<Paintable> children = new ArrayList<>();
    public ArrayList<Paintable> childrenQueue = new ArrayList<>(); //children waiting for queue to finish to be added

    public Player player;
    public Paintable background;
    public ArrayList<BasicElement> guiElements = new ArrayList<>();


    //Listeners
    public CoinCollected coinCollected = new CoinCollected();


    private boolean DEBUG = false;
    public void setDEBUG(boolean DEBUG){
        this.DEBUG = DEBUG;
    }


    public Scene(){

    }


    public void add(Paintable o){
        o.setClient(client);
        children.add(o);
    } //adding children directly
    public void addToWaitingList(Paintable o){
        o.setClient(client);
        childrenQueue.add(o); } //adding children while still in game loop, avoid concurrent modification error

    //Specific elements
    public void addPlayer(Player p){
        p.setClient(client);
        player = p;
    }
    public void addBackground(Paintable o){
        background = o;
    }
    public void addGUIElement(BasicElement element){
        guiElements.add(element);
    }


    //Paint
    public void paint(Graphics2D g2d){

        //All paintables must be drawn in this order

        background.paint(g2d);

        for(Paintable child: children){
            child.paint(g2d);

            if (DEBUG){
                child.paintBox(g2d);
            }
        }

        player.paint(g2d);
        if (DEBUG){
            player.paintBox(g2d);
        }

        for(BasicElement element: guiElements){
            element.paint(g2d);

            if (DEBUG){
                element.paintBox(g2d);
            }
        }

    }


    //Periodically call to update scene
    public void purgeChildren(){
        children.removeIf(child -> child.toDelete);
    }
    public void addQueuedChildren(){
        children.addAll(childrenQueue);
        childrenQueue.clear();
    }



    //Multiplayer section
    private Client client;
    private Client.ClientUpdateListener clientUpdateListener;
    public void setClient(Client client) {
        this.client = client;
        this.clientUpdateListener = client.clientUpdateListener; //linking listener
    }





    //began to shift children to hashmap
    public JSONObject toJSON(){

        JSONObject scene = new JSONObject();

        scene.put("background", background.ID);
        scene.put("mainPlayer", player.toJSON());

        JSONObject childrenObject = new JSONObject();
        for(Paintable child: children){
            childrenObject.put(child.MULTIPLAYER_ID, child.toJSON());
        }

        /*
        JSONArray childrenArray = new JSONArray();
        for(Paintable child: children){
            childrenArray.add(child.toJSON());
        }
        */

        scene.put("children", childrenObject);

        return scene;
    }


    public Paintable getChildByMID(String MID){


        for(Paintable child: children){
            if(child.MULTIPLAYER_ID.equals(MID)){
                return child;
            }
        }

        return null;
    }

    public InitialSceneListener initialSceneListener = new InitialSceneListener();
    public class InitialSceneListener{
        public void receivedInput(String msg) {
            System.out.println(msg);
            fillSceneFromJSONString(msg);
        }
    }

    public void fillSceneFromJSONString(String msg){
        JSONObject object;
        try{
            JSONParser parser = new JSONParser();
            object = (JSONObject) parser.parse(msg);

            String background = (String) object.get("background");


            JSONObject children = (JSONObject) object.get("children");
            children.forEach((MID, child) -> {
                Paintable p = Game.getObject((JSONObject) child);
                add(p);
            });

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    public MessageListener messageListener = new MessageListener();
    public class MessageListener{
        public void receivedJSONInput(JSONObject object) {
            updateSceneFromJSON(object);
        }
    }

    public void updateSceneFromJSON(JSONObject object){
        System.out.println("object: "+object);

        String code = (String) object.get("stateCode");
        JSONObject paintableJSON = (JSONObject) object.get("object");
        String MID = (String) paintableJSON.get("M_ID");

        switch (code){
            case "CREATE":
                {
                    Paintable p = Game.getObject(paintableJSON);
                    add(p);
                }
                break;

            case "CHANGE":
                {
                    Paintable p = getChildByMID(MID);
                    if(p==null)break;
                    p.setFromJSON(paintableJSON);
                }
                break;
            case "DELETE":
                {
                    Paintable p = getChildByMID(MID);
                    if(p==null)break;
                    p.toDelete = true;
                }
                break;
            default:
                break;

        }
    }



    //Listeners
    Random rand = new Random();
    private class CoinCollected implements CoinCollectListener {
        @Override
        public void onCollectCoin(Coin coin) {
            //Adding a coin

            Sound.setPlaySound("\\src\\Game\\Resources\\Sounds\\Coin\\coin.wav");
            Coin c = new Coin(rand.nextInt(300), rand.nextInt(300));
            addToWaitingList(c);

            c.onCreate();
        }
    }

}
