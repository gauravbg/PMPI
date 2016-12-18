# Player Match Performance Index
Stony Brook University - CSE 537 - AI Course Project

We are using the dataset available at https://www.kaggle.com/hugomathien/soccer

In this project, we are predicting the likelihood of a player to score in a particular match. In order to make the predict, we have used a Bayesian Network which is based on the following parameters:
- Team Strength
- Team Form
- Individual Player Rating
- Individual Player Form

After feeding this value to the Bayesian Network, we come up with the top 5 players who are likely to score in a selected match. We then check the results of our prediction with the actual result of that match.
