# Bank - Clean Architecture - Java  [![Build Status](https://travis-ci.com/MikhailovValentine/clean-architecture-java-bank-example.svg?branch=master)](https://travis-ci.com/github/MikhailovValentine/clean-architecture-java-bank-example) 
* [Introduction](#introduction)
* [Architecture](#architecture)
* [Building and Running](#building-and-running)
* [License](#license)

### Introduction
This is one more example of the [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html) implementation in java.
<br/> 
I have chosen bank apps - because it's business logic can be a token example of a "business logic oriented" application.
And at the same time bank app can be simplified to be small enough to understand basic ideas/advantages of the Clean Architecture.
<br/>
Is service are belongings to use-case level, for me the answer is yes.
 But what is make Clean Architecture different from Onion, Hexagonal, and other multilayers architectures?
<br/>
If we take a typical data-centric application it is seldom gonna create use-case scenarios that require more than 
one service and one data-source and presenter.

### Architecture

##### The main principle of the Clean Architecture
* Dependencies go from implementation to abstractions

##### Advantages
* Independent from other technologies like UI, databases, frameworks, etc.
* Better testable

We will operate with several abstractions(entities):
* Client
* Account
* Transaction
* Money
* Currency

And implement a couple of business use-case that is useful for any bank application:  
* A client registered, providing his data for identification.
* A client asks to get his profile information, providing identification data.
* A client stops been the bank client - deletes profile.
* A client creates a new(or another) account in some currency.
* A client adds some money to its account.
* A client takes some money from the account.
* A client transfers money from one account to another(his or  another client) account.
* A client requests information about his accounts - the amount of money on it and currency.
* A client requests his operations - all transactions.

And we need some connections to 'external work' (from our application point of view) so we need adapter/gateways:
* Client Data interface
* Account Data interface
* Transaction Data interface

### Building and Running
#### From Console
* building the application:
```
./gradlew clean build
```
* running the application test:
```
./gradlew :clean-achitecture-simple-impl:cleanTest :clean-achitecture-simple-impl:test
```
#### From the IntelliJ:
Importing the project - open the _build.gradle_ file as "a Project" in IntelliJ all dependencies will be loaded by IDE.



### License
This example is licensed with the [MIT license](LICENSE).