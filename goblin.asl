//If I have the vase in my location, then under all circumstances, I should stash the vase, and say thank you.
+vase(goblin) 
	: true 
	<- stash(vase);
	.print("Thank you for the vase!").
						
//If I have the coin in my location, then under all circumstances, I should stash the coin, and say thank you.
+coin(goblin) 
	: true 
	<- stash(coin);
	.print("Thank you for the coin!").
						
//If I have the gem in my location, then under all circumstances, I should stash the gem, and say thank you.
+gem(goblin) 
	: true 
	<- stash(gem);
	.print("Thank you for the gem!").
	
