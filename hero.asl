//NAME:Daniel Fox
//STUDENT NUMBER:201278002
//COMP310 Assignment 2 : The Quest

/* Initial beliefs and Rules */
//A rule to determine if the hero is "at" the location of another agent (the paremeter P)
//Satisfied is achieved when the agent holds all 3 coins which will signal to the agent to return to goblin 
at(P) :- pos(P,X,Y) & pos(hero,X,Y).
satisfied :- hero(coin) & hero(gem) & hero(vase).

/* Initial goal */
// Initial goal to achieve "started". This will get the agent doing something
!started.
!check(slots). //Initial the goal to move the agent
	
/* Plans */ 
//Started plan provided prints to terminal
//Check(slots) plan: We keep moving aslong as we dont sense any of the three items
//pos(hero,7,7) prevents the recursive call if the robot is at the end of the grid

+!started : true <- .print("You dont scare me, Goblin!").
	
	
+!check(slots) : not coin(hero) & not gem(hero) & not vase(hero) & not pos(hero,7,7)
	<- next(slot); 
	!check(slots).
+!check(slots).

//used to continue moving the agent if not satisfied
+!checkAgain(slots) : not satisfied 
						<- next(slot). 

/* belief event */
//atmoic is needed for this case or the hero agent will "bug" and get stuck moving from the goblin
//This ensures it is run to completion so that nothing else happens until we are back where we started

// If we sense an item , say so and ensure its picked up, if the agent doesnt beleif its holding that item
// If we sense that the agent belief is that its holding a coin, say it cant carry anymore
//and skip to the next position and dont pickup the item
//checkAgain(slots) used to continue moving the agent if not satisfied

@coinTake[atomic]
+coin(hero) : not hero(coin)
		<- .print("Coin!");
			!ensure_pickup(C).//achieve the goal and "call" it

+coin(hero) : hero (coin) 
			<- .print("I'm already carrying a coin");
			!ensure_pickup(C).
			
@gemTake[atomic]		
+gem(hero) : not hero(gem) 
		<- .print("Gem!");
			!ensure_pickup(C).
+gem(hero) : hero (gem) 
			<- .print("I'm already carrying a gem");
			!ensure_pickup(C).
			
@vaseTake[atomic]			
+vase(hero) : not hero(vase)
		<- .print("Vase!");
			!ensure_pickup(C).
+vase(hero) : hero (vase)
			<- .print("I'm already carrying a vase");
			!ensure_pickup(C).
			


/* Goals */
//The ensure pickup goal is defined for each item , when the agent beliefs there is one of the items
//then we tell the agent to perform the pick action. 
//After ensuring the pickup it will call the check inventory goal to check if it is has achieved the rule of satisfied.
+!ensure_pickup(C): coin(hero) <- .print("Picking up coin!"); 
								pick(coin);//perform pick up action 
								!ensure_pickup(C);////check goal achieved 
								!checkInventory(C).//check if holding all 3 items
+!ensure_pickup(C): gem(hero) <- .print("Picking up gem!"); 
								pick(gem);
								!ensure_pickup(C);
								!checkInventory(C).
+!ensure_pickup(C): vase(hero) <- .print("Picking up vase!"); 
								pick(vase);
								!ensure_pickup(C);
								!checkInventory(C).
+!ensure_pickup(_).//way of achieving goal if no coin/gem/vase


// we want to keep checking the inventory till we are satisfied
+!checkInventory(C) : satisfied <- .print("QUICK!! RETURN TO THE GOBLIN!");
									!carry_to(goblin).
									//carry to function	
//If we arnt satified we continue									
//+!checkInventory(C) : not satisfied <- .print(""). //prints errors if not stated
+!checkInventory(_).//way of achieving goal not satisfied 

// Remember where to go back
+!carry_to(R)  <- ?pos(hero,X,Y);//query the position 
				 -+pos(last,X,Y); //save last position
				 
				//takes to goblin and drops items		
				!take(coin,gem,vase,R);

				// goes back and continue to check
				!at(last);
				!check(slots).
				
//To take items C,G,V to L(Target location), make hero at L and drop items
//!at(L) needs to be called after each drop action as the agent will start returning to the position before dropping all the items away from the goblin.
+!take(C,G,V,L) : true
   <- !at(L);
		drop(C);
		!at(L)
   		drop(G);	  
		!at(L)
   		drop(V).


//If we are at L, then we have achieved the goal of being there.
+!at(L) : at(L).
// Otherwise, to be at L we move towards it, and try again to be 
// at L
+!at(L) <- ?pos(L,X,Y)
			move_towards(X,Y);
		   !at(L).

		   
		   
// Minor bugs:
//Very rare issue if agent is holding a coin and is then there is a two coins next to each other, as the agent skips the  first coin, it gets stuck on the second coin.
//Couldnt drop all the items at once without calling the !at function multiple times wasnt sure how to fix.

	  
