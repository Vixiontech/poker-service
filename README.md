# poker-service

11 March 2024
Version 1.0.0 with initial procedure

13 Marzo 2024 
Version 1.0.1 working game with data in frontend, WIP

25 Aprile 2024 
Version 1.0.2 Saving rake, prog, household, royal, straight, fourofkind in mongo

26 Aprile 2024
Version 1.0.3 Small fixes for progressive (fixed/not fixed), royalFlush, straightFlush and Four of a Kind

30 Aprile 2024
Version 1.0.4 now saving dealer name in message, based on Game Commons 4.9.5

2 maggio 2024
Version 1.0.5 small fix: progressive and rake now are float/double, NOT integer. Game Commons 4.9.8

3 maggio 2024
Version 1.0.6 poker now is loading the last saved license in database. WIP

7 maggio 2024
Version 1.0.7 removed license from PokerGameMessage, now it is in GameCommons 4.9.8.16 for all the games

8 maggio 2024 
Version 1.0.8 new endpoint common for all games (with username and phases with license based on Game commons 4.9.9.1). removed wrong auth to query games from mongo