# Spider
Android app: Spider Solitaire using a single card deck

Set-up:
- From AndroidStudio, open the outer-most directory (Spider) as the project.
- Allow AndroidStudio to configure all the gradle stuff, then restart the program.

New features needed:
- Make layous look nicer
	- Main menu (res/layout/activity_main.xml)
	- Stats menu (res/layout/activity_stats.xml)
		* Cheat: tap bottom right side of screen to win game (for testing stats menu)
- Score (I just need to create the algorithm)
- Option in main menu to show rules
- Hints
- Make the front of the cards look nicer (bigger number, big suit image on body)
- Animations for: draw new cards (card moving to each stack), single tap of screen (card moving from stack to stack), completed stack (going up to top left)
- Visual response to illegal single tap (card spins if it's tapped but can't go anywhere)


-- Rules --
How to Play:
- Select a difficulty
	- Easy = 1 suit, Medium = 2 suits, Hard = 3 suits, Expert = 4 suits
- This game has the same rules as spider solitaire, but is set up like normal solitaire.
- Cards can be placed on any card with one higher value, or an empty stack.
- Card stacks can only be moved when the cards below are sequentially decreasing, and of the same suit.
- When 13 cards of the same suit, in order from K -> A, are created, a complete set is formed.
- When all 52 cards are completed, the game is won.
- Tap the hidden cards in the top right to draw 8 new cards (one is assigned to each stack).
- Press the Undo button in the bottom left to undo the previous move.
- If Undo is pressed twice in a row, only one move is relinquished. Additional consecutive undo's will not decrease move count.

Tips:
- A single tap of a card or stack of cards will move it to the most optimal stack.

Scoring:
- TBD
- Time into the game is the multiplier
- Actions have their own points
	- Completed stack = a lot
	- Revealing hidden card = a little

Move count:
- Every move adds one to the move count.
- Undo removes one move from the count.
- Consecutive undo's will not further reduce the move count.
