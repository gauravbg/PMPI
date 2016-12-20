# Player Match Performance Index
Stony Brook University - CSE 537 - AI Course Project

We are using the dataset available at https://www.kaggle.com/hugomathien/soccer

In this project, we are predicting the likelihood of a player to score in a particular match. In order to make the predict, we have used a Bayesian Network which is based on the following parameters:
- Team Strength
- Team Form
- Individual Player Rating
- Individual Player Form

Each of these factors is based on the following individual factors:
- Team Factors
    - Number of goals scored in the past few matches
    - Number of wins, draws and losses for that particular team in the last few matches
    - Total points scored by a particular team in the past few seasons
    - Ratings (strength) of players playing in that team
    
- Player Factors
  - Form of the player's team (based on the past 5 matches)
  - Position of that players i.e. Goalkeeper, Defender, Midfielder or Forward
  - Form of that player in the past 5 matches by computing the number of goals, assist and shots taken by the player
  - Overall rating of that player based on his attributes

After feeding these values to the Bayesian Network, we come up with the top 5 players who are likely to score in a selected match. We then check the results of our prediction with the actual result of that match.

![Alt text](https://cloud.githubusercontent.com/assets/13979092/21337925/9e9f8d82-c63f-11e6-9348-2aa7e4315781.png "Optional title")

We have tested our algorithm on the English Premier League, but this can be easily extended to any season of any league.
