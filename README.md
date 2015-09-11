## Place-Disambiguation: 
#### Combining Wikipedia and DBpedia for Place Name Disambiguation in Short Text

Program:
This project provides source code for disambiguating place names from short text. DBpedia and Wikipedia are combined as the background knowledge for place name disambiguation. To run the source code, you need to have the library of opencsv.jar (http://code.google.com/p/opencsv/downloads/detail?name=opencsv-2.4.jar&can=2&q=), which is used to read csv files.

Two major classes are used in the source code:
TestDataGenerator.java: This class divides the natural langauge paragraphs from government websites into short sentences. The generated file is called "testData.txt", which is used for testing the performance of the algorithm.

ModelTrainer.java: This class trains a combined model based the description in our corresponding paper. It then applies the model to the generated test data.


Data: 
The ground truth data is also provided in this project for further testing. The ground truth data contain two common place names in the U.S.: "Washington" and "Greenville". The ground truth data is organized into three folders:
originDBpedia: data downloaded from the DBpedia pages as triples. File format is .csv
originGovData: natural language descriptions retrieved from cities' government websites.
originWikipedia: content from Wikipedia pages, as natural language descriptions.

 
Important note: 
The data are put into two separate folders, i.e., folder of "Washington" and folder of "Greenville". To run the program against the data, please first copy one set of the data into the root directory.


