# Spider
Android app: Spider Solitaire using a single card deck

Set-up:
- From AndroidStudio, open the outer-most directory (Spider) as the project.
- Allow AndroidStudio to configure all the gradle stuff, then restart the program.

Considerations:
- Not sure if we should add all the gradle stuff to the gitignore, and let our own machines configure it on its own. This part I don't know anything about. But I got it set-up finally on new machine (with having to let it change those gradle/manifest/settings files) and fixed the app and can run/play it on my android.

New features needed:
- Timer
- Score
- Update rules and add option to show rules from main screen and within gameView
- Hints
- MainActivity.onPause(): save state
- MainActivity.onResume(): restore saved data
- Real card images/suits
- Animations for: draw new cards (card moving to each stack), single tap of screen (card moving from stack to stack), completed stack (going up to top left)
- Visual response to illegal single tap
- Database to track stats


-- Rules --
How to Play:
- blah blah

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
