//This is the java file that encapsulates the Forest Environment and its behaviour

// Do NOT change this file - though you may read it to understand it.
//When marking, we will use an exact copy of this when running your agent.


import jason.asSyntax.*;
import jason.environment.Environment;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.Random;
import java.util.logging.Logger;

import java.util.Random;   

public class ForestEnv extends Environment {

    public static final int GSize = 8; // grid size
    public static final int GEM = 16; //gem code in grid model
    public static final int COIN = 32; //Coin code in grid model
    public static final int VASE = 64; //Vase code in grid model
	public static final int TELEPORTER = 128; //Code for Teleporter
	

    /*
    * LOOK HERE for all the percepts and actions you can use
	* (See also the executeAction() method and the updatePercepts() method)
    */
    public static final Term    ns = Literal.parseLiteral("next(slot)");

    public static final Term    pg = Literal.parseLiteral("pick(gem)");
    public static final Term    dg = Literal.parseLiteral("drop(gem)");
    public static final Term    sg = Literal.parseLiteral("stash(gem)");
    public static final Literal gh = Literal.parseLiteral("gem(hero)");
    public static final Literal gg = Literal.parseLiteral("gem(goblin)");


    public static final Term    pc = Literal.parseLiteral("pick(coin)");
    public static final Term    dc = Literal.parseLiteral("drop(coin)");
    public static final Term    sc = Literal.parseLiteral("stash(coin)");
    public static final Literal ch = Literal.parseLiteral("coin(hero)");
    public static final Literal cg = Literal.parseLiteral("coin(goblin)");

    public static final Term    pv = Literal.parseLiteral("pick(vase)");
    public static final Term    dv = Literal.parseLiteral("drop(vase)");
    public static final Term    sv = Literal.parseLiteral("stash(vase)");
    public static final Literal vh = Literal.parseLiteral("vase(hero)");
    public static final Literal vg = Literal.parseLiteral("vase(goblin)");
	
	public static final Literal hg = Literal.parseLiteral("hero(gem)");
	public static final Literal hc = Literal.parseLiteral("hero(coin)");
	public static final Literal hv = Literal.parseLiteral("hero(vase)");


    static Logger logger = Logger.getLogger(ForestEnv.class.getName());

    private ForestModel model;
    private ForestView  view;
    
    @Override
    public void init(String[] args) {
        model = new ForestModel();
        view  = new ForestView(model);
        model.setView(view);
        updatePercepts();
    }
    

    /*
    * LOOK HERE to see what the different actions do
	* See also the declaration of the literals and terms above
    */
    @Override
    public boolean executeAction(String ag, Structure action) {
        logger.info(ag+" doing: "+ action);
        try {
            if (action.equals(ns)) {
                model.nextSlot();
            } else if (action.getFunctor().equals("move_towards")) {
                int x = (int)((NumberTerm)action.getTerm(0)).solve();
                int y = (int)((NumberTerm)action.getTerm(1)).solve();
                model.moveTowards(x,y);
            } else if (action.equals(pg)) {
                model.pickGem();
            } else if (action.equals(dg)) {
                model.dropGem();
            } else if (action.equals(sg)) {
                model.stashGem();
            } else if (action.equals(pc)) {
                model.pickCoin();
            } else if (action.equals(dc)) {
                model.dropCoin();
            } else if (action.equals(sc)) {
                model.stashCoin();
            }else if (action.equals(pv)) {
                model.pickVase();
            } else if (action.equals(dv)) {
                model.dropVase();
            } else if (action.equals(sv)) {
                model.stashVase();
            }else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        updatePercepts();

        try {
            Thread.sleep(700);
        } catch (Exception e) {}
        informAgsEnvironmentChanged();
        return true;
    }
    
    /*
    * LOOK HERE to see how the percepts change in the environment
	* See the declaration of the literals and terma above
    */
    void updatePercepts() {
        clearPercepts();
        
        Location heroLoc = model.getAgPos(0);
        Location goblinLoc = model.getAgPos(1);
        
		
		
		if(model.hasObject(TELEPORTER, heroLoc)){
			Random rand = new Random(System.currentTimeMillis());
			int x = rand.nextInt(GSize);
			int y = rand.nextInt(GSize);
			System.out.println("~~~~~ TELEPORT ~~~~~> " + x + "," + y);
			model.setAgPos(0, x,y);
		}
		
		heroLoc = model.getAgPos(0);
		
        Literal pos1 = Literal.parseLiteral("pos(hero," + heroLoc.x + "," + heroLoc.y + ")");
        Literal pos2 = Literal.parseLiteral("pos(goblin," + goblinLoc.x + "," + goblinLoc.y + ")");

        addPercept(pos1);
        addPercept(pos2);
        
        if (model.hasObject(GEM, heroLoc)) {
            addPercept(gh);
        }
        if (model.hasObject(GEM, goblinLoc)) {
            addPercept(gg);
        }
        if (model.hasObject(COIN, heroLoc)) {
            addPercept(ch);
        }
        if (model.hasObject(COIN, goblinLoc)) {
            addPercept(cg);
        }
        if (model.hasObject(VASE, heroLoc)) {
            addPercept(vh);
        }
        if (model.hasObject(VASE, goblinLoc)) {
            addPercept(vg);
        }
		
		if(model.heroHasGem){
			addPercept(hg);
		}
		
		if(model.heroHasCoin){
			addPercept(hc);
		}
		if(model.heroHasVase){
			addPercept(hv);
		}
    }

    class ForestModel extends GridWorldModel {
        
        public boolean heroHasGem = false; // whether hero is carrying gem or not
        public boolean heroHasCoin = false; // whether hero is carrying coin or not
        public boolean heroHasVase = false; // whether hero is carrying Vase or not

		public boolean gobCoin = false;
		public boolean gobVase = false;
		public boolean gobGem = false;
		
        Random random = new Random(System.currentTimeMillis());

        private ForestModel() {
            super(GSize, GSize, 2);
            
            // initial location of agents
            try {
                setAgPos(0, 0, 0);
            
                Location goblinLoc = new Location(GSize/2, GSize/2);
                setAgPos(1, goblinLoc);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            //Objects are placed randomly in the forest
			Random rand = new Random(System.currentTimeMillis());
			for (int i = 0; i < GSize; i ++)
			{
				
				//On average, 1 item every 2 rows
				int t = rand.nextInt(3); 
				int x = rand.nextInt(GSize);
				int y = rand.nextInt(GSize);
				
				//Check that the random coordinates are OK
				if (isFree(x,y)){
					switch(t){
						case 0:
							add(GEM, x,y);
							break;
						case 1:
							add(VASE, x, y);
							break;
						case 2:
							add(COIN, x, y);
							break;
					}
				}
				
			}
			int x = rand.nextInt(GSize);
			int y = rand.nextInt(GSize);
			
			//Check random coordinates are OK for the teleporter
			if(isFree(x,y)){
				add(TELEPORTER, x, y);
				
			}
        }

		
		
        void nextSlot() throws Exception {
            Location heroLoc = getAgPos(0);
            heroLoc.x++;
            if (heroLoc.x == getWidth()) {
                heroLoc.x = 0;
                heroLoc.y++;
            }
            // finished searching the whole grid
            if (heroLoc.y == getHeight()) {
                return;
            }
            setAgPos(0, heroLoc);
            setAgPos(1, getAgPos(1)); // just to draw it in the view
        }
        
        void moveTowards(int x, int y) throws Exception {
            Location heroLoc = getAgPos(0);
            if (heroLoc.x < x)
                heroLoc.x++;
            else if (heroLoc.x > x)
                heroLoc.x--;
            if (heroLoc.y < y)
                heroLoc.y++;
            else if (heroLoc.y > y)
                heroLoc.y--;
            setAgPos(0, heroLoc);
            setAgPos(1, getAgPos(1)); // just to draw it in the view
        }
        
        void pickGem() {
            // Hero location has gem
            if (model.hasObject(GEM, getAgPos(0))) {
                remove(GEM, getAgPos(0));
                heroHasGem = true;
            }
        }

        void pickCoin() {
            // Hero location has coin
            if (model.hasObject(COIN, getAgPos(0))) {
                remove(COIN, getAgPos(0));
                heroHasCoin = true;
                
            }
        }

        void pickVase() {
            // Hero location has vase
            if (model.hasObject(VASE, getAgPos(0))) {
                remove(VASE, getAgPos(0));
                heroHasVase = true;
            }
        }

        void dropGem() throws Exception {
            //Hero drops gem
            if (heroHasGem) {
                heroHasGem = false;
                add(GEM, getAgPos(0));
				nextSlot();
            }
        }

        void dropCoin() throws Exception {
            //Hero drops coin
            if (heroHasCoin) {
                heroHasCoin = false;
                add(COIN, getAgPos(0));
				nextSlot();
            }
        }

        void dropVase() throws Exception{
            //Hero drops vase
            heroHasVase = false;
            add(VASE, getAgPos(0));
			nextSlot();
        }

        void stashGem() {
            // Goblin stashes gem
            if (model.hasObject(GEM, getAgPos(1))) {
                remove(GEM, getAgPos(1));
				gobGem = true;
            }
        }

        void stashCoin() {
            // Goblin stashes coin
            if (model.hasObject(COIN, getAgPos(1))) {
                remove(COIN, getAgPos(1));
				gobCoin = true;
            }
        }

        void stashVase() {
            // Goblin stashes vase
            if (model.hasObject(VASE, getAgPos(1))) {
                remove(VASE, getAgPos(1));
				gobVase = true;
            }
        }
    }
    
    class ForestView extends GridWorldView {

        public ForestView(ForestModel model) {
            super(model, "Goblin Forest", 600);
            defaultFont = new Font("Arial", Font.BOLD, 18); // change default font
            setVisible(true);
            repaint();
        }

        /** draw application objects */
        @Override
        public void draw(Graphics g, int x, int y, int object) {
            switch (object) {
                case ForestEnv.GEM: drawGem(g, x, y);  break;
                case ForestEnv.COIN: drawCoin(g, x, y);  break;
                case ForestEnv.VASE: drawVase(g, x, y);  break;
				case ForestEnv.TELEPORTER: drawTeleporter(g,x,y); break;
            }
        }

        @Override
        public void drawAgent(Graphics g, int x, int y, Color c, int id) {
            
            String label = "Goblin";
            c = Color.green;
            if (id == 0) {
                c = Color.yellow;
                label = "Hero\n";
                if (((ForestModel)model).heroHasGem) {
                    label += "G";
                }
                if (((ForestModel)model).heroHasCoin) {
                    label += "C";
                }
                if (((ForestModel)model).heroHasVase) {
                    label += "V";
                }
            }
		
            super.drawAgent(g, x, y, c, -1);
            g.setColor(Color.black);
            
            super.drawString(g, x, y, defaultFont, label);
        }

        public void drawGem(Graphics g, int x, int y) {
            super.drawObstacle(g, x, y);
            g.setColor(Color.green);
            drawString(g, x, y, defaultFont, "Gem");
        }

        public void drawCoin(Graphics g, int x, int y) {
            super.drawObstacle(g, x, y);
            g.setColor(Color.orange);
            drawString(g, x, y, defaultFont, "Coin");
        }

        public void drawVase(Graphics g, int x, int y) {
            super.drawObstacle(g, x, y);
            g.setColor(Color.red);
            drawString(g, x, y, defaultFont, "Vase");
        }
		
		 public void drawTeleporter(Graphics g, int x, int y) {
            super.drawObstacle(g, x, y);
            g.setColor(Color.white);
            drawString(g, x, y, defaultFont, "~~T~~");
        }

    }    
}
