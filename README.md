# Movie Recommendations

## Deliverable 2
Final (Kaggle) Score: 0.83711
Rank: 4
Kaggle name: Casper Boone

New/changed classes for this submission
* Ensemble
* EnsembleSource
* LatentFactors
* LatentFactorsCrossValidation
* LatentFactorsTestSet
* LatentFactorTraining
* Matrix
* Movie (changed)
* User (changed)
* Util (changed)

Most important features (not a complete list):
* Regularization
* Global/local biases
* Cross validation techniques
* Number of latent factors exploration, in LatentFactorTraining (tested from 1 to 29, outcome 9 or 23 are best, dependent on other parameters)
* Ensemble methods

The working of all code is explained either in JavaDoc or in in-code comments.

## Deliverable 1
Final (Kaggle) Score: 0.84225
Kaggle name: Casper Boone

Most important features (not a complete list):
* Item-Item collaborative filtering
* Cosine distance (with average subtraction), also tried Jaccard and Pearson correlation. Their implementation can still be found in Predictor.
* Multi threading for improved performance (total run time is about 180s)
* Similarity caching for improved performance (slow for first iterations, but then the algorithm moves on to predicting about 40k rating per 5s)
* Global / local biases
* Light version of cross validation that gives a RMSE as a result. Which can later on also be used for training parameters. (`CollaborativeFilteringTestSet`)

Short overview of the multithreaded workflow: `CollaborativeFiltering` divides the predictions(.csv) over all predictors (all predictors are a separate thread, the number of concurrent threads is limited to 20).
A `Predictor` is created with a given range (start to end) of predictions(.csv), for which it will predict the rating.

The working of all code is explained either in JavaDoc or in in-code comments.

