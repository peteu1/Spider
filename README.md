# Spider
Android app: Spider Solitaire using a single card deck

NOTE: As of 4/15/20, stopping dev directly on master branch. Always keep master branch functioning, and develop new features on a separate branch which can be merged in after passing tests.

New features needed:
- Make layous look nicer
	- Main menu (res/layout/activity_main.xml)
	- Stats menu (res/layout/activity_stats.xml) [Good enough for v1.0]
		* Cheat: tap bottom right side of screen to win game (for testing stats menu)
- Score (I just need to create the algorithm)
- Option in main menu to show rules
- Make the front of the cards look nicer (bigger number, big suit image on body)
- Hints (use animation to show stack move for hint; don't modify game; set master.locked = true while hint animation showing)
- Animations for:
	- draw new cards (card moving to each stack), 
	- completed stack (going up to top left)
	- show hints
- Visual response to illegal single tap (cards in stack spin if tapped but can't go anywhere)
- Generate seed and shuffle deck according to random seed
- Loading/Splash screen
- Cache images instead of re-creating each time
- AppIntro to give breif overview of the app


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
